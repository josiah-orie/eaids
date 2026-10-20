-- ============================================================
-- Link match_stat_entries to match_fixtures (optional FK)
-- ============================================================

ALTER TABLE match_stat_entries
    ADD COLUMN fixture_id BINARY(16) NULL AFTER camp_id,
    ADD CONSTRAINT fk_match_stat_entries_fixture FOREIGN KEY (fixture_id) REFERENCES match_fixtures (id),
    ADD INDEX idx_match_stat_entries_fixture (fixture_id);
