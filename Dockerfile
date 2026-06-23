FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=prod

COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/uploads

EXPOSE 8012

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
