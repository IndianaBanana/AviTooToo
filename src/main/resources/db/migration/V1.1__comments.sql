--для точности понимания на диаграммах
comment on materialized view user_rating_view is 'materialized view';
comment on column user_rating_view.user_id is 'grouped by. has index';
comment on column user_rating_view.average_rating is 'avg(rating.rating_value)';
comment on column user_rating_view.rating_count is 'count(rating.*)';