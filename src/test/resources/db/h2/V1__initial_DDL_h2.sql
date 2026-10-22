-- Test-only H2 equivalent of the immutable db/migration/V1__initial_DDL.sql. Never edit.
-- Differences: IF NOT EXISTS, "TYPE ARRAY" instead of PostgreSQL's "TYPE[]", and unique constraint names
-- (H2 scopes constraint names per schema, PostgreSQL per table).
-- Flyway baselines the resulting schema at version 1 and applies V2+ from db/migration unchanged.
CREATE TABLE IF NOT EXISTS tournament
(
    id             UUID         NOT NULL PRIMARY KEY,
    description    VARCHAR(2000),
    name           VARCHAR(255) NOT NULL,
    scoring        VARCHAR(32)  NOT NULL,
    hidden         BOOLEAN      NOT NULL,
    tournament_key VARCHAR(32)  NOT NULL,
    start_date     TIMESTAMP(6) NOT NULL,
    pinned         BOOLEAN      NOT NULL
);
CREATE TABLE IF NOT EXISTS substitute
(
    id            UUID        NOT NULL PRIMARY KEY,
    tournament_id UUID        NOT NULL,
    name          VARCHAR(64) NOT NULL,
    profile_links VARCHAR(255) ARRAY,
    CONSTRAINT fk_substitute_tournament_id FOREIGN KEY (tournament_id) REFERENCES tournament (id) ON DELETE cascade
);
CREATE TABLE IF NOT EXISTS tournament_stage
(
    id                  UUID         NOT NULL PRIMARY KEY,
    tournament_id       UUID         NOT NULL,
    qualification_count INTEGER      NOT NULL,
    participant_id_list UUID ARRAY   NOT NULL,
    name                VARCHAR(255) NOT NULL,
    start_date          TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_stage_tournament_id FOREIGN KEY (tournament_id) REFERENCES tournament (id) ON DELETE cascade
);
CREATE TABLE IF NOT EXISTS participant
(
    id                         UUID        NOT NULL PRIMARY KEY,
    replacement_participant_id UUID,
    name                       VARCHAR(64) NOT NULL,
    profile_links              VARCHAR(255) ARRAY,
    CONSTRAINT fk_replacement_participant_id FOREIGN KEY (replacement_participant_id) REFERENCES participant (id) ON DELETE cascade
);
CREATE TABLE IF NOT EXISTS game
(
    id                  UUID         NOT NULL PRIMARY KEY,
    tournament_stage_id UUID         NOT NULL,
    participant_id_list UUID ARRAY   NOT NULL,
    round               INTEGER,
    link                VARCHAR(255),
    type                VARCHAR(32)  NOT NULL,
    name                VARCHAR(255) NOT NULL,
    completed           BOOLEAN      NOT NULL,
    CONSTRAINT fk_tournament_stage_id FOREIGN KEY (tournament_stage_id) REFERENCES tournament_stage (id) ON DELETE cascade
);
CREATE TABLE IF NOT EXISTS player
(
    id             UUID        NOT NULL PRIMARY KEY,
    game_id        UUID        NOT NULL,
    participant_id UUID,
    castles        INTEGER,
    house          VARCHAR(16) NOT NULL,
    penalty_points SMALLINT    NOT NULL,
    points         INTEGER,
    CONSTRAINT fk_game_id FOREIGN KEY (game_id) REFERENCES game (id) ON DELETE cascade,
    CONSTRAINT fk_participant_id FOREIGN KEY (participant_id) REFERENCES participant (id) ON DELETE cascade
);
