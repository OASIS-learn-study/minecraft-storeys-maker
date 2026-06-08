#!/usr/bin/env bash
set -euox pipefail

# NB: "docker build -v $PWD:/project:Z" works in Podman, but not in Docker (at least not in the version available on GCB)
if command -v podman >/dev/null 2>&1; then
  CONTAINER=podman
  VOL_SUFFIX=:Z
else
  CONTAINER=docker
  VOL_SUFFIX=
fi
$CONTAINER build -f Dockerfile-build -t minecraft-storeys-maker-build .
$CONTAINER run --rm -v $PWD:/project$VOL_SUFFIX minecraft-storeys-maker-build ./gradlew --no-daemon build -PexcludeTests="**/SeleniumTest*"

# NB --format=docker because the "oci" default looses the mc-health HEALTHCHECK of the parent image
if command -v podman >/dev/null 2>&1; then
  podman build -f Dockerfile --format=docker -t minecraft-storeys-maker .
else
  docker build -f Dockerfile -t minecraft-storeys-maker .
fi
