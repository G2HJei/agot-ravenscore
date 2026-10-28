CREATE TABLE player_inactivity
(
    id             UUID         NOT NULL PRIMARY KEY,
    player_id      UUID         NOT NULL,
    start_time     TIMESTAMP(6) NOT NULL,
    duration_hours INTEGER      NOT NULL,
    CONSTRAINT fk_player_inactivity_player_id FOREIGN KEY (player_id) REFERENCES player (id) ON DELETE cascade,
    CONSTRAINT uk_player_inactivity_player_id_start_time UNIQUE (player_id, start_time)
);
-- penalties given before inactivity tracking existed: one dummy entry per penalized player
INSERT INTO player_inactivity (id, player_id, start_time, duration_hours)
SELECT gen_random_uuid(), id, TIMESTAMP '1970-01-01 00:00:00', 0
FROM player
WHERE penalty_points > 0;
