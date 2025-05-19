#!/bin/bash

chmod +x ./mvnw

./mvnw clean package

java -jar target/AviTooToo-0.0.1-SNAPSHOT.jar

