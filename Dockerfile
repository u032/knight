FROM gradle:7.4.2-jdk17-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar --no-daemon

FROM openjdk:17-alpine
EXPOSE 8080:8080
COPY --from=build /home/gradle/src/.env.prod /.env
COPY --from=build /home/gradle/src/firestore.json /firestore.json
COPY --from=build /home/gradle/src/build/libs/knight.jar /knight.jar
ENTRYPOINT ["java", "-jar", "/knight.jar"]
