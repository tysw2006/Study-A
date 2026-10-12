package com.studycafe.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    private final Map<String, VerificationInfo> verificationStorage = new ConcurrentHashMap<>();

    // 1. 인증번호 생성 및 메일 발송 (3분 유효기간)
    public void sendVerificationCode(String email) {
        String code = createRandomCode();
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(3); // [수정] 3분으로 변경

        // 재발급 시 기존 시도 횟수 및 정보 초기화
        verificationStorage.put(email, new VerificationInfo(code, expireTime, false));

        sendEmail(email, code);
    }

    // 2. 인증번호 검증 (5회 틀릴 시 즉시 폐기)
    public void verifyCode(String email, String code) {
        VerificationInfo info = verificationStorage.get(email);

        if (info == null) {
            throw new RuntimeException("인증 요청 내역이 없습니다. 인증번호를 먼저 발급받아주세요.");
        }

        // 3분 유효시간 만료 확인
        if (LocalDateTime.now().isAfter(info.getExpireTime())) {
            verificationStorage.remove(email);
            throw new RuntimeException("인증번호 유효시간(3분)이 만료되었습니다. 다시 발급받아주세요.");
        }

        // 5회 초과 실패 여부 확인
        if (info.getFailedAttempts() >= 5) {
            verificationStorage.remove(email);
            throw new RuntimeException("인증 시도 횟수(5회)를 초과하여 만료되었습니다. 다시 발급받아주세요.");
        }

        // 일치 여부 판별
        if (info.getCode().equals(code)) {
            info.setVerified(true); // 인증 완료
        } else {
            info.increaseFailedAttempts();
            int remaining = 5 - info.getFailedAttempts();

            if (remaining <= 0) {
                verificationStorage.remove(email); // 5회 소진 시 즉시 삭제
                throw new RuntimeException("인증 시도 횟수(5회)를 모두 초과했습니다. 다시 발급받아주세요.");
            }
            throw new RuntimeException("인증번호가 일치하지 않습니다. (남은 시도: " + remaining + "회)");
        }
    }

    // 3. 최종 회원가입 시 인증 완료 여부 확인
    public boolean isEmailVerified(String email) {
        VerificationInfo info = verificationStorage.get(email);
        return info != null && info.isVerified();
    }

    // 4. 회원가입 완료 후 인증 세션 삭제
    public void clearVerification(String email) {
        verificationStorage.remove(email);
    }

    // 실제 메일 전송
    private void sendEmail(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, "Studycafe");
            helper.setTo(toEmail);
            helper.setSubject("[Studycafe] 회원가입 인증번호 안내");

            String htmlContent = "<div style='max-width: 500px; margin: 0 auto; padding: 20px; font-family: sans-serif; border: 1px solid #e2e8f0; border-radius: 8px;'>"
                    + "<h2 style='color: #1e293b; border-bottom: 2px solid #f1f5f9; padding-bottom: 12px; margin-top: 0;'>스터디카페 본인 확인</h2>"
                    + "<p style='color: #475569; font-size: 15px; line-height: 1.6;'>안녕하세요. 스터디카페 회원가입을 위한 인증번호입니다.</p>"
                    + "<p style='color: #475569; font-size: 15px;'>화면의 인증번호 입력창에 아래 6자리 숫자를 입력해 주세요.</p>"
                    + "<div style='background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 18px; text-align: center; margin: 25px 0;'>"
                    + "<span style='font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #ff6b00;'>" + code + "</span>"
                    + "</div>"
                    + "<p style='color: #94a3b8; font-size: 13px;'>* 인증번호의 유효시간은 3분입니다.</p>"
                    + "<p style='color: #94a3b8; font-size: 13px;'>* 5회 이상 오입력 시 인증번호가 자동 폐기됩니다.</p>"
                    + "<hr style='border: none; border-top: 1px solid #f1f5f9; margin: 20px 0;'/>"
                    + "<p style='color: #cbd5e1; font-size: 11px; margin-bottom: 0;'>© Studycafe Team. All rights reserved.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("인증메일 발송 완료: {}", toEmail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("메일 발송 실패", e);
            throw new RuntimeException("이메일 발송에 실패했습니다. 관리자에게 문의하세요.");
        }
    }

    private String createRandomCode() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(1000000);
        return String.format("%06d", num);
    }

    // 내부 저장 객체 (실패 횟수 카운터 추가)
    private static class VerificationInfo {
        private final String code;
        private final LocalDateTime expireTime;
        private boolean verified;
        private int failedAttempts = 0; // [추가] 실패 횟수

        public VerificationInfo(String code, LocalDateTime expireTime, boolean verified) {
            this.code = code;
            this.expireTime = expireTime;
            this.verified = verified;
        }

        public String getCode() { return code; }
        public LocalDateTime getExpireTime() { return expireTime; }
        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }
        public int getFailedAttempts() { return failedAttempts; }
        public void increaseFailedAttempts() { this.failedAttempts++; }
    }
}