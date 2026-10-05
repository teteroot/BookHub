FROM gradle:jdk21-alpine AS build
WORKDIR /gateway
COPY ./build.gradle.kts ./
COPY ./settings.gradle.kts ./
RUN gradle build --dry-run || return 0
COPY . .
RUN gradle build

FROM alpine/java:21-jre AS final
WORKDIR /gateway
COPY --from=build /gateway/build/libs/*-SNAPSHOT.jar /gateway/app.jar
EXPOSE 8888
ENTRYPOINT ["java","-jar","app.jar"]