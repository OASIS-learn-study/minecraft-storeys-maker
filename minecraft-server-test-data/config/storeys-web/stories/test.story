// Comment

= Once upon a time...
== There was a Minecraft plugin to test.

/tp 232 63 216 -180 25

The pig should be talking next...

@Piggy Hi there! I'm Piggy.
If I'm speaking to you, /narrate works!

Depending on whether there is a fishing rod in the inventory, there will now be a different message shown...

// Indentation with 2 spaces allows to inline JS!
// TODO https://github.com/OASIS-learn-study/minecraft-storeys-maker/issues/301 - this is currently broken (but js.story is OK)

  if (!player.inventory.contains(ItemTypes.FISHING_ROD)) {
    return 'There may be a fishing rod hidden somewhere… look for it, and then catch a fish!'
  } else {
    return 'Go fishing with the rod in your inventory..'
  }

%await 1s

Tests have concluded. Hope everything worked?

// TODO %in -311.300 76 11.628 -323.571 70.5 16.516
