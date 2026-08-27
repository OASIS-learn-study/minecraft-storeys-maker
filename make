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

if command -v java >/dev/null 2>&1 && command -v node >/dev/null 2>&1 && command -v npm >/dev/null 2>&1; then
  ./gradlew --no-daemon build -PexcludeTests="**/SeleniumTest*"
else
  # Fallback for environments without a local Java/Node toolchain.
  # --network host avoids intermittent TLS download corruption observed with rootless Podman networking.
  BUILD_MAX_RETRIES=3
  BUILD_ATTEMPT=1
  until [ $BUILD_ATTEMPT -gt $BUILD_MAX_RETRIES ]; do
    if $CONTAINER run --network host --rm -v $PWD:/project$VOL_SUFFIX minecraft-storeys-maker-build \
        ./gradlew --no-daemon build -PexcludeTests="**/SeleniumTest*"; then
      break
    fi
    if [ $BUILD_ATTEMPT -eq $BUILD_MAX_RETRIES ]; then
      echo "Gradle build failed after $BUILD_MAX_RETRIES attempts."
      exit 1
    fi
    echo "Gradle build attempt $BUILD_ATTEMPT failed; retrying..."
    BUILD_ATTEMPT=$((BUILD_ATTEMPT + 1))
  done
fi

# NB --format=docker because the "oci" default looses the mc-health HEALTHCHECK of the parent image
if command -v podman >/dev/null 2>&1; then
  podman build -f Dockerfile --format=docker -t minecraft-storeys-maker .
else
  docker build -f Dockerfile -t minecraft-storeys-maker .
fi
