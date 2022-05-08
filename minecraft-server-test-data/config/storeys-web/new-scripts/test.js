e.whenCommand("new", function callback(m) {
   m.title("Hello");
   if (!m.player().inventory.contains(ItemTypes.FISHING_ROD)) {
      m.cmd("/say There may be a fishing rod hidden somewhere… look for it, and then catch a fish!");
   } else {
      m.cmd("/say Go fishing with the rod in your inventory..");
   }
});
