FROM openjdk:8-jdk as build

RUN apt-get update
COPY . /project
WORKDIR /project
RUN ./gradlew build -PexcludeTests="**/SeleniumTest*"

FROM itzg/minecraft-server:java8
COPY --from=build /project/web/build/libs/*-all.jar /mods/

ENV EULA=TRUE
ENV TYPE=SPONGEVANILLA
EXPOSE 25565 25575 7070 8080
ENTRYPOINT [ "/start" ]
