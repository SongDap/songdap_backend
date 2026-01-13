-- ============================================
-- Album 테이블, musics 테이블, users 테이블 수정
-- Version: 2
-- Created: 2026-01-10
-- ============================================



-- ============================================
-- 1. albums 테이블
-- ============================================
ALTER TABLE albums
ADD COLUMN color VARCHAR(20) NOT NULL DEFAULT '';

ALTER TABLE albums
DROP COLUMN category,
DROP COLUMN category_detail;

-- ============================================
-- 2. musics 테이블
-- ============================================
ALTER TABLE musics
ADD COLUMN image VARCHAR(512) NOT NULL DEFAULT '',
ADD COLUMN pos_x DOUBLE NOT NULL DEFAULT 0,
ADD COLUMN pos_y DOUBLE NOT NULL DEFAULT 0,
ADD COLUMN card_length DOUBLE NOT NULL DEFAULT 0;

-- ============================================
-- 3. users 테이블
-- ============================================
ALTER TABLE users
MODIFY COLUMN email VARCHAR(128) NULL;





