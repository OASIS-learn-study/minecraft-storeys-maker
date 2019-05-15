module.exports = (async (minecraft) => {
  const registration = await minecraft.whenEntityRightClicked("joe");
  registration.on().subscribe(async response => {
    await minecraft.showTitle(response.playerUUID, "hello my little friend");
  });
});