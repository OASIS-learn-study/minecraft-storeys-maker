FROM s2i-minecraft-server

# NB: The .dockerignore file excludes a lot to make "Uploading context" faster.

COPY web/build/libs/web-*-all.jar /deployments/mods/

# Minecraft server
EXPOSE 25565

# Vert.x EventBus
EXPOSE 8080

# Static HTTP web server for JS
EXPOSE 9090
