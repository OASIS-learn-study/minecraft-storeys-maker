/**
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

import static com.google.common.truth.Truth.assertThat;

import ch.vorburger.minecraft.storeys.events.ConditionService.ConditionServiceRegistration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class ConditionServiceTest {

    @Test
    public final void testConditionServiceBasics() {
        @SuppressWarnings("resource")
        ConditionService conditionService = new ConditionService();
        final AtomicBoolean hit = new AtomicBoolean(false);

        ConditionServiceRegistration registration = conditionService.register(() -> true, () -> hit.set(true));
        conditionService.run();
        assertThat(hit.get()).isTrue();

        registration.unregister();
        hit.set(false);
        conditionService.run();
        assertThat(hit.get()).isFalse();
    }

    @Test
    public final void testConditionServiceFiresOnlyOnChange() {
        @SuppressWarnings("resource")
        ConditionService conditionService = new ConditionService();
        final AtomicBoolean isHitting = new AtomicBoolean(false);
        final AtomicInteger hits = new AtomicInteger(0);
        ConditionServiceRegistration registration = conditionService.register(() -> isHitting.get(), () -> hits.incrementAndGet());

        conditionService.run();
        assertThat(hits.get()).isEqualTo(0);

        isHitting.set(true);
        conditionService.run();
        assertThat(hits.get()).isEqualTo(1);

        conditionService.run();
        // It must still be 1 and not 2 now!
        assertThat(hits.get()).isEqualTo(1);

        conditionService.run();
        assertThat(hits.get()).isEqualTo(1);

        isHitting.set(false);
        conditionService.run();
        assertThat(hits.get()).isEqualTo(1);

        isHitting.set(true);
        conditionService.run();
        assertThat(hits.get()).isEqualTo(2);

        // We do one last toggle mainly to make sure that the following unregister is not affected by the state change
        isHitting.set(false);
        conditionService.run();
        assertThat(hits.get()).isEqualTo(2);

        registration.unregister();
        conditionService.run();
        assertThat(hits.get()).isEqualTo(2);
    }

}
