# ---------- STAGE 1: BUILD ----------
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# ---------- STAGE 2: RUNTIME ----------
FROM eclipse-temurin:21-jre  # ⚠️ Dùng Debian, không dùng Alpine

RUN addgroup --system app && adduser --system --ingroup app app
USER app
WORKDIR /app

COPY --from=builder /build/target/*.jar /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0" \
    SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
