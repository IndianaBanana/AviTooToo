# AviTooToo. SENLA courses final project

## Система размещения частных объявлений.

создать базу данных с СУБД PostgreSQL: avi_too_too c public схемой

В переменные окружения добавить:

```commandline
export SONAR_LOGIN=ваш_токен (необязательно если не собираетесь использовать SonarQube)
export DB_URL=jdbc:postgresql://localhost:5432/avi_too_too
export DB_USERNAME=ваш_логин
export DB_PASSWORD=ваш_пароль
```

для запуска проекта (заходим в корень проекта):

```commandline
zsh start_script.sh #для Unix систем


cmd start_script.bat #для Windows систем
```

## Стек технологий

Java 17,
PostgreSQL для СУБД