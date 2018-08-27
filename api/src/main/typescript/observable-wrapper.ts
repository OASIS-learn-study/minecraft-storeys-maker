import { Observable, Observer, Subject } from 'rxjs';

export class Minecraft {
  private address = "ch.vorburger.minecraft.storeys";
  private callbacks: Map<string, Subject<any>> = new Map();

  constructor(private eb: any) {
    eb.registerHandler("mcs.events", function (error, message) {
      if (error == null) {
        this.callbacks.get(message.body.event).next(message.body);
      } else {
        console.log("Vert.x Event Bus received error: " + error);
      }
    });
  }

  showTitle(token: Token, title: string): Observable<void> {
    return Observable.create(observer => {
      this.eb.send(this.address, {"token": token, "message": title}, {"action":"showTitle"}, this.handler(observer));
    });
  }

  narrate(code: string, entityName: string, text: string): Observable<void> {
    return Observable.create(observer => {
      this.eb.send(this.address, {"code": code, "entity": entityName, "text": text}, {"action":"narrate"}, this.handler(observer));
    });
  }

  login(token: string, key: string): Observable<LoginResponse> {
    return Observable.create(observer => {
      this.eb.send(this.address, {"token": token, "key": key}, {"action":"login"}, this.handler(observer));
    });
  }

  runCommand(code: string, command: string): Observable<void> {
    return Observable.create(observer => {
      this.eb.send(this.address, {"code": code, "command": command}, {"action":"runCommand"}, this.handler(observer));
    });
  }

  getItemHeld(code: string, hand: HandType): Observable<ItemType> {
    return Observable.create(observer => {
      this.eb.send(this.address, {"code": code, "hand": hand.toString()}, {"action":"getItemHeld"}, this.handler(observer));
    });
  }

  // All Event Handlers go here

  whenCommand(token: Token, commandName: string): Observable<CommandRegistration> {
    return this.whenRegister(token, 'newCmd' + commandName);
  }

  whenInside(token: Token, x1, y1, z1, x2, y2, z2: number): Observable<InsideRegistration> {
    return this.whenRegister(token, "myPlayer_inside_" + x1 + "/" + y1 + "/" + z1 + "/" + x2 + "/" + y2 + "/" + z2 + "/");
  }

  private whenRegister(token: Token, eventName: string) {
    if (this.callbacks.get(eventName)) {
      return Observable.create(observer => observer.error("can't re-register already registered command " + eventName));
    }
    return Observable.create(observer => {
      this.eb.send(this.address, "mcs.actions", { "action": "registerCondition", "condition": eventName, "code": token.loginCode }, (err) => {
        if (!err) {
          const subject = Subject.create();
          this.callbacks.set(eventName, subject);
          observer.next(new CommandRegistration(eventName, subject, this.callbacks, this.eb));
        } else {
          observer.error(err);
        }
      });
    });
  }

  whenEntityRightClicked(token: Token, entityName: string): Subject<void> {
    return this.when(token, "entity_interaction:" + entityName + "/right clicked");
  }

  whenPlayerJoins(token: Token): Subject<{player: string}> {
    return this.when(token, "playerJoined");
  }

  private when<T>(token: Token, eventName: string): Subject<T> {
    const existingSubject = this.callbacks.get(eventName);
    if (existingSubject) {
      return existingSubject;
    }
    return Subject.create(subject => {
      this.eb.send(this.address, {"token": token}, (err) => {
        if (!err) {
          this.callbacks.set(eventName, subject);
        } else {
          subject.error(err);
        }
      });
    });
  }

  private handler<T>(observer: Observer<T>) {
    return (err: any, result: any) => {
      if (!err) {
        console.log("result: ", result)
        observer.next(result.body as T);
      } else {
        console.log("error: ", err)
        observer.error(err);
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

export class LoginResponse {
  secret: string;
  key: string;
}

export abstract class AbstractRegistration {
  constructor(private commandName: string, 
    private subject: Subject<void>,
    private callbacks: Map<string, Subject<void>>,
    private eb: any) {
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

export class CommandRegistration extends AbstractRegistration {
}

export class InsideRegistration extends AbstractRegistration {
}
