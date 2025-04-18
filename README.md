# AviTooToo. SENLA courses final project

## Система размещения частных объявлений.

1) Регистрация пользователя в системе (пользователя и администратора).
2) Редактирование профиля.
3) Просмотр списка объявлений. Поиск и фильтрация.
4) Возможность добавления / редактирования / удаления объявлений.
5) Возможность оставлять комментарии под объявлениями.
6) Организация личной переписки покупателя и продавца.
7) Возможность проплатить отображение объявления в топе выдачи.
8) Система рейтингов продавцов, влияющая на положение объявлений продавца в поисковой выдаче.
   Чем ниже рейтинг, тем ниже объявление в выдаче.
9) История продаж пользователя.

### Общие требования к системе:

#### Обязательные:

1. Соответствие всем каноническим принципам написания Java-программ и Java naming convention.
2. Соответствие принципам MVC.
3. Соответствие принципам «сильной связности» и «слабой связанности».
4. Продуманная модульная структура (структура пакетов).
5. Обоснованное использование нескольких шаблонов проектирования.
6. Наличие диаграммы базы данных.
7. Приведение БД, с которой работает система, к 3 нормальной форме.
8. Наличие скрипта инициализации БД (SQL, запускаемый через скрипт инициализации системы или вручную, или миграции,
   реализованные в Liquibase или Flyway).
9. Наличие иерархии пользователей и ролей (например, обычный пользователь и администратор).
10. Наличие unit-тестов, покрывающих слои сервисов и контроллеров и написанных на JUnit 5 и Mockito.
11. Наличие системы обработки исключительных ситуаций и валидации входных данных (ControllerAdvice и аннотации Hibernate
    Validation).
12. Наличие многоуровневой системы логирования (info-лог при каждом успешном запросе или бизнес-операции и error при
    ошибке при обработке запроса).
13. Наличие скриптов для автоматической сборки и развертывания приложения (собирать систему скриптом .bash или .sh в
    готовый к использованию war- или jar-файл или использовать Docker/Docker-compose).
14. Наличие подробной и пошаговой документации по установке и развертыванию приложения (файл readme.md).

#### Дополнительные:

1. Задание выполнено в запланированные сроки: в 6 недель для Java Base, в 2 недели для Java Intensive.
2. Наличие документации эндпоинтов (Swagger или Open API).
3. Отображение объектов DTO на Entity и обратно реализовано с помощью библиотеки MapStruct.
4. Наличие интеграционных тестов эндпоинтов с реальными запросами в тестовую БД с тестовыми данными (скрипты
   инициализации тестовой БД в h2 тестовыми данными, Testcontainers, сравнение результатов запросов с образцами JSON).

### Технические требования к заданию:

1. Использование Java версии 17.
2. Использование Spring или Spring Boot для конфигурирования и реализации IoC.
3. Использование JPA/Hibernate для работы с БД (Spring Data JPA допускается).
4. Использование Maven или Gradle для сборки проекта.
5. Использование PostgreSQL как СУБД.
6. Использование JUnit 5 и Mockito для написания модульных тестов.
7. Использование Spring Security для авторизации пользователей.
