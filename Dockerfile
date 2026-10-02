FROM maven:3.9.16-eclipse-temurin-17 AS build

WORKDIR /build
ARG MODULE
COPY ${MODULE}/pom.xml ./pom.xml
COPY ${MODULE}/src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/*.jar ./app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]