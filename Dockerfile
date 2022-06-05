FROM itzg/minecraft-server:java8-multiarch

# RUN mkdir /mods/ && cd /mods/ && curl -sS -L -J -f -O https://github.com/OASIS-learn-study/swissarmyknife-minecraft-server-binaries/raw/master/LuckPerms-Sponge-5.3.98.jar

COPY web/build/libs/*-all.jar /mods/
COPY minecraft-server/* /data-init/

ENV EULA=TRUE
ENV TYPE=SPONGEVANILLA
ENV OVERRIDE_SERVER_PROPERTIES=true
ENV ENABLE_RCON=false
ENV MOTD="\u00A7a\u00A7l/make\u00A7r your \u00A7bown\u00A7r Plugins/Mods, \u00A7l\u00A76with Scratch\!"
EXPOSE 25565 25575 7070 8080
ENTRYPOINT [ "/data-init/start-custom" ]
HEALTHCHECK --start-period=1m --timeout=3s --interval=17s --retries=1 CMD mc-health

# TODO When https://github.com/itzg/docker-minecraft-server/issues/1449,
#      then "ENV ICON=/data-init/server-icon.png", but until then that's in the start-custom
