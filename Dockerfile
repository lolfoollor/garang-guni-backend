FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src
COPY config ./config
COPY lombok.config ./
RUN chmod 774 ./mvnw && ./mvnw clean install -DskipTests
CMD ["./mvnw", "spring-boot:run"]
