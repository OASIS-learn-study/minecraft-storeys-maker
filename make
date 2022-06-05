#!/usr/bin/env bash
set -euox pipefail

podman build -f Dockerfile-build -t minecraft-storeys-maker-build .
podman run --rm -v $PWD:/project:Z minecraft-storeys-maker-build ./gradlew --no-daemon build -PexcludeTests="**/SeleniumTest*"

# NB --format=docker because the "oci" default looses the mc-health HEALTHCHECK of the parent image
podman build -f Dockerfile --format=docker -t minecraft-storeys-maker .
