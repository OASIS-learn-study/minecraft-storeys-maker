FROM openjdk:8-jdk as build

COPY . /project
WORKDIR /project
RUN ./gradlew build -x test

# Keep any changes made here strictly in sync with Dockerfile-local
FROM itzg/minecraft-server:java8
COPY --from=build /project/web/build/libs/*-all.jar /mods/
COPY minecraft-server/* /data-init/

ENV EULA=TRUE
ENV TYPE=SPONGEVANILLA
ENV SKIP_SERVER_PROPERTIES=true
EXPOSE 25565 25575 7070 8080
ENTRYPOINT [ "/data-init/start-custom" ]

# TODO When https://github.com/itzg/docker-minecraft-server/issues/1449,
#      then "ENV ICON=/data-init/server-icon.png", but until then that's in the start-custom
