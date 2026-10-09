FROM gradle:jdk21-alpine AS build
WORKDIR /book
COPY ./build.gradle.kts ./
COPY ./settings.gradle.kts ./
RUN gradle build --dry-run || return 0
COPY . .
RUN gradle build -x test

FROM alpine/java:21-jre AS final
WORKDIR /book
COPY --from=build /book/build/libs/*-SNAPSHOT.jar /book/app.jar
EXPOSE 8002
ENTRYPOINT ["java","-jar","app.jar"]