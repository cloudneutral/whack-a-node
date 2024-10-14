create table if not exists wan_user_profile
(
    id        uuid default gen_random_uuid(),
    version   int         not null default 0,
    payload   jsonb       not null,
    expire_at timestamptz not null,

    primary key (id, version)
);

