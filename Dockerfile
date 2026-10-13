FROM maven:3.9.16-eclipse-temurin-17 AS build

WORKDIR /build
ARG MODULE
COPY ${MODULE}/pom.xml ./pom.xml
COPY ${MODULE}/src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S -g 10001 app \
      && adduser -S -D -H -u 10001 -G app app
COPY --from=build --chown=10001:10001 /build/target/*.jar ./app.jar
USER 10001:10001
ENTRYPOINT ["java","-jar","/app/app.jar"]