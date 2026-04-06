FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY src src

RUN chmod +x gradlew
RUN ./gradlew --no-daemon clean installDist

FROM eclipse-temurin:21-jre

WORKDIR /opt/ad-analytics

COPY --from=builder /app/build/install/ad-analytics /opt/ad-analytics

ENTRYPOINT ["/opt/ad-analytics/bin/ad-analytics"]
