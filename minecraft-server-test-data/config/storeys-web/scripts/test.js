module.exports = (async (minecraft) => {
  const registration = await minecraft.whenEntityRightClicked("Chestnut");
  registration.on().subscribe(async response => {
    await minecraft.showTitle(response.playerUUID, "Hey there, from test.js!");
  });
});
