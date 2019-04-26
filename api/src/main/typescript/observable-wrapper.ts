import { Observable, Observer, Subject, ConnectableObservable } from 'rxjs';
import * as EventBus from 'vertx3-eventbus-client';

export class MinecraftProvider {
  private eb: any;

  connect(eventBusURL: string): Observable<Minecraft> {
    this.eb = new EventBus(eventBusURL);
    this.eb.enableReconnect(true);
    return Observable.create(observer => {
      this.eb.onopen = () => {
        const minecraft = new Minecraft(this.eb);
        minecraft.onConnect();
        observer.next(minecraft);
      }
    });
  }
}

export class Minecraft {
  static address = "ch.vorburger.minecraft.storeys";
  private callbacks: Map<string, Subject<any>> = new Map();

  callBuffer: ConnectableObservable<any>[] = [];
  isOpen: boolean = false;

  constructor(private eb: any) {
    this.eb.registerHandler("mcs.events", (error, message) => {
      if (error == null) {
        this.callbacks.get(message.body.event).next(message.body);
      } else {
        console.log("Vert.x Event Bus received error:", error);
      }
    });
  }

  login(key: string): Observable<LoginResponse> {
    return Observable.create(observer => {
      this.eb.send(Minecraft.address, { key }, { "action": "login" }, (err: any, result: any) => {
        if (!err) {
          observer.next(result.body as LoginResponse);
        } else {
          observer.error(err);
        }
      });
    });
  }

  @buffered()
  showTitle(playerUUID: string, title: string): Observable<void> {
    return Observable.create(observer => {
      this.eb.send(Minecraft.address, { "playerUUID": playerUUID, "message": title }, { "action": "showTitle" }, this.handler(observer));
    });
  }

  @buffered()
  narrate(playerUUID: string, entity: string, text: string): Observable<void> {
    return Observable.create(observer => {
      this.eb.send(Minecraft.address, { "playerUUID": playerUUID, "entity": entity, "text": text }, { "action": "narrate" }, this.handler(observer));
    });
  }

  @buffered()
  runCommand(playerUUID: string, command: string): Observable<void> {
    return Observable.create(observer => {
      this.eb.send(Minecraft.address, {"playerUUID": playerUUID, "command": command}, {"action":"runCommand"}, this.handler(observer));
    });
  }

  @buffered()
  getItemHeld(playerUUID: string, hand: HandType): Observable<ItemType> {
    return Observable.create(observer => {
      this.eb.send(Minecraft.address, { "playerUUID": playerUUID, "hand": hand.toString() }, { "action": "getItemHeld" }, this.handler(observer));
    });
  }

  @buffered()
  addRemoveItem(playerUUID: string, amount: number, item: ItemType): Observable<void> {
    return Observable.create(observer => {
      this.eb.send(Minecraft.address, { "playerUUID": playerUUID, "amount": amount, "item": item }, { "action": "addRemoveItem" }, this.handler(observer));
    });
  }

  onConnect() {
    this.isOpen = true;
    this.callBuffer.forEach((value) => value.connect());
  }

  // All Event Handlers go here

  whenCommand(playerUUID: string, commandName: string): Observable<Registration> {
    return this.whenRegister(playerUUID, 'newCmd' + commandName);
  }

  whenInside(playerUUID: string, name: string): Observable<Registration> {
    return Observable.create(observer => {
      this.eb.send(Minecraft.address, { "playerUUID": playerUUID, "name": name }, { "action": "whenInside" }, this.handler(observer));
    }).map(() => this.whenRegister(playerUUID, "player_inside_" + name + playerUUID)).concatAll();
  }

  private whenRegister(playerUUID: string, eventName: string): Observable<Registration> {
    if (this.callbacks.get(eventName)) {
      return Observable.create(observer => observer.error("can't re-register already registered event " + eventName));
    }
    return Observable.create(observer => {
      this.eb.send("mcs.actions", { "action": "registerCondition", "condition": eventName, "playerUUID": playerUUID }, (err) => {
        if (!err) {
          observer.next(new Registration(eventName, this.callbacks));
        } else {
          observer.error(err);
        }
      });
    });
  }

  whenEntityRightClicked(playerUUID: string, entityName: string): Observable<Registration> {
    return this.whenRegister(playerUUID, "entity_interaction:" + entityName + "/right clicked");
  }

  whenPlayerJoins(playerUUID: string): Subject<{player: string}> {
    return this.when(playerUUID, "playerJoined");
  }

  private when<T>(playerUUID: string, eventName: string): Subject<T> {
    const existingSubject = this.callbacks.get(eventName);
    if (existingSubject) {
      return existingSubject;
    }
    const subject = new Subject<T>();
    this.eb.send("mcs.actions", { "action": "registerCondition", "condition": eventName, "playerUUID": playerUUID }, (err) => {
      if (!err) {
        this.callbacks.set(eventName, subject);
      } else {
        subject.error(err);
      }
    });
    return subject;
  }

  private handler<T>(observer: Observer<T>) {
    return (err: any, result: any) => {
      if (!err) {
        observer.next(result.body as T);
      } else {
        console.log("error: ", err)
        observer.error(err);
      };
    };
  }
}

export function buffered() {
  return function (target, key, descriptor) {
    const originalMethod = descriptor.value;
    descriptor.value = function (...args: any[]) {
      if (this.isOpen) {
        return originalMethod.apply(this, args);
      }

      const obs = originalMethod.apply(this, args).publish();
      this.callBuffer.push(obs);
      return obs;
    };

    return descriptor;
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
