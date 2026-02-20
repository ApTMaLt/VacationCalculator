# ==================================================================
# Этап 1: Сборка приложения (Build Stage)
# ==================================================================
FROM gradle:8.5-jdk21-alpine AS builder

# Установка рабочей директории
WORKDIR /home/gradle/project

# Копируем файлы конфигурации Gradle для кэширования зависимостей
COPY build.gradle settings.gradle ./
# Копируем обертку Gradle (если используется wrapper)
COPY gradlew ./
COPY gradle ./gradle

# Загружаем зависимости (кешируется, если build.gradle не меняется)
RUN gradle dependencies --no-daemon || echo "Ignored failure"

# Копируем исходный код
COPY src ./src

# СобираемJAR-файл
RUN gradle bootJar --no-daemon

# ==================================================================
# Этап 2: Запуск приложения (Run Stage)
# ==================================================================
FROM eclipse-temurin:21-jre-alpine

# Установка рабочей директории
WORKDIR /app

# Создаем пользователя без прав root для безопасности
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Копируем собранный JAR из этапа сборки
# Используем wildcard, так как имя файла может содержать версию (например, 0.0.1-SNAPSHOT)
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

# Открываем порт, указанный в application.properties (8080)
EXPOSE 8080

# Параметры JVM (опционально, для настройки памяти)
ENV JAVA_OPTS="-Xms512m -Xmx512m"

# Команда запуска
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
