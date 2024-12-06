create table tournament
(
    id             uuid         not null primary key,
    description    varchar(2000),
    name           varchar(255) not null,
    scoring        varchar(32)  not null,
    tournament_key varchar(32)  not null
);


create table participant
(
    id                         uuid         not null primary key,
    tournament_id              uuid         not null,
    replacement_participant_id uuid,
    name                       varchar(64)  not null,
    substitute                 boolean      not null,
    profile_link               varchar(255) not null,
    constraint fk_replacement_participant_id foreign key (replacement_participant_id) references participant (id) on delete cascade,
    constraint fk_tournament_id foreign key (tournament_id) references tournament (id) on delete cascade
);

create table tournament_stage
(
    id                  uuid         not null primary key,
    tournament_id       uuid         not null,
    participant_id_list uuid[]       not null,
    name                varchar(255) not null,
    constraint fk_tournament_id foreign key (tournament_id) references tournament (id) on delete cascade
);

create table game
(
    id                  uuid         not null primary key,
    tournament_stage_id uuid         not null,
    participant_id_list uuid[]       not null,
    current_round       integer      not null,
    game_link           varchar(255) not null,
    game_type           varchar(32)  not null,
    name                varchar(255) not null,

    constraint fk_tournament_stage_id foreign key (tournament_stage_id) references tournament_stage (id) on delete cascade
);


create table player
(
    id             uuid        not null primary key,
    game_id        uuid        not null,
    participant_id uuid,
    castles        integer,
    house          varchar(16) not null,
    iron_throne    integer,
    land_areas     integer,
    penalty_points smallint    not null,
    rank           integer,
    score          integer,
    supply         integer,
    constraint fk_game_id foreign key (game_id) references game (id) on delete cascade,
    constraint fk_participant_id foreign key (participant_id) references participant (id) on delete cascade
);

