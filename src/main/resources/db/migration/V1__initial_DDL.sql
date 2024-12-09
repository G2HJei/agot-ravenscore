CREATE TABLE tournament
(
    id             UUID         NOT NULL PRIMARY KEY,
    description    VARCHAR(2000),
    name           VARCHAR(255) NOT NULL,
    scoring        VARCHAR(32)  NOT NULL,
    hidden         BOOLEAN      NOT NULL,
    tournament_KEY VARCHAR(32)  NOT NULL,
    start_date     DATE         NOT NULL
);
CREATE TABLE substitute
(
    id            UUID        NOT NULL PRIMARY KEY,
    tournament_id UUID        NOT NULL,
    name          VARCHAR(64) NOT NULL,
    profile_links VARCHAR(255)[],
    CONSTRAINT fk_tournament_id FOREIGN KEY (tournament_id) REFERENCES tournament (id) ON DELETE cascade
);
CREATE TABLE tournament_stage
(
    id                  UUID         NOT NULL PRIMARY KEY,
    tournament_id       UUID         NOT NULL,
    qualification_count INTEGER      NOT NULL,
    participant_id_list UUID[]       NOT NULL,
    name                VARCHAR(255) NOT NULL,
    start_date          DATE         NOT NULL,
    CONSTRAINT fk_tournament_id FOREIGN KEY (tournament_id) REFERENCES tournament (id) ON DELETE cascade
);
CREATE TABLE participant
(
    id                         UUID        NOT NULL PRIMARY KEY,
    replacement_participant_id UUID,
    name                       VARCHAR(64) NOT NULL,
    profile_links              VARCHAR(255)[],
    CONSTRAINT fk_replacement_participant_id FOREIGN KEY (replacement_participant_id) REFERENCES participant (id) ON DELETE cascade
);
CREATE TABLE game
(
    id                  UUID         NOT NULL PRIMARY KEY,
    tournament_stage_id UUID         NOT NULL,
    participant_id_list UUID[]       NOT NULL,
    round               INTEGER      NOT NULL,
    link                VARCHAR(255) NOT NULL,
    type                VARCHAR(32)  NOT NULL,
    name                VARCHAR(255) NOT NULL,
    completed           BOOLEAN      NOT NULL,
    CONSTRAINT fk_tournament_stage_id FOREIGN KEY (tournament_stage_id) REFERENCES tournament_stage (id) ON DELETE cascade
);
CREATE TABLE player
(
    id             UUID        NOT NULL PRIMARY KEY,
    game_id        UUID        NOT NULL,
    participant_id UUID,
    castles        INTEGER,
    house          VARCHAR(16) NOT NULL,
    penalty_points SMALLINT    NOT NULL,
    RANK           INTEGER,
    score          INTEGER,
    CONSTRAINT fk_game_id FOREIGN KEY (game_id) REFERENCES game (id) ON DELETE cascade,
    CONSTRAINT fk_participant_id FOREIGN KEY (participant_id) REFERENCES participant (id) ON DELETE cascade
);