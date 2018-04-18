# minecraft-storeys-maker [![Build Status](https://travis-ci.org/vorburger/minecraft-storeys-maker.svg?branch=master)](https://travis-ci.org/vorburger/minecraft-storeys-maker)

Minecraft extension to make your own stories in, with and for Minecraft - it's like being a movie director!

<a href="http://www.youtube.com/watch?feature=player_embedded&v=ZHHUB7R0gEo
" target="_blank"><img src="http://img.youtube.com/vi/ZHHUB7R0gEo/0.jpg"
alt="Minecraft Stories Maker" width="480" height="360" border="10" /></a>

Please do Star & Watch this GitHub project if you like it!

## Scratch integration

This project has two parts, delivered as separate Minecraft plugins: 
* The first, described below, lets you create stories by writing .story text files.
* The second, [described in a separate README](/scratch/README.md), lets you script Minecraft with Scratch!

## Get it

[Download the latest storeys-master-all.jar file from Bintray](https://bintray.com/vorburger/minecraft/minecraft-storeys-maker#files).

Place it into your [spongepowered.org](https://www.spongepowered.org) Minecraft, typically the `mods/` directory of a Vanilla server.

Tested on Sponge Vanilla 7.0.0 (Minecraft 1.12.1) and Sponge Vanilla 5.1.0 (Minecraft 1.10.2).  Likely works on Sponge Forge as well.


## Use it

Write your own .story file, e.g. see [hello.story](storeys/src/main/resources/hello.story).

Run it with `/story <story-name>` (without .story suffix, so e.g. `/story hello`)

## Story syntax

    // Comment

    = Title
    == Subtitle

    /tp 0 0 0

    %await 2s

    /tp -235 64 230 17 12

    This is something which will appear on the chat.

    @entityName this is something that the entity will "narrate" (in its name tag, like a speech bubble in a cartoon)
    you can use more than one line; everything until the next paragraph break (double new line) will be part of the narration.
    You do not have to break up the text yourself - it will automatically be "chopped up" appropriately by itself.

    Now this will appear on the chat again (unless there is a '@' character in front again).

You can of course use ANY Minecraft command in any line that starts with the '/' character, not just /tp.

An entity's name must be given to your actors with a name tag (created via an Anvil), as always in normal Minecraft.

The `%await` action is is useful e.g. if you are teleporting your viewer around to show him a scenery,
and need the story to "pause" (to appreciate the beauty of your creation).  You do not need to explicitly use this
otherwise, as the story automatically pauses appropriately when narrating, showing titles and chat.

You can obviously mix the order and repeat titles, comments, chats, narrations, commands and actions.

## Commands

* `/story <story-name>` plays a story, read from `config/storeys/stories/<story-name>.story`
* `/narrate <entityName> Text..` makes an entity "narrate" the _Text_

## Build it

    ./gradlew build

In Docker:

    docker build -t minecraft-storeys-maker .

    docker run --rm -p 25565:25565 -p 8080:8080 -p 7070:7070 minecraft-storeys-maker

In OpenShift, just ignore the "error: build error: No source files were specified" from the first command:

    oc new-app https://github.com/vorburger/minecraft-storeys-maker.git

    oc start-build minecraft-storeys-maker --from-dir=. --follow

    oc get pods | grep -v build

    oc port-forward minecraft-storeys-maker-.... 25565:25565
    oc port-forward minecraft-storeys-maker-.... 7070:7070
    oc port-forward minecraft-storeys-maker-.... 8080:8080

You'll need to have [the "s2i-minecraft-server" base image](https://github.com/vorburger/s2i-minecraft-server) available.


## FAQ

**Seriously, "storeys" (not _"stories"_) Maker, are you mental?** Yeah.. just to avoid any possible confusion with Minecraft Story Mode! ;-)

**License? Contributions?** Licensed under the [GNU Affero General Public License v3.0 (AGPLv3)](LICENSE).  Contributions most welcome.
