CREATE DATABASE IF NOT EXISTS studycafe;
USE studycafe;

-- 1. 회원 테이블
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER', -- 'USER', 'ADMIN'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 좌석 테이블
CREATE TABLE seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seat_number VARCHAR(50) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE -- 사용 가능 여부
);

-- 3. 스터디 룸 테이블
CREATE TABLE study_rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_name VARCHAR(100) NOT NULL UNIQUE,
    capacity INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE -- 사용 가능 여부
);

-- 4. 예약 테이블
CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    seat_id BIGINT,
    study_room_id BIGINT,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'CANCELLED', 'COMPLETED'
    reservation_type VARCHAR(50) NOT NULL, -- 'SEAT', 'ROOM'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (seat_id) REFERENCES seats(id),
    FOREIGN KEY (study_room_id) REFERENCES study_rooms(id)
);

-- 초기 데이터 삽입 (테스트용)
INSERT INTO users (email, password, name, role) VALUES 
('admin@studycafe.com', '{bcrypt}$2a$10$X/h2y/l2.W./w/U.o.Q.e.6.u./Q.3.z.l.q.r.e.c.z.v.e.w.v.a', '관리자', 'ADMIN'), -- 패스워드: admin123
('user@test.com', '{bcrypt}$2a$10$k/x/y/l2.W./w/U.o.Q.e.6.u./Q.3.z.l.q.r.e.c.z.v.e.w.v.a', '테스트유저', 'USER');

INSERT INTO seats (seat_number) VALUES ('A1'), ('A2'), ('A3'), ('B1'), ('B2'), ('B3');
INSERT INTO study_rooms (room_name, capacity) VALUES ('Room 1 (4인)', 4), ('Room 2 (6인)', 6);
