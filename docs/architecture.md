# Architecture

## Java Code

* `api/` is (should be) Storeys' API, for both JS with Vert.x client + Java
* `engine/` will be an interactive dialogs runtime, useable both in Minecraft and standalone
* `storeys/` is the core module including `/narrate` and `.story` DSL
* `test-utils/` is a minor technical utility for classpath duplication detection
* `web/` contains the Vert.x server back-end for both the Scratch integration and JS

## JavaScript Code

* `scratch3/` & `scratch3-server` are the Scratch 3.0 integrations, see [detailed doc in issue #290](https://github.com/OASIS-learn-study/minecraft-storeys-maker/issues/290)

## Infrastructure

* `minecraft-server/` is used by the `Dockerfile` to create the Minecraft server container image
* `minecraft-server-test-data/` is used by the `./test` script, see [testing docs](docs/testing.md)

## Other

* `logo/` has this project's graphical identity (courtesy of teneresa@)
