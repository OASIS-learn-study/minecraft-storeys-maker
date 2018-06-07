import { Observable, Observer } from 'rxjs';
import * as EventBus from 'vertx3-eventbus-client';
import { Minecraft } from '../../../build/classes/java/main/Minecraft-ts/minecraft-proxy';

export class StoreysMinecraft {
  private minecraft: Minecraft;
  constructor(private eb: any) {
    this.minecraft = new Minecraft(eb, "ch.vorburger.minecraft.storeys");
  }

  showTitle(code: string, title: string): Observable<void> {
    return Observable.create(observer => {
      this.minecraft.showTitle(code, title, this.handler(observer));
    });
  }

  narrate(code: string, entity: string, text: string): Observable<void> {
    return Observable.create(observer => {
      this.minecraft.narrate(code, entity, text, this.handler(observer));
    });
  }

  private handler(observer: Observer<void>) {
    return (err: any, result: any) => {
      if (!err) {
        observer.next(result);
      } else {
        observer.error(err);
      };
    };
  }
}