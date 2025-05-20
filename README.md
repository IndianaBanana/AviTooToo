# AviTooToo. SENLA courses final project

## Система размещения частных объявлений.

Нужно скачать и установить docker https://www.docker.com/products/docker-desktop/

для деплоя:
1. открыть терминал в корне проекта
2. ввести ```docker-compose up --build```

## Технологический стек и образы Docker

- **Java SE 17**
    - Образ сборки: `maven:3.8.3-openjdk-17`
    - Образ рантайма: `openjdk:17-jdk-slim`

- **PostgreSQL 17**
    - Образ БД: `postgres:latest`