declare const e: Events;

declare class Minecraft {
  cmd(command: string): void;

  title(text: string): void;

  narrate(entity: string, text: string): void;

  addRemoveItem(count: number, itemType: any);

  player(): any;
}

type EventType = "playerJoined";

type MinecraftCallback = (m: Minecraft) => void;

declare class Events {
  whenCommand(command: string, callback: MinecraftCallback): void;

  whenEntityRightClicked(
    entity: string,
    callback: (m: Minecraft) => void
  ): void;

  whenPlayerJoins(callback: MinecraftCallback): void;

  whenEvent(eventType: EventType, callback: MinecraftCallback): void;

  whenInside(name: string, callback: MinecraftCallback): void;
}
