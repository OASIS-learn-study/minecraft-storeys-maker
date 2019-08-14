/*
 * ch.vorburger.minecraft.storeys
 *
 * Copyright (C) 2016 - 2018 Michael Vorburger.ch <mike@vorburger.ch>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package study.learn.storeys.engine.prompters;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

import study.learn.storeys.engine.Text;

public class WebsocketPrompterIO implements SimplePrompterIO {

    private CountDownLatch responseAwaitLatch = new CountDownLatch(1);

    private String answer;

    private final Session session;

    WebsocketPrompterIO(Session session) {
        this.session = session;
    }

    @Override
    public String readLine(String prompt, List<Text> choices) throws IOException {
        RemoteEndpoint.Async remote = session.getAsyncRemote();
        remote.sendText(prompt);

        for (int i = 0; i < choices.size(); i++) {
            remote.sendText("<a href='javascript:sendResponse(" + (i + 1) + ")'>" + choices.get(i).getString() + "</a>");
        }

        return waitForResponse();
    }

    void trigger(String answerString) {
        this.answer = answerString;
        responseAwaitLatch.countDown();
    }

    private String waitForResponse() {
        try {
            responseAwaitLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("don't interrupt me when I'm talking", e);
        }
        responseAwaitLatch = new CountDownLatch(1);
        return answer;
    }

    @Override
    public void writeLine(String info) throws IOException {
        session.getAsyncRemote().sendText(info);
    }

}

