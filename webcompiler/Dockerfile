# Użyj obrazu z JDK do budowania aplikacji
FROM gradle:8.9-jdk21 AS build

# Ustaw katalog roboczy
WORKDIR /home/gradle/project

# Skopiuj pliki projektu
COPY . .

# Upewnij się, że wszystkie pliki mają odpowiednie uprawnienia
RUN chmod -R 755 /home/gradle/project

# Zbuduj aplikację
RUN gradle build -x test

# Użyj obrazu z JRE do uruchomienia aplikacji
FROM openjdk:22-jdk-slim

# Zainstaluj gcc i g++
RUN apt-get update && apt-get install -y gcc g++ && apt-get clean

# Skopiuj zbudowany plik JAR z poprzedniego etapu
COPY --from=build /home/gradle/project/build/libs/*.jar /app.jar

# Expose the port the app runs on
EXPOSE 8080

# Ustaw zmienną środowiskową do uruchomienia aplikacji
ENV JAVA_OPTS=""

# CMD puste - nie uruchamia aplikacji, tylko buduje obraz
CMD ["java", "-jar", "/app.jar"]
