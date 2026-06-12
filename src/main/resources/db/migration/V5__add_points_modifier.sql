ALTER TABLE participant
    ADD COLUMN points_modifier numeric(3, 2) DEFAULT 1;

ALTER TABLE substitute
    ADD COLUMN points_modifier numeric(3, 2) DEFAULT 1;

ALTER TABLE game
    ADD COLUMN points_modifier numeric(3, 2) DEFAULT 1;