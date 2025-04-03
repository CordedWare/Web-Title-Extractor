# Stage 1 (Build)
FROM eclipse-temurin:17-jdk-jammy as builder

WORKDIR /app
COPY . .
RUN ln -s /usr/local/sbt/bin/sbt /usr/local/bin/sbt

RUN sbt compile

# Stage 2(Run)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=builder /app/target/universal/stage /app

EXPOSE 8006

CMD ["./bin/web-title-extractor-service"]