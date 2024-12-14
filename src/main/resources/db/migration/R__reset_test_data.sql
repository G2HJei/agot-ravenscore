TRUNCATE TABLE tournament CASCADE;
TRUNCATE TABLE substitute CASCADE;
TRUNCATE TABLE tournament_stage CASCADE;
TRUNCATE TABLE participant CASCADE;
TRUNCATE TABLE game CASCADE;
TRUNCATE TABLE player CASCADE;

-- Insert into tournament table
INSERT INTO tournament (id, description, name, scoring, hidden, tournament_key, start_date, pinned)
VALUES ('11111111-1111-1111-1111-111111111111'::UUID, 'Winter Tournament of Westeros', 'Winter Clash',
        'RANKING_PLUS_CASTLES', false, 'WINT2024', '2024-01-01', true);

-- Insert into participant table
INSERT INTO participant (id, replacement_participant_id, name, profile_links)
VALUES ('22222222-2222-2222-2222-222222222222'::UUID, NULL, 'Eddard Stark',
        ARRAY ['https://example.com/profiles/eddard']),
       ('33333333-3333-3333-3333-333333333333'::UUID, '72222222-2222-2222-2222-222222222222'::UUID, 'Jon Snow',
        ARRAY ['https://thronemaster.com/profiles/jon']),
       ('72222222-2222-2222-2222-222222222222'::UUID, NULL, 'Petyr Baelish',
        ARRAY ['https://thronemaster.com/profiles/Littlefinger']),
       ('44444444-4444-4444-4444-444444444444'::UUID, NULL, 'Daenerys Targaryen',
        ARRAY ['https://swordsandravens.com/profiles/daenerys', 'Daenerys69']);

-- Insert into tournament_stage table
INSERT INTO tournament_stage (id, tournament_id, qualification_count, participant_id_list, name, start_date)
VALUES ('55555555-5555-5555-5555-555555555555'::UUID, '11111111-1111-1111-1111-111111111111'::UUID, 1,
        ARRAY ['22222222-2222-2222-2222-222222222222'::UUID, '33333333-3333-3333-3333-333333333333'::UUID, '72222222-2222-2222-2222-222222222222'::UUID, '44444444-4444-4444-4444-444444444444'::UUID],
        'Winter War Round 1', '2024-01-01');

-- Insert into game table #1
INSERT INTO game (id, tournament_stage_id, participant_id_list, round, link, type, name, completed)
VALUES ('66666666-6666-6666-6666-666666666666'::UUID, '55555555-5555-5555-5555-555555555555'::UUID,
        ARRAY ['22222222-2222-2222-2222-222222222222'::UUID, '72222222-2222-2222-2222-222222222222'::UUID, '44444444-4444-4444-4444-444444444444'::UUID],
        10,
        'https://example.com/games/winterwar1', 'A_DANCE_WITH_DRAGONS', 'Battle of the North', true);

-- Insert into game table #2
INSERT INTO game (id, tournament_stage_id, participant_id_list, round, link, type, name, completed)
VALUES ('16666666-6666-6666-6666-666666666666'::UUID, '55555555-5555-5555-5555-555555555555'::UUID,
        ARRAY ['22222222-2222-2222-2222-222222222222'::UUID, '72222222-2222-2222-2222-222222222222'::UUID, '44444444-4444-4444-4444-444444444444'::UUID],
        2,
        'https://example.com/games/winterwar2', 'A_FEAST_FOR_CROWS', 'Battle of the South', false);

-- Insert into player table #1
INSERT INTO player (id, game_id, participant_id, castles, house, penalty_points, points)
VALUES ('77777777-7777-7777-7777-777777777777'::UUID, '66666666-6666-6666-6666-666666666666'::UUID,
        '22222222-2222-2222-2222-222222222222'::UUID, 3, 'STARK', 2, 7),
       ('37777777-7777-7777-7777-777777777777'::UUID, '66666666-6666-6666-6666-666666666666'::UUID,
        '72222222-2222-2222-2222-222222222222'::UUID, 1, 'ARRYN', 1, 1),
       ('88888888-8888-8888-8888-888888888888', '66666666-6666-6666-6666-666666666666'::UUID,
        '44444444-4444-4444-4444-444444444444'::UUID, 2, 'TARGARYEN', 0, 5);

-- Insert into player table #2
INSERT INTO player (id, game_id, participant_id, castles, house, penalty_points, points)
VALUES ('17777777-7777-7777-7777-777777777777'::UUID, '16666666-6666-6666-6666-666666666666'::UUID,
        null, 3, 'GREYJOY', 0, 0),
       ('27777777-7777-7777-7777-777777777777'::UUID, '16666666-6666-6666-6666-666666666666'::UUID,
        null, 3, 'ARRYN', 0, 0),
       ('18888888-8888-8888-8888-888888888888', '16666666-6666-6666-6666-666666666666'::UUID,
        null, 2, 'LANNISTER', 0, 0);

-- Insert into substitute table
INSERT INTO substitute (id, name, profile_links, tournament_id)
VALUES ('99999999-9999-9999-9999-999999999999'::UUID, 'Balon Greyjoy', ARRAY ['https://discord.com/profiles/balon'],
        '11111111-1111-1111-1111-111111111111'::UUID);