CREATE TABLE tournament
(
    id             uuid         NOT NULL PRIMARY KEY,
    description    VARCHAR(2000),
    name           VARCHAR(255) NOT NULL,
    scoring        VARCHAR(32)  NOT NULL,
    hidden         BOOLEAN      NOT NULL,
    tournament_KEY VARCHAR(32)  NOT NULL,
    start_date     DATE         NOT NULL
);
CREATE TABLE substitute
(
    id            uuid        NOT NULL PRIMARY KEY,
    tournament_id uuid        NOT NULL,
    name          VARCHAR(64) NOT NULL,
    profile_links VARCHAR(255)[],
    CONSTRAINT fk_tournament_id FOREIGN KEY (tournament_id) REFERENCES tournament (id) ON DELETE cascade
);
CREATE TABLE tournament_stage
(
    id                  uuid         NOT NULL PRIMARY KEY,
    tournament_id       uuid         NOT NULL,
    stage_number        INTEGER      NOT NULL,
    participant_id_list uuid[]       NOT NULL,
    name                VARCHAR(255) NOT NULL,
    start_date          DATE         NOT NULL,
    CONSTRAINT fk_tournament_id FOREIGN KEY (tournament_id) REFERENCES tournament (id) ON DELETE cascade
);
CREATE TABLE participant
(
    id                         uuid        NOT NULL PRIMARY KEY,
    replacement_participant_id uuid,
    name                       VARCHAR(64) NOT NULL,
    profile_links              VARCHAR(255)[],
    CONSTRAINT fk_replacement_participant_id FOREIGN KEY (replacement_participant_id) REFERENCES participant (id) ON DELETE cascade
);
CREATE TABLE game
(
    id                  uuid         NOT NULL PRIMARY KEY,
    tournament_stage_id uuid         NOT NULL,
    participant_id_list uuid[]       NOT NULL,
    round               INTEGER      NOT NULL,
    game_link           VARCHAR(255) NOT NULL,
    game_type           VARCHAR(32)  NOT NULL,
    name                VARCHAR(255) NOT NULL,
    CONSTRAINT fk_tournament_stage_id FOREIGN KEY (tournament_stage_id) REFERENCES tournament_stage (id) ON DELETE cascade
);
CREATE TABLE player
(
    id             uuid        NOT NULL PRIMARY KEY,
    game_id        uuid        NOT NULL,
    participant_id uuid,
    castles        INTEGER,
    house          VARCHAR(16) NOT NULL,
    iron_throne    INTEGER,
    land_areas     INTEGER,
    penalty_points SMALLINT    NOT NULL,
    RANK           INTEGER,
    score          INTEGER,
    supply         INTEGER,
    CONSTRAINT fk_game_id FOREIGN KEY (game_id) REFERENCES game (id) ON DELETE cascade,
    CONSTRAINT fk_participant_id FOREIGN KEY (participant_id) REFERENCES participant (id) ON DELETE cascade
);