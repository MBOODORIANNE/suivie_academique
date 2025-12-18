# Étape 1 : Build
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY . .
RUN chmod +x mvnw
RUN rm -rf ~/.m2/repository/org/springframework/boot/spring-boot-autoconfigure


RUN ./mvnw clean package -U -DskipTests


# Étape 2 : Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
