# Interactive Story / Game Engine

## How to change code

[Use the Web IDE GitPod.io to edit and run this project](https://gitpod.io#https://github.com/vorburger/minecraft-storeys-maker/tree/master/engine)!  [![Gitpod.io](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io#https://github.com/vorburger/minecraft-storeys-maker/tree/master/engine) 

## How to run it (commandline)

    cd engine

    ../gradlew :engine:build

    java -jar build/libs/engine-1.0.0-SNAPSHOT.jar

## Server version

    java -jar build/libs/engine-1.0.0-SNAPSHOT.jar server

    Then open 'prompt-client.html' in a browser of your choice

PS: `../gradlew run` doesn't really work
(even if you add `run { standardInput = System.in }`),
because we need a real full terminal, without Gradle's continuous build prompt.
