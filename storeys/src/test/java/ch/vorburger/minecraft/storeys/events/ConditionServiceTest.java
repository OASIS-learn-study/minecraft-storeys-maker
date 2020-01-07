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
package ch.vorburger.minecraft.storeys.events;

import ch.vorburger.minecraft.storeys.events.ConditionService.ConditionServiceRegistration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.spongepowered.api.entity.living.player.Player;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ConditionServiceTest {

    @Test
    public final void testConditionServiceBasics() {
        @SuppressWarnings("resource")
        ConditionService conditionService = new ConditionService();
        final AtomicBoolean hit = new AtomicBoolean(false);

        ConditionServiceRegistration registration = conditionService.register(new Condition() {
            @Override
            public boolean isHot() {
                return true;
            }

            @Override
            public Player getEffectedPlayer() {
                return null;
            }
        }, (player) -> hit.set(true));
        conditionService.run();
        assertThat(hit.get(), is(true));

        registration.unregister();
        hit.set(false);
        conditionService.run();
        assertThat(hit.get(), is(false));
    }

    @Test
    public final void testConditionServiceFiresOnlyOnChange() {
        @SuppressWarnings("resource")
        ConditionService conditionService = new ConditionService();
        final AtomicBoolean isHitting = new AtomicBoolean(false);
        final AtomicInteger hits = new AtomicInteger(0);
        ConditionServiceRegistration registration = conditionService.register(new Condition() {
            @Override
            public boolean isHot() {
                return isHitting.get();
            }

            @Override
            public Player getEffectedPlayer() {
                return null;
            }
        }, (player) -> hits.incrementAndGet());

        conditionService.run();
        assertThat(hits.get(), is(0));

        isHitting.set(true);
        conditionService.run();
        assertThat(hits.get(), is(1));

        conditionService.run();
        // It must still be 1 and not 2 now!
        assertThat(hits.get(), is(1));

        conditionService.run();
        assertThat(hits.get(), is(1));

        isHitting.set(false);
        conditionService.run();
        assertThat(hits.get(), is(1));

        isHitting.set(true);
        conditionService.run();
        assertThat(hits.get(), is(2));

        registration.unregister();
        conditionService.run();
        assertThat(hits.get(), is(2));
    }

}
