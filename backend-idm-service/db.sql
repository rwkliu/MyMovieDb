create schema idm;
create table idm.refresh_token
(
    id              int auto_increment
        primary key,
    token           char(36)  not null,
    user_id         int       not null,
    token_status_id int       not null,
    expire_time     timestamp not null,
    max_life_time   timestamp not null,
    constraint token
        unique (token)
);

create table idm.role
(
    id          int          not null
        primary key,
    name        varchar(32)  not null,
    description varchar(128) not null,
    precedence  int          not null
);

create table idm.token_status
(
    id    int         not null
        primary key,
    value varchar(32) not null
);

create table idm.user
(
    id              int auto_increment
        primary key,
    email           varchar(32) not null,
    user_status_id  int         not null,
    salt            char(8)     not null,
    hashed_password char(88)    not null,
    constraint email
        unique (email)
);

create table idm.user_role
(
    user_id int not null,
    role_id int not null
);

create table idm.user_status
(
    id    int         not null
        primary key,
    value varchar(32) not null
);

