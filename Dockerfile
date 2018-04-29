FROM s2i-minecraft-server

# NB: The .dockerignore file excludes a lot to make "Uploading context" faster.
# This helps for Docker but not OpenShift; see https://github.com/openshift/origin/issues/13255.

COPY web/build/libs/web-*-all.jar /deployments/mods/


# NB: The EXPOSE is really just for "UI convenience", not technically required...

# Minecraft server
EXPOSE 25565

# Vert.x EventBus
EXPOSE 8080

# Static HTTP web server for JS
EXPOSE 7070
