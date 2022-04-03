# Story Syntax (DSL)

Write your own .story file, e.g. see [test.story](../minecraft-server-test-data/config/storeys-web/stories/test.story).

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
