# ---------- Stage 1: Build ----------
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
COPY .mvn ./.mvn
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN mkdir -p /app/uploads

COPY --from=builder /build/target/*.jar /app/app.jar

ENV UPLOAD_DIR=/app/uploads
ENV JAVA_OPTS=""

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
