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


--1) Регистрация пользователя в системе (пользователя и администратора).
--2) Редактирование профиля.
create table if not exists "user" (
    user_id uuid primary key,
    first_name varchar(255) not null,
    last_name varchar(255) not null,
    phone varchar(255) not null unique,
    username varchar(255) not null unique, -- probably email
    password varchar(255) not null,
    role varchar(255) not null
);

--8) Система рейтингов продавцов, влияющая на положение объявлений продавца в поисковой выдаче.
--   Чем ниже рейтинг, тем ниже объявление в выдаче.
create table if not exists user_rating (
    user_id uuid primary key,
    average_rating numeric not null check (average_rating >= 1 and average_rating <= 5),
    rating_count integer not null,
    foreign key (user_id) references "user"(user_id) on delete cascade
);

create table if not exists rating (
    user_id uuid,
    rater_id uuid,
    rating smallint not null check (rating >= 1 and rating <= 5),
    check (user_id != rater_id),
    foreign key (user_id) references "user"(user_id) on delete cascade,
    foreign key (rater_id) references "user"(user_id) on delete cascade,
    primary key (user_id, rater_id)
);

--3) Просмотр списка объявлений. Поиск и фильтрация.
--4) Возможность добавления / редактирования / удаления объявлений.
--7) Возможность проплатить отображение объявления в топе выдачи.
create table if not exists advertisement (
    advertisement_id uuid primary key,
    user_id uuid not null, -- the user who created the advertisement
    title varchar(255) not null,
    description text not null,
    price numeric not null,
    quantity smallint not null,
    is_paid boolean default false, -- if the advertisement is paid or not. for showing it in top
    create_date timestamp default now(),
    close_date timestamp,
    foreign key (user_id) references "user"(user_id) on delete cascade
);

--9) История продаж пользователя.
create table if not exists sale_history (
    sale_history_id uuid primary key,
    advertisement_id uuid not null,
    buyer_id uuid,
    sale_date timestamp default now(),
    quantity smallint not null,
    foreign key (buyer_id) references "user"(user_id) on delete set null,
    foreign key (advertisement_id) references advertisement(advertisement_id) on delete cascade
);

--5) Возможность оставлять комментарии под объявлениями.
create table if not exists comment (
    comment_id uuid primary key,
    advertisement_id uuid not null, -- the advertisement to which the comment is attached
    commenter_id uuid not null, -- the user who wrote the comment
    root_comment_id uuid, -- useful for selecting nested comments (less selections)
    parent_comment_id uuid, -- the comment to which the comment is attached
    comment_text text not null,
    comment_date timestamp default now(),
    check (comment_id != parent_comment_id and comment_id != root_comment_id),
    foreign key (commenter_id) references "user"(user_id) on delete set null,
    foreign key (advertisement_id) references advertisement(advertisement_id) on delete cascade,
    foreign key (root_comment_id) references comment(comment_id) on delete restrict,
    foreign key (parent_comment_id) references comment(comment_id) on delete restrict
);

--6) Организация личной переписки покупателя и продавца.
create table if not exists message (
    message_id uuid primary key,
    advertisement_id uuid not null,
    sender_id uuid not null,
    recipient_id uuid not null,
    message_text text not null,
    message_date timestamp default now(),
    is_read boolean default false,
    foreign key (sender_id) references "user"(user_id) on delete set null,
    foreign key (recipient_id) references "user"(user_id) on delete set null,
    foreign key (advertisement_id) references advertisement(advertisement_id) on delete cascade,
    check (sender_id != recipient_id)
);





