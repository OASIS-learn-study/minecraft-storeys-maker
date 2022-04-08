= JavaScript

// This is a chat message:
Depending on whether there is a fishing rod in the inventory, there will now be a different message shown...

// Indentation with 2 spaces allows to inline JS:

  if (!player.inventory.contains(ItemTypes.FISHING_ROD)) {
    // Trailing \n is required because of https://github.com/OASIS-learn-study/minecraft-storeys-maker/issues/333
    return 'There may be a fishing rod hidden somewhere… look for it, and then catch a fish!\n'
  } else {
    return 'Go fishing with the rod in your inventory..\n'
  }

La Fin. Tests have concluded. Hope everything worked?
