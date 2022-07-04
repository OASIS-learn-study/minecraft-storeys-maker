declare const e: Events;

declare class Minecraft {
  cmd(command: string): void;

  title(text: string): void;

  narrate(entity: string, text: string): void;

  player(): any;
}

declare class Events {
  whenCommand(command: string, callback: (m: Minecraft) => void): void;

  whenEntityRightClicked(entity: string, callback: (m: Minecraft) => void): void;

  whenPlayerJoins(callback: (m: Minecraft) => void): void;

}