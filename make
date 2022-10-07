#!/usr/bin/env bash
set -euox pipefail

# NB: "docker build -v $PWD:/project:Z" works in Podman, but not in Docker (at least not in the version available on GCB)
docker build -f Dockerfile-build -t minecraft-storeys-maker-build .
docker run --rm -v $PWD:/project:Z minecraft-storeys-maker-build ./gradlew --no-daemon build -PexcludeTests="**/SeleniumTest*"

# NB --format=docker because the "oci" default looses the mc-health HEALTHCHECK of the parent image
if [ $(command -v podman) ]; then
  podman build -f Dockerfile --format=docker -t minecraft-storeys-maker .
else
  docker build -f Dockerfile -t minecraft-storeys-maker .
fi
