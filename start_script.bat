@echo off

call mvnw.cmd clean package
java -jar target\AviTooToo-0.0.1-SNAPSHOT.jar

