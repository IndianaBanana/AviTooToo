--Система размещения частных объявлений.
--
--1) Регистрация пользователя в системе (пользователя и администратора).
--2) Редактирование профиля.
--3) Просмотр списка объявлений. Поиск и фильтрация.
--4) Возможность добавления / редактирования / удаления объявлений.
--5) Возможность оставлять комментарии под объявлениями.
--6) Организация личной переписки покупателя и продавца.
--7) Возможность проплатить отображение объявления в топе выдачи.
--8) Система рейтингов продавцов, влияющая на положение объявлений продавца в поисковой выдаче.
--   Чем ниже рейтинг, тем ниже объявление в выдаче.
--9) История продаж пользователя.

create extension if not exists pgcrypto;

--1) Регистрация пользователя в системе (пользователя и администратора).
--2) Редактирование профиля.
create table if not exists "user" (
    user_id uuid primary key default gen_random_uuid(),
    first_name varchar(255) not null,
    last_name varchar(255) not null,
    phone varchar(255) not null unique,
    username varchar(255) not null unique, -- probably email
    password varchar(255) not null,
    role varchar(255) not null
);

create table if not exists rating (
    user_id uuid,
    rater_id uuid,
    rating_value smallint not null check (rating_value >= 1 and rating_value <= 5),
    check (user_id != rater_id),
    foreign key (user_id) references "user"(user_id) on delete cascade,
    foreign key (rater_id) references "user"(user_id) on delete cascade,
    primary key (user_id, rater_id)
);

--8) Система рейтингов продавцов, влияющая на положение объявлений продавца в поисковой выдаче.
-- будем обновлять данные о рейтинге ВСЕХ продавцов периодически (например, раз в 15 минут или реже)
-- пользователю поставившему оценку надо обязательно вернуть в ответ что его оценка учтена
-- и рейтинг будет пересчитан в течении какого-то времени.
create materialized view if not exists user_rating as
    select
        user_id,
        avg(rating_value) as average_rating,
        count(*) as rating_count
    from rating
    group by user_id
with data;
create unique index if not exists index_user_rating_user_id -- заместо первичного ключа
on user_rating(user_id);
--для точности понимания на диаграммах
comment on materialized view user_rating is 'materialized view';
comment on column user_rating.user_id is 'grouped by. has index';
comment on column user_rating.average_rating is 'avg(rating.rating_value)';
comment on column user_rating.rating_count is 'count(rating.*)';
--refresh materialized view user_rating; -- для обновления view

create table if not exists city (
    city_id uuid primary key default gen_random_uuid(),
    name varchar(255) unique not null
);

create table if not exists advertisement_type (
    advertisement_type_id uuid primary key default gen_random_uuid(),
    name varchar(255) unique not null
);

--3) Просмотр списка объявлений. Поиск и фильтрация.
--4) Возможность добавления / редактирования / удаления объявлений.
--7) Возможность проплатить отображение объявления в топе выдачи.
create table if not exists advertisement (
    advertisement_id uuid primary key default gen_random_uuid(),
    user_id uuid not null, -- the user who created the advertisement
    city_id uuid not null,
    advertisement_type_id uuid not null,
    title varchar(255) not null,
    description text not null,
    price numeric not null,
    quantity integer not null,
    is_paid boolean not null default false, -- if the advertisement is paid or not. for showing it in top
    create_date timestamp not null default now(),
    close_date timestamp,
    foreign key (user_id) references "user"(user_id) on delete cascade,
    foreign key (city_id) references city(city_id) on delete cascade,
    foreign key (advertisement_type_id) references advertisement_type(advertisement_type_id) on delete cascade
);
create index if not exists index_advertisement_user_id on advertisement(user_id);
create index if not exists index_advertisement_city_id on advertisement(city_id);
create index if not exists index_advertisement_advertisement_type_id on advertisement(advertisement_type_id);

--9) История продаж пользователя.
create table if not exists sale_history (
    sale_history_id uuid primary key default gen_random_uuid(),
    advertisement_id uuid not null,
    buyer_id uuid,
    sale_date timestamp not null default now(),
    quantity smallint not null,
    foreign key (buyer_id) references "user"(user_id) on delete set null,
    foreign key (advertisement_id) references advertisement(advertisement_id) on delete cascade
);

