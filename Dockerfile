FROM gradle:7.4.2-jdk17-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar --no-daemon

FROM ghcr.io/graalvm/jdk:java17-22.1.0
EXPOSE 8080:8080
COPY --from=build /home/gradle/src/.env.docker /.env
COPY --from=build /home/gradle/src/firebase.json /firebase.json
COPY --from=build /home/gradle/src/build/libs/knight.jar /knight.jar
ENTRYPOINT ["java", "-jar", "/knight.jar"]
