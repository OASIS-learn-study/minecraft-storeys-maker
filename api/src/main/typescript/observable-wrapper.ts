import { Observable, Observer, Subject, ConnectableObservable } from 'rxjs';

import * as EventBus from 'vertx3-eventbus-client';
import { JSEncrypt } from 'jsencrypt';

export class MinecraftProvider {
  private eb: any;

  connect(eventBusURL: string, code: string): Observable<Minecraft> {
    this.eb = new EventBus(eventBusURL);
    this.eb.enableReconnect(true);
    return Observable.create(observer => {
      this.eb.onopen = () => {
        let crypt = new JSEncrypt(512);
        crypt.getKey(() => {
          this.login(code, crypt.getPublicKeyB64()).subscribe(response => {
              console.log("Logging in...", response);
              var id = crypt.decrypt(response.secret);
              var key = response.key;
              crypt = new JSEncrypt();
              crypt.setPublicKey(key);
              const minecraft = new Minecraft(this.eb, crypt.encrypt(id) || "");
              minecraft.onConnect();
              observer.next(minecraft);
          }, err => console.log("login reply with error: ", err));
        });
      }
    });
  }

  private login(token: string, key: string): Observable<LoginResponse> {
    return Observable.create(observer => {
      this.eb.send(Minecraft.address, { "token": token, "key": key }, { "action": "login" }, (err: any, result: any) => {
        if (!err) {
          observer.next(result.body as LoginResponse);
        } else {
          observer.error(err);
        }
      });
    });
  }
}

export class Minecraft {
  static address = "ch.vorburger.minecraft.storeys";
  private callbacks: Map<string, Subject<any>> = new Map();

  callBuffer: ConnectableObservable<any>[] = [];
  isOpen: boolean = false;

  constructor(private eb: any, private code: string) {
    this.eb.registerHandler("mcs.events", (error, message) => {
      if (error == null) {
        this.callbacks.get(message.body.event).next(message.body);
      } else {
        console.log("Vert.x Event Bus received error:", error);
      }
    });
  }

  @buffered()
  showTitle(title: string): Observable<void> {
    return Observable.create(observer => {
      this.eb.send(Minecraft.address, { "token": { loginCode: this.code }, "message": title }, { "action": "showTitle" }, this.handler(observer));
    });
  }

  @buffered()
  narrate(entity: string, text: string): Observable<void> {
    return Observable.create(observer => {
      this.eb.send(Minecraft.address, { "code": this.code, "entity": entity, "text": text }, { "action": "narrate" }, this.handler(observer));
    });
  }

  @buffered()
  runCommand(command: string): Observable<void> {
    return Observable.create(observer => {
      this.eb.send(Minecraft.address, {"code": this.code, "command": command}, {"action":"runCommand"}, this.handler(observer));
    });
  }

  @buffered()
  getItemHeld(hand: HandType): Observable<ItemType> {
    return Observable.create(observer => {
      this.eb.send(Minecraft.address, { "code": this.code, "hand": hand.toString() }, { "action": "getItemHeld" }, this.handler(observer));
    });
  }

  onConnect() {
    this.isOpen = true;
    this.callBuffer.forEach((value) => value.connect());
  }

  // All Event Handlers go here

  whenCommand(commandName: string): Observable<Registration> {
    return this.whenRegister('newCmd' + commandName);
  }

  whenInside(name: string): Observable<Registration> {
    return Observable.create(observer => {
      this.eb.send(Minecraft.address, { "code": this.code, "name": name }, { "action": "whenInside" }, this.handler(observer));
    }).map(() => this.whenRegister("player_inside_" + name)).concatAll();
    
  }

  private whenRegister(eventName: string): Observable<Registration> {
    if (this.callbacks.get(eventName)) {
      return Observable.create(observer => observer.error("can't re-register already registered event " + eventName));
    }
    return Observable.create(observer => {
      this.eb.send("mcs.actions", { "action": "registerCondition", "condition": eventName, "code": this.code }, (err) => {
        if (!err) {
          observer.next(new Registration(eventName, this.callbacks));
        } else {
          observer.error(err);
        }
      });
    });
  }

  whenEntityRightClicked(entityName: string): Subject<void> {
    return this.when("entity_interaction:" + entityName + "/right clicked");
  }

  whenPlayerJoins(): Subject<{player: string}> {
    return this.when("playerJoined");
  }

  private when<T>(eventName: string): Subject<T> {
    const existingSubject = this.callbacks.get(eventName);
    if (existingSubject) {
      return existingSubject;
    }
    const subject = new Subject<T>();
    this.eb.send("mcs.actions", { "action": "registerCondition", "condition": eventName, "code": this.code }, (err) => {
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

export class LoginResponse {
  secret: string;
  key: string;
}

export class Registration {
  private subject: Subject<void>;
  constructor(private commandName: string,
    private callbacks: Map<string, Subject<void>>) {
      this.subject = new Subject();
      this.callbacks.set(this.commandName, this.subject);
  }
  on(): Subject<void> {
    return this.subject;
  }

  unregister(): void {
    this.callbacks.delete(this.commandName);
    //TODO send an unregister event to the server and handle
    // this.eb.send("ch.vorburger.minecraft.storeys", {})
  }
}
