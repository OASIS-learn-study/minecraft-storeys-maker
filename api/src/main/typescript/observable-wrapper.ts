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
}

export enum HandType {
  MainHand = "MainHand",
  OffHand = "OffHand"
}

export enum ItemType {
  Nothing,
  Apple, Beef, Beetroot, Boat, Book, Bow, Bowl, Bread,
  Cactus, Cake, Carrot, Cauldron, Chicken, Clock,
  Cookie
}

export class Token {
  loginCode?: string
  playerSource?: string
}

export class LoginResponse {
  secret: string;
  key: string;
}