# Этап 1: Сборка приложения
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Копируем pom.xml и загружаем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем приложение
COPY src ./src
RUN mvn clean package -DskipTests

# Этап 2: Запуск приложения
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Создаем директории для загрузки файлов
RUN mkdir -p /app/uploads /app/temp-uploads

# Копируем собранный jar из предыдущего этапа
COPY --from=build /app/target/*.jar app.jar

# Открываем порт приложения
EXPOSE 8080

# Запускаем приложение с профилем docker
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "/app/app.jar"]

