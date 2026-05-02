# -------- Build stage --------
# BEFORE: FROM maven:3.9-eclipse-temurin-21
FROM public.ecr.aws/docker/library/maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q    # cache deps layer separately — faster rebuilds

COPY src ./src

RUN mvn package -DskipTests -q

# -------- Runtime stage --------
# BEFORE: FROM eclipse-temurin:21-jdk-alpine
FROM public.ecr.aws/docker/library/eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]