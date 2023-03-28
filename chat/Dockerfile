#FROM gradle:jdk11 AS build
#COPY --chown=gradle:gradle build.gradle settings.gradle gradlew /home/
#COPY --chown=gradle:gradle gradle /home/gradle
#COPY --chown=gradle:gradle src /home/src
#WORKDIR /home/
#RUN gradle build -x test -Pdocker --no-daemon

FROM amazoncorretto:17-alpine
EXPOSE 80
#RUN mkdir /app
#COPY --from=build /home/build/libs/*.jar /app/app.jar
#COPY build/libs/iti-rest-api*.jar /app/app.jar
ADD build/libs/chat*.jar chat.jar
#ENTRYPOINT ["java", "-jar", "-Dserver.port=80", "/app/app.jar"]
ENTRYPOINT ["java", "-jar", "-Dserver.port=8080", "chat.jar"]
