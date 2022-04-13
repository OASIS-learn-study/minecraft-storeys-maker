FROM openjdk:11-jdk as build

# https://github.com/nodesource/distributions/blob/master/README.md#installation-instructions
RUN curl -fsSL https://deb.nodesource.com/setup_17.x | bash - && apt install -y nodejs && node --version

COPY . /project
WORKDIR /project
RUN ./gradlew build -PexcludeTests="**/SeleniumTest*"

# Keep any changes made here strictly in sync with Dockerfile-local
FROM itzg/minecraft-server:java8-multiarch
COPY --from=build /project/web/build/libs/*-all.jar /mods/
COPY minecraft-server/* /data-init/

ENV EULA=TRUE
ENV TYPE=SPONGEVANILLA
ENV OVERRIDE_SERVER_PROPERTIES=true
ENV ENABLE_RCON=false
ENV MOTD="\u00A7a\u00A7l/make\u00A7r your \u00A7bown\u00A7r Plugins/Mods, \u00A7l\u00A76with Scratch\!"
EXPOSE 25565 25575 7070 8080
ENTRYPOINT [ "/data-init/start-custom" ]

# TODO When https://github.com/itzg/docker-minecraft-server/issues/1449,
#      then "ENV ICON=/data-init/server-icon.png", but until then that's in the start-custom
