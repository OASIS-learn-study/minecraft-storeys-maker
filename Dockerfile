FROM openjdk:8-jdk as build

COPY . /project
WORKDIR /project
RUN ./gradlew build -x test

# Keep any changes made here strictly in sync with Dockerfile-local
FROM itzg/minecraft-server:java8
COPY --from=build /project/web/build/libs/*-all.jar /mods/

ENV EULA=TRUE
ENV TYPE=SPONGEVANILLA
EXPOSE 25565 25575 7070 8080
ENTRYPOINT [ "/start" ]
