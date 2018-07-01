import { Observable, Observer } from 'rxjs';

export class Minecraft {
  private address = "ch.vorburger.minecraft.storeys";
  constructor(private eb: any) {
  }

  showTitle(code: string, title: string): Observable<void> {
    return Observable.create(observer => {
      this.eb.send(this.address, {"token": {loginCode: code}, "message": title}, {"action":"showTitle"}, this.handler(observer));
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
        observer.next(result as T);
      } else {
        observer.error(err);
      };
    };
  }
}

export enum HandType {
  MainHand,
  OffHand
}

export enum ItemType {
  Nothing,
  Apple, Beef, Beetroot, Boat, Book, Bow, Bowl, Bread,
  Cactus, Cake, Carrot, Cauldron, Chicken, Clock,
  Cookie
}

export class LoginResponse {
  secret: string;
  key: string;
}