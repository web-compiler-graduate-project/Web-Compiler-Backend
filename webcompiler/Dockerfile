FROM gradle:8.9-jdk21 AS build

WORKDIR /home/gradle/project

COPY . .

RUN chmod -R 755 /home/gradle/project

RUN gradle build -x test

FROM openjdk:22-jdk-slim

RUN apt-get update && apt-get install -y gcc g++ && apt-get clean

COPY --from=build /home/gradle/project/build/libs/*.jar /app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

CMD ["java", "-jar", "/app.jar"]
