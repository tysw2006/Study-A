package com.studycafe.service;
import com.studycafe.domain.*;
import com.studycafe.dto.AuthRequest;
import com.studycafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final com.studycafe.repository.ReservationRepository reservationRepository;

    @Transactional
    public User register(AuthRequest req) {
        if(userRepository.findByEmail(req.getEmail()).isPresent()){
            throw new RuntimeException("Email already exists");
        }
        User user = User.builder()
                .email(req.getEmail())
                .password(req.getPassword())
                .name(req.getName())
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    public User login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(u -> u.getPassword().equals(password))
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        reservationRepository.deleteByUser(user); // 외래키 제약조건 방지 (유저의 예약 기록 먼저 삭제)
        userRepository.delete(user);
    }
}
