FROM gradle:jdk21-alpine AS build
WORKDIR /auth
COPY ./build.gradle.kts ./
COPY ./settings.gradle.kts ./
RUN gradle build --dry-run || return 0
COPY . .
RUN gradle build

FROM alpine/java:21-jre AS final
WORKDIR /auth
COPY --from=build /auth/build/libs/*-SNAPSHOT.jar /auth/app.jar
EXPOSE 1812
ENTRYPOINT ["java","-jar","app.jar"]