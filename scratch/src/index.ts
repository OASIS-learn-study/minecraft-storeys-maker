import * as EventBus from 'vertx3-eventbus-client';
import { JSEncrypt } from 'jsencrypt';

export default class Main {
    constructor() {
        let eventbus = new EventBus("http://localhost:8080/eventbus");
        (<any>eventbus).enableReconnect(true);
        eventbus.onopen = () => {
            console.log("connected to server");
            let crypt = new JSEncrypt(512);
            console.log(crypt);
            
        };
        console.log('Typescript Webpack starter launched');
    }
}

let start = new Main();