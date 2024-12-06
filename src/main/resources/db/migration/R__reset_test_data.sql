TRUNCATE TABLE tournament CASCADE;
TRUNCATE TABLE participant CASCADE;
TRUNCATE TABLE tournament_stage CASCADE;
TRUNCATE TABLE game CASCADE;
TRUNCATE TABLE player CASCADE;

-- Insert into tournament table
INSERT INTO tournament (id, description, name, scoring, hidden, tournament_key)
VALUES ('11111111-1111-1111-1111-111111111111'::UUID, 'Winter Tournament of Westeros', 'Winter Clash',
        'POSITION_PLUS_CITIES', false, 'WINT2024');

-- Insert into participant table
INSERT INTO participant (id, replacement_participant_id, name, profile_link)
VALUES ('22222222-2222-2222-2222-222222222222'::UUID, NULL, 'Eddard Stark', 'https://example.com/profiles/eddard'),
       ('33333333-3333-3333-3333-333333333333'::UUID, '22222222-2222-2222-2222-222222222222'::UUID, 'Jon Snow',
        'https://example.com/profiles/jon'),
       ('44444444-4444-4444-4444-444444444444'::UUID, NULL, 'Daenerys Targaryen',
        'https://example.com/profiles/daenerys');

-- Insert into tournament_stage table
INSERT INTO tournament_stage (id, tournament_id, participant_id_list, name)
VALUES ('55555555-5555-5555-5555-555555555555'::UUID, '11111111-1111-1111-1111-111111111111'::UUID,
        ARRAY ['22222222-2222-2222-2222-222222222222'::UUID, '44444444-4444-4444-4444-444444444444'::UUID],
        'Winter War Round 1');

-- Insert into game table
INSERT INTO game (id, tournament_stage_id, participant_id_list, current_round, game_link, game_type, name)
VALUES ('66666666-6666-6666-6666-666666666666'::UUID, '55555555-5555-5555-5555-555555555555'::UUID,
        ARRAY ['22222222-2222-2222-2222-222222222222'::UUID, '44444444-4444-4444-4444-444444444444'::UUID], 1,
        'https://example.com/games/winterwar1', 'standard', 'Battle of the North');

-- Insert into player table
INSERT INTO player (id, game_id, participant_id, castles, house, iron_throne, land_areas, penalty_points, rank, score,
                    supply)
VALUES ('77777777-7777-7777-7777-777777777777'::UUID, '66666666-6666-6666-6666-666666666666'::UUID,
        '22222222-2222-2222-2222-222222222222'::UUID, 3, 'Stark', 1, 7, 0, 1, 15, 5),
       ('88888888-8888-8888-8888-888888888888', '66666666-6666-6666-6666-666666666666'::UUID,
        '44444444-4444-4444-4444-444444444444'::UUID, 2, 'Targaryen', 0, 6, 2, 2, 12, 4);

-- Insert into substitute table
INSERT INTO substitute (id, name, profile_link)
VALUES ('99999999-9999-9999-9999-999999999999'::UUID, 'Balon Greyjoy', 'https://example.com/profiles/balon');