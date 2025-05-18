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
    username varchar(255) not null unique,
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
create materialized view if not exists user_rating_view as
    select
        user_id,
        avg(rating_value) as average_rating,
        count(*) as rating_count
    from rating
    group by user_id
with data;
create unique index if not exists index_user_rating_user_id on user_rating_view(user_id);-- заместо первичного ключа

--refresh materialized view concurrently user_rating_view; -- для обновления view

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
    is_promoted boolean not null default false, -- if the advertisement is paid or not. for showing it in top
    create_date timestamp(3) not null default now(),
    close_date timestamp(3),
    foreign key (user_id) references "user"(user_id) on delete cascade,
    foreign key (city_id) references city(city_id) on delete cascade,
    foreign key (advertisement_type_id) references advertisement_type(advertisement_type_id) on delete cascade
);
create index if not exists index_advertisement_user_id on advertisement(user_id);
--create index if not exists index_advertisement_city_id on advertisement(city_id);
create index if not exists index_advertisement_advertisement_type_id on advertisement(advertisement_type_id);

--9) История продаж пользователя.
create table if not exists sale_history (
    sale_history_id uuid primary key default gen_random_uuid(),
    advertisement_id uuid not null,
    buyer_id uuid, -- buyer_id будет null только если пользователь который купил объявление удалился (чтобы не удалялась история продаж)
    sale_date_time timestamp(3) not null default now(),
    quantity smallint not null,
    foreign key (buyer_id) references "user"(user_id) on delete set null,
    foreign key (advertisement_id) references advertisement(advertisement_id) on delete cascade
);

--5) Возможность оставлять комментарии под объявлениями.
create table if not exists comment (
    comment_id uuid primary key default gen_random_uuid(),
    advertisement_id uuid not null, -- для какого объявления коммент
    commenter_id uuid, -- при удалении пользователя комментарии остаются в базе, а пользователь null
    root_comment_id uuid, -- полезно для выбора вложенных комментариев (меньше запросов)
    parent_comment_id uuid,
    comment_text text not null,
    comment_date timestamp(3) not null default now(),
    check (comment_id != parent_comment_id and comment_id != root_comment_id),
    foreign key (commenter_id) references "user"(user_id) on delete set null,
    foreign key (advertisement_id) references advertisement(advertisement_id) on delete cascade,
    foreign key (root_comment_id) references comment(comment_id) on delete cascade,
    foreign key (parent_comment_id) references comment(comment_id) on delete cascade
);
create index if not exists index_comment_advertisement_id_comment_date on comment(advertisement_id, comment_date);
create index if not exists index_comment_root_comment_id on comment(root_comment_id);

--6) Организация личной переписки покупателя и продавца.
create table if not exists message (
    message_id uuid primary key default gen_random_uuid(),
    advertisement_id uuid,
    sender_id uuid not null,
    recipient_id uuid not null,
    message_text text not null,
    message_date_time timestamp(3) not null default now(),
    is_read boolean not null default false,
    foreign key (sender_id) references "user"(user_id) on delete cascade,
    foreign key (recipient_id) references "user"(user_id) on delete cascade,
    foreign key (advertisement_id) references advertisement(advertisement_id) on delete cascade,
    check (sender_id != recipient_id)
);
create index if not exists index_message_advertisement_id_sender_id_recipient_id on message(advertisement_id, sender_id, recipient_id);
create index if not exists index_message_message_date_time on message(message_date_time);


