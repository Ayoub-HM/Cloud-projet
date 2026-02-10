# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY app/pom.xml app/pom.xml
RUN mvn -f app/pom.xml -q -DskipTests dependency:go-offline
COPY app app
RUN mvn -f app/pom.xml -q -DskipTests package

# runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
# create non-root user (Debian-compatible)
RUN groupadd -r app && useradd -r -g app -s /bin/false app
USER app
COPY --from=build /workspace/app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