--5) Возможность оставлять комментарии под объявлениями.
create table if not exists comment (
    comment_id uuid primary key default gen_random_uuid(),
    advertisement_id uuid not null, -- the advertisement to which the comment is attached
    commenter_id uuid not null, -- the user who wrote the comment
    root_comment_id uuid, -- useful for selecting nested comments (less selections)
    parent_comment_id uuid, -- the comment to which the comment is attached
    comment_text text not null,
    comment_date timestamp not null default now(),
    check (comment_id != parent_comment_id and comment_id != root_comment_id),
    foreign key (commenter_id) references "user"(user_id) on delete set null,
    foreign key (advertisement_id) references advertisement(advertisement_id) on delete cascade,
    foreign key (root_comment_id) references comment(comment_id) on delete restrict,
    foreign key (parent_comment_id) references comment(comment_id) on delete restrict
);
create index if not exists index_comment_advertisement_id on comment(advertisement_id);

--6) Организация личной переписки покупателя и продавца.
create table if not exists message (
    message_id uuid primary key default gen_random_uuid(),
    advertisement_id uuid,
    sender_id uuid not null,
    recipient_id uuid not null,
    message_text text not null,
    message_date timestamp not null default now(),
    is_read boolean not null default false,
    foreign key (sender_id) references "user"(user_id) on delete set null,
    foreign key (recipient_id) references "user"(user_id) on delete set null,
    foreign key (advertisement_id) references advertisement(advertisement_id) on delete cascade,
    check (sender_id != recipient_id)
);


-- inserts
insert into city (name)
values
    ('Орел'),
    ('Мценск'),
    ('Кромы'),
    ('Дмитровск'),
    ('Ливны'),
    ('Новосиль');

insert into advertisement_type (name)
values
    ('Личные вещи'),
    ('Транспорт'),
    ('Недвижимость'),
    ('Работа'),
    ('Для дома и дачи'),
    ('Бытовая электроника'),
    ('Хобби и отдых'),
    ('Животные'),
    ('Для бизнеса');

-- Первый базовый комментарий (root)
insert into comment (comment_id, advertisement_id, commenter_id, comment_text)
values
('11111111-1111-1111-1111-111111111111', '218c00e2-01d3-40b6-99d2-1e54986e26e3', '4807b4ba-fd3c-4e48-b3b6-70a5905b37eb', 'Первый комментарий к объявлению');

-- Ответ на первый комментарий (1 уровень вложенности)
insert into comment (comment_id, advertisement_id, commenter_id, comment_text, root_comment_id, parent_comment_id)
values
('22222222-2222-2222-2222-222222222222', '218c00e2-01d3-40b6-99d2-1e54986e26e3', '4807b4ba-fd3c-4e48-b3b6-70a5905b37eb', 'Ответ на первый комментарий', '11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111');

-- Ответ на ответ (2 уровень вложенности)
insert into comment (comment_id, advertisement_id, commenter_id, comment_text, root_comment_id, parent_comment_id)
values
('33333333-3333-3333-3333-333333333333', '218c00e2-01d3-40b6-99d2-1e54986e26e3', '4807b4ba-fd3c-4e48-b3b6-70a5905b37eb', 'Ответ на ответ', '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222');

-- Второй базовый комментарий (новая ветка)
insert into comment (comment_id, advertisement_id, commenter_id, comment_text)
values
('44444444-4444-4444-4444-444444444444', '218c00e2-01d3-40b6-99d2-1e54986e26e3', '4807b4ba-fd3c-4e48-b3b6-70a5905b37eb', 'Второй корневой комментарий');

-- Ответ к новому корневому комментарию
insert into comment (comment_id, advertisement_id, commenter_id, comment_text, root_comment_id, parent_comment_id)
values
('55555555-5555-5555-5555-555555555555', '218c00e2-01d3-40b6-99d2-1e54986e26e3', '4807b4ba-fd3c-4e48-b3b6-70a5905b37eb', 'Ответ на второй корневой комментарий', '44444444-4444-4444-4444-444444444444', '44444444-4444-4444-4444-444444444444');

-- Еще один ответ на корневой комментарий
insert into comment (comment_id, advertisement_id, commenter_id, comment_text, root_comment_id, parent_comment_id)
values
('66666666-6666-6666-6666-666666666666', '218c00e2-01d3-40b6-99d2-1e54986e26e3', '4807b4ba-fd3c-4e48-b3b6-70a5905b37eb', 'Еще один ответ на второй корневой комментарий', '44444444-4444-4444-4444-444444444444', '44444444-4444-4444-4444-444444444444');

-- Ответ на ответ
insert into comment (comment_id, advertisement_id, commenter_id, comment_text, root_comment_id, parent_comment_id)
values
('77777777-7777-7777-7777-777777777777', '218c00e2-01d3-40b6-99d2-1e54986e26e3', '4807b4ba-fd3c-4e48-b3b6-70a5905b37eb', 'Ответ на ответ к второму комментарию', '44444444-4444-4444-4444-444444444444', '55555555-5555-5555-5555-555555555555');
