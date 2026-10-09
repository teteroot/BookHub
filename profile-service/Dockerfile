FROM gradle:jdk21-alpine AS build
WORKDIR /profile
COPY ./build.gradle.kts ./
COPY ./settings.gradle.kts ./
RUN gradle build --dry-run || return 0
COPY . .
RUN gradle build -x test

FROM alpine/java:21-jre AS final
WORKDIR /profile
COPY --from=build /profile/build/libs/*-SNAPSHOT.jar /profile/app.jar
EXPOSE 8001
ENTRYPOINT ["java","-jar","app.jar"]