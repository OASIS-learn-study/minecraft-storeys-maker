import { Observable, Observer } from 'rxjs';

export class Minecraft {
  private address = "ch.vorburger.minecraft.storeys";
  constructor(private eb: any) {
  }

  showTitle(token: Token, title: string): Observable<void> {
    return Observable.create(observer => {
      this.eb.send(this.address, {"token": token, "message": title}, {"action":"showTitle"}, this.handler(observer));
    });
  }

  narrate(code: string, entity: string, text: string): Observable<void> {
    return Observable.create(observer => {
      this.eb.send(this.address, {"code": code, "entity": entity, "text": text}, {"action":"narrate"}, this.handler(observer));
    });
  }

  login(token: string, key: string): Observable<LoginResponse> {
    return Observable.create(observer => {
      this.eb.send(this.address, {"token": token, "key": key}, {"action":"login"}, this.handler(observer));
    });
  }

  runCommand(code: string, command: string): Observable<void> {
    return Observable.create(observer => {
      this.eb.send(this.address, {"code": code, "command": this.escapeQuotes(command) }, {"action":"runCommand"}, this.handler(observer));
    });
  }

  getItemHeld(code: string, hand: HandType): Observable<ItemType> {
    return Observable.create(observer => {
      this.eb.send(this.address, {"code": code, "hand": hand.toString()}, {"action":"getItemHeld"}, this.handler(observer));
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

  private escapeQuotes(text: string): string {
    // see https://github.com/vorburger/minecraft-storeys-maker/issues/92
    // TODO find the right expression and then replace it with text.replace(new RegExp(..., 'g'), '\\"');
    return text.replace('\"', '\\"');
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