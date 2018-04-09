import * as EventBus from 'vertx3-eventbus-client';
import { JSEncrypt } from "jsencrypt";

export class Minecraft {

    private eb : any; // TODO EventBus
    private code : string;

    constructor(private token: string) {
        var crypt = new JSEncrypt(512);
        // TODO url must be made configurable
        this.eb = new EventBus("http://localhost:8080/eventbus");
        this.eb.enableReconnect(true);
        this.eb.onopen = function() {
            this.eb.registerHandler("mcs.events", function (error, message) {
                if (error != null) {
                    console.log("Vert.x Event Bus received error: " + error);
                } else if (message.body.event === 'loggedIn') {
                    var id = crypt.decrypt(message.body.secret);
                    var key = message.body.key;
                    crypt = new JSEncrypt();
                    crypt.setPublicKey(key);
                    this.code = crypt.encrypt(id);
                } else {
                    ext.eventReceived(message);
                }
            });

            crypt.getKey(function() {
                this.eb.send("mcs.actions", { action: "login", token: token, key: crypt.getPublicKeyB64()});
            });

            this.eb.send("mcs.actions", { "action": "ping" });
            // TODO await "PONG" reply, and set status green
        };
        this.eb.onclose = function() {
            console.log("Vert.x Event Bus Connection Close");
        };
    }

    setTitle(title: string): Promise<void> {
        return new Promise((resolve, reject) => {
            this.eb.send("mcs.actions", { "action": "setTitle", "text": title, "code": this.token }, (error, reply) => {
                if(error) {
                    reject(error);
                } else {
                    resolve(/* No reply */);
                }
            });
        });
    }

    whenCommand(name: string, callback: Function) {
        // TODO...
        //    this.eb.send("mcs.actions", { "action": "registerCondition", "condition": "newCmd" + name, "code": this.token }, function(reply) {
        // });
    }

    // TODO ...
}