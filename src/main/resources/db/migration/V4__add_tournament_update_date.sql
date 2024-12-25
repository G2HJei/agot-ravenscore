ALTER TABLE tournament
    ADD COLUMN last_updated TIMESTAMP(6) NOT NULL DEFAULT current_date;