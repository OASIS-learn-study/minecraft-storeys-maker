import { Subject } from 'rxjs';
import * as EventBus from '@vertx/eventbus-bridge-client.js';
import fetch from 'node-fetch';

declare function require(moduleNames: string): any;

export class MinecraftProvider {

  constructor(private eventBusURL: string, private code: string) {
  }

  connect(): Promise<Minecraft> {
    const fetchFunction = typeof window === 'undefined' ? fetch : window.fetch;
    return fetchFunction(this.eventBusURL + '/login/' + encodeURI(this.code))
      .then(response => response.text())
      .then(token => new Minecraft(this.eventBusURL, token));
  }
}

export class Minecraft {
  private eb: any;
  static address = "ch.vorburger.minecraft.storeys";
  private callbacks: Map<string, Subject<any>> = new Map();

  private _onConnect: Promise<void>;

  constructor(eventBusURL: any, private token: string) {
    this.eb = new EventBus(eventBusURL + '/eventbus?token=' + encodeURI(token));
    this.eb.enableReconnect(true);

    this._onConnect = new Promise<void>(resolve => {
      this.eb.onopen = () => {
        resolve()
      }
    });

    this._onConnect.then(() => {
      this.eb.registerHandler("mcs.events", (error, message) => {
        if (error == null) {
          const callback = this.callbacks.get(message.body.event);
          if (callback) {
            callback.next(message.body);
          }
        } else {
          console.log("Vert.x Event Bus received error:", error);
        }
      });
    });
  }

  get loggedInPlayer(): string {
    return require('jwt-decode')(this.token).playerUUID;
  }

  showTitle(playerUUID: string, title: string): Promise<void> {
    return this._onConnect.then(() => new Promise<void>((resolve, reject) =>
      this.eb.send(Minecraft.address, { "playerUUID": playerUUID, "message": title }, { "action": "showTitle" }, this.handle(resolve, reject))
    ));
  }

  narrate(playerUUID: string, entity: string, text: string): Promise<void> {
    return this._onConnect.then(() => new Promise<void>((resolve, reject) =>
      this.eb.send(Minecraft.address, { "playerUUID": playerUUID, "entity": entity, "text": text }, { "action": "narrate" }, this.handle(resolve, reject))
    ));
  }

  runCommand(playerUUID: string, command: string): Promise<void> {
    return this._onConnect.then(() => new Promise<void>((resolve, reject) =>
      this.eb.send(Minecraft.address, { "playerUUID": playerUUID, "command": command }, { "action": "runCommand" }, this.handle(resolve, reject))
    ));
  }

  getItemHeld(playerUUID: string, hand: HandType): Promise<ItemType> {
    return this._onConnect.then(() => new Promise<ItemType>((resolve, reject) =>
      this.eb.send(Minecraft.address, { "playerUUID": playerUUID, "hand": hand.toString() }, { "action": "getItemHeld" }, this.handle(resolve, reject))
    ));
  }

  addRemoveItem(playerUUID: string, amount: number, item: ItemType): Promise<void> {
    return this._onConnect.then(() => new Promise<void>((resolve, reject) =>
      this.eb.send(Minecraft.address, { "playerUUID": playerUUID, "amount": amount, "item": item }, { "action": "addRemoveItem" }, this.handle(resolve, reject))
    ));
  }

  // All Event Handlers go here

  whenCommand(commandName: string): Promise<Registration> {
    return this.whenRegister('newCmd' + commandName);
  }

  whenInside(playerUUID: string, name: string): Promise<Registration> {
    return this._onConnect.then(() => new Promise((resolve, reject) =>
      this.eb.send(Minecraft.address, { "playerUUID": playerUUID, "name": name }, { "action": "whenInside" }, this.handle(resolve, reject)))
      .then(() => this.whenRegister("player_inside_" + name + playerUUID)))
  }

  private whenRegister(eventName: string): Promise<Registration> {
    if (this.callbacks.get(eventName)) {
      return Promise.reject("can't re-register already registered event " + eventName);
    }
    return this._onConnect.then(() => new Promise<Registration>((resolve, reject) =>
      this.eb.send("mcs.actions", { "action": "registerCondition", "condition": eventName }, (err) => {
        if (!err) {
          resolve(new Registration(eventName, this.callbacks));
        } else {
          reject(err);
        }
      })
    ));
  }

  whenEntityRightClicked(entityName: string): Promise<Registration> {
    return this.whenRegister("entity_interaction:" + entityName + "/right clicked");
  }

  whenPlayerJoins(playerUUID: string): Subject<{ player: string }> {
    return this.when(playerUUID, "playerJoined");
  }

  private when<T>(playerUUID: string, eventName: string): Subject<T> {
    const existingSubject = this.callbacks.get(eventName);
    if (existingSubject) {
      return existingSubject;
    }
    const subject = new Subject<T>();
    this._onConnect.then(() => this.eb.send("mcs.actions", { "action": "registerCondition", "condition": eventName, "playerUUID": playerUUID }, (err) => {
      if (!err) {
        this.callbacks.set(eventName, subject);
      } else {
        subject.error(err);
      }
    }));
    return subject;
  }

  private handle<T>(resolve, reject) {
    return (err: any, result: any) => {
      if (!err) {
        resolve(result.body as T);
      } else {
        console.log("error: ", err)
        reject(err);
      };
    };
  }
}

export enum HandType {
  MainHand = "MainHand",
  OffHand = "OffHand"
}

export enum ItemType {
  Nothing = "Nothing",
  Apple = "Apple", Beef = "Beef", Beetroot = "Beetroot", Boat = "Boat", Book = "Book",
  Bow = "Bow", Bowl = "Bowl", Bread = "Bread", Cactus = "Cactus", Cake = "Cake",
  Carrot = "Carrot", Cauldron = "Cauldron", Chicken = "Chicken", Clock = "Clock",
  Cookie = "Cookie"
}

export class Token {
  loginCode?: string
  playerSource?: string
}

export interface LoginResponse {
  playerUuid: string;
}

export interface PlayerIniciatedEvent {
  playerUUID: string;
}

export class Registration {
  private subject: Subject<PlayerIniciatedEvent>;
  constructor(private commandName: string,
    private callbacks: Map<string, Subject<PlayerIniciatedEvent>>) {
    this.subject = new Subject<PlayerIniciatedEvent>();
    this.callbacks.set(this.commandName, this.subject);
  }
  on(): Subject<PlayerIniciatedEvent> {
    return this.subject;
  }

  unregister(): void {
    this.callbacks.delete(this.commandName);
    //TODO send an unregister event to the server and handle
    // this.eb.send("ch.vorburger.minecraft.storeys", {})
  }
}
