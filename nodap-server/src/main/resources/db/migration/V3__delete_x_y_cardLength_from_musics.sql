-- ============================================
-- musics 테이블에서 pos_x, pos_y, card_length 삭제
-- Version: 3
-- Created: 2026-01-21
-- ============================================

ALTER TABLE musics
    DROP COLUMN pos_x,
    DROP COLUMN pos_y,
    DROP COLUMN card_length;

