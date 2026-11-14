FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY config/ config/
COPY src/ src/
RUN sh ./mvnw -B -DskipTests package && \
    cp target/ms-usuarios-*.jar /build/app.jar

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd --gid 10001 app && useradd --uid 10001 --gid app --no-create-home app
WORKDIR /app
COPY --from=build /build/app.jar app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=5s --timeout=3s --start-period=30s --retries=12 \
    CMD ["curl", "-fsS", "http://localhost:8080/actuator/health"]
CMD ["java", "-jar", "app.jar"]