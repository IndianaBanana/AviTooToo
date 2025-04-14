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
create table "user" (
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
create table user_rating (
    user_rating_id uuid primary key,
    user_id uuid not null,
    average_rating numeric not null check (average_rating >= 1 and average_rating <= 5),
    rating_count integer not null,
    foreign key (user_id) references "user"(user_id) on delete cascade
)

create table rating (
    rating_id uuid primary key,
    seller_id uuid not null, -- the seller who received the rating
    user_id uuid not null,-- the user who rated the seller
    rating integer not null check (rating >= 1 and rating <= 5),
    check (user_id != seller_id),
    foreign key (user_id) references "user"(user_id) on delete cascade,
    foreign key (seller_id) references "user"(user_id) on delete cascade,
    unique (user_id, seller_id)
);

--3) Просмотр списка объявлений. Поиск и фильтрация.
--4) Возможность добавления / редактирования / удаления объявлений.
--7) Возможность проплатить отображение объявления в топе выдачи.
create table advertisement (
    advertisement_id uuid primary key,
    title varchar(255) not null,
    description text not null,
    price numeric not null,
    is_paid boolean not null, -- if the advertisement is paid or not. for showing it in top
    user_id uuid not null, -- the user who created the advertisement
    creation_date timestamp not null,
    close_date timestamp,
    foreign key (user_id) references "user"(user_id) on delete cascade
);

--9) История продаж пользователя.
create table sale_history (
    sale_history_id uuid primary key,
    user_id uuid not null,
    title_of_sold_advertisement varchar(255) not null,
    description_of_sold_advertisement varchar(255) not null,
    price_of_sold_advertisement numeric not null,
    sale_date timestamp not null,
    quantity integer not null,
    foreign key (user_id) references "user"(user_id) on delete cascade
);

--5) Возможность оставлять комментарии под объявлениями.
create table comment (
    comment_id uuid primary key,
    user_id uuid not null, -- the user who wrote the comment
    advertisement_id uuid not null, -- the advertisement to which the comment is attached
    root_comment_id uuid, -- useful for selecting nested comments (less selections)
    parent_comment_id uuid, -- the comment to which the comment is attached
    comment_text text not null,
    check (comment_id != parent_comment_id 
	    and parent_comment_id!=root_comment_id 
	    and comment_id != root_comment_id
	    ),
    foreign key (user_id) references "user"(user_id) on delete set null,
    foreign key (advertisement_id) references advertisement(advertisement_id) on delete cascade,
    foreign key (root_comment_id) references comment(comment_id) on delete restrict,
    foreign key (parent_comment_id) references comment(comment_id) on delete restrict
);

--6) Организация личной переписки покупателя и продавца.
create table message (
    message_id uuid primary key,
    sender_id uuid not null,
    recipient_id uuid not null,
    advertisement_id uuid not null,
    message_text text not null,
    message_date timestamp not null,
    is_read boolean default false,
    foreign key (sender_id) references "user"(user_id) on delete set null,
    foreign key (recipient_id) references "user"(user_id) on delete set null,
    foreign key (advertisement_id) references advertisement(advertisement_id) on delete cascade,
    check (sender_id != recipient_id)
);





