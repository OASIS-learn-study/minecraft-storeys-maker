Third scripting option
======================

When using this plugin you can either write a story as a text file or create something similar using scratch,
but there is a 3rd option you can also use javascript to create them.

In the config directory you will find a folder called "storyes-web/scripts" (after you started the server once)
you can put a javascript file in this folder that looks like this:

```javascript
module.exports = (async (minecraft) => {
  const registration = await minecraft.whenEntityRightClicked("joe");
  registration.on().subscribe(async response => {
    await minecraft.showTitle(response.playerUUID, "hello my little friend");
  });
});
```

This will register a listener for when an entity named "joe" gets right clicked and let him say: "hello my little friend".
The [minecraft object](api/src/main/typescript/observable-wrapper.ts) has all the functions that the scratch has, this is what scratch uses as well.
These scripts get started run when the server starts.