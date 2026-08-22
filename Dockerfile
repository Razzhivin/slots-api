# Шаг 1: Сборка приложения с помощью Maven и Eclipse Temurin JDK 17
FROM mirror.gcr.io/library/maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Шаг 2: Легковесный запуск на Eclipse Temurin JRE 17
FROM mirror.gcr.io/library/eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/slots-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]