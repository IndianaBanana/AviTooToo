# AviTooToo. SENLA courses final project

## Система размещения частных объявлений.

Нужно скачать и установить docker https://www.docker.com/products/docker-desktop/

для деплоя:

1. открыть терминал в корне проекта
2. ввести ```docker-compose up --build```

### Технологический стек и образы Docker

- **Java SE 17**
    - Образ сборки: `maven:3.8.3-openjdk-17`
    - Образ рантайма: `openjdk:17-jdk-slim`

- **PostgreSQL 17**
    - Образ БД: `postgres:latest`
- **PgAdmin 4**

## Без Docker

создать базу данных с СУБД PostgreSQL: avi_too_too c public схемой

В переменные окружения добавить:

```
DB_URL=jdbc:postgresql://localhost:5432/avi_too_too
DB_USERNAME=ваш_логин
DB_PASSWORD=ваш_пароль
```

для запуска проекта (заходим в корень проекта):

```commandline
zsh start_script.sh #для Unix систем
```

для Windows
запускаем ```start_script.bat```

### Технологический стек

- **Java SE 17**
- **PostgreSQL 17**
