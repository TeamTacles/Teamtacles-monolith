FROM ubuntu:latest as build

RUN apt-get update
RUN apt-get install openjdk-21-jdk -y

COPY . /app
WORKDIR /app/teamtacles-api

RUN apt-get install maven -y
RUN mvn clean install

FROM openjdk:21-jdk-slim

EXPOSE 8080

COPY --from=build /app/teamtacles-api/target/teamtacles-api-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT [ "java", "-jar", "app.jar" ]
