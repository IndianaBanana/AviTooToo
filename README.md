# AviTooToo. SENLA courses final project

## Система размещения частных объявлений.

создать базу данных с СУБД PostgreSQL: avi_too_too c public схемой

В переменные окружения добавить:

```commandline
SONAR_LOGIN=ваш_токен (необязательно если не собираетесь использовать SonarQube)
DB_URL=jdbc:postgresql://localhost:5432/avi_too_too
DB_USERNAME=ваш_логин
DB_PASSWORD=ваш_пароль
```

для запуска проекта (заходим в корень проекта):

```commandline
zsh start_script.sh #для Unix систем
```
для Windows
запускаем start_script.bat


## Стек технологий

Java 17,
PostgreSQL для СУБД