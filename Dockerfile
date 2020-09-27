FROM openjdk:8-jdk-alpine

ARG JAR_FILE=target/scheduler-bot.jar
ARG JAR_LIB_FILE=target/lib/

WORKDIR bot/

COPY ${JAR_FILE} app.jar

ADD ${JAR_LIB_FILE} lib/

ENTRYPOINT [ "java", "-jar", "app.jar"]