# ---------- build ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY app/pom.xml app/pom.xml
RUN mvn -f app/pom.xml -q -DskipTests dependency:go-offline
COPY app app
RUN mvn -f app/pom.xml -q -DskipTests package

# ---------- runtime ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# non-root user
RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /workspace/app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
