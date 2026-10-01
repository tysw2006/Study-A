-- 1. 데이터베이스 선택
CREATE DATABASE IF NOT EXISTS studycafe;
USE studycafe;

-- 기존 테이블 초기화 (외래키 제약조건 방지를 위해 순서대로 삭제)
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS study_rooms;
DROP TABLE IF EXISTS seats;
DROP TABLE IF EXISTS users;

-- ==========================================
-- [1] 테이블 구조 설계 (DDL)
-- ==========================================

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seat_number VARCHAR(50) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE study_rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_name VARCHAR(100) NOT NULL UNIQUE,
    capacity INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    seat_id BIGINT,
    study_room_id BIGINT,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', 
    reservation_type VARCHAR(50) NOT NULL, 
    total_price INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (seat_id) REFERENCES seats(id),
    FOREIGN KEY (study_room_id) REFERENCES study_rooms(id)
);

-- ==========================================
-- [2] 초기 테스트 데이터 세팅 (DML)
-- ==========================================

INSERT INTO users (email, password, name, role) VALUES 
('admin@studycafe.com', '1234', '관리자', 'ADMIN'),
('user@test.com', '0987', '테스트유저', 'USER');

INSERT INTO seats (seat_number) VALUES 
('A1'), ('A2'), ('A3'), ('A4'), ('A5'), ('A6'),
('B1'), ('B2'), ('B3'), ('B4'), ('B5'), ('B6'),
('C1'), ('C2'), ('C3'), ('C4'), ('C5'), ('C6');

INSERT INTO study_rooms (room_name, capacity) VALUES 
('Room 1 (2인)', 2), 
('Room 2 (4인)', 4),
('Room 3 (8인)', 8);
