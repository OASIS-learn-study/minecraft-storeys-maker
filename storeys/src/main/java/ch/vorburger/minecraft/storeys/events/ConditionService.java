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

import static java.util.Objects.requireNonNull;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import com.google.common.annotations.VisibleForTesting;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.scheduler.Task;

@ThreadSafe
public class ConditionService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ConditionService.class);

    public class ConditionServiceRegistration {
        private final Pair<Condition, Callback> entry;

        private ConditionServiceRegistration(Pair<Condition, Callback> entry) {
            this.entry = entry;
        }

        public void unregister() {
            checks.remove(entry);
        }
    }

    private final Set<Pair<Condition, Callback>> checks = new CopyOnWriteArraySet<>();
    private final @Nullable Task task;

    public ConditionService(PluginInstance plugin) {
        task = Task.builder().execute(() -> run()).intervalTicks(10).name(getClass().getSimpleName()).submit(requireNonNull(plugin, "plugin"));
    }

    @VisibleForTesting
    ConditionService() {
        task = null;
    }

    @Override
    public void close() throws Exception {
        task.cancel();
    }

    public ConditionServiceRegistration register(Condition condition, Callback callback) {
        Pair<Condition, Callback> entry = Pair.of(condition, callback);
        checks.add(entry);
        return new ConditionServiceRegistration(entry);
    }

    @VisibleForTesting
    void run() {
        for (Pair<Condition, Callback> check : checks) {
            if (check.getLeft().isHot()) {
                try {
                    check.getRight().call();
                } catch (Exception e) {
                    LOG.error("Condition failed: {}", check.getLeft(), e);
                }
            }
        }
    }

}
