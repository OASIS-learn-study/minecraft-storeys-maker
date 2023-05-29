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

import static java.util.Objects.requireNonNull;

import ch.vorburger.minecraft.storeys.japi.impl.Unregisterable;
import ch.vorburger.minecraft.storeys.japi.impl.events.Callback;
import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

@ThreadSafe @Singleton public class ConditionService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ConditionService.class);

    /**
     * Wrapper for the Condition because in multi-threaded scenarios any
     * change to its equals() and hashCode() could break the unregistration,
     * if the implementations equals() and hashCode() relied on non-volatile/atomic fields.
     */
    private static class DelegatingCondition implements Condition {

        final Condition delegate;

        DelegatingCondition(Condition delegate) {
            this.delegate = delegate;
        }

        @Override public boolean isHot() {
            return delegate.isHot();
        }

        @Override public Player getEffectedPlayer() {
            return delegate.getEffectedPlayer();
        }

        @Override public final boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override public final int hashCode() {
            return super.hashCode();
        }
    }

    public class ConditionServiceRegistration implements Unregisterable {
        private final Triple<Condition, AtomicBoolean, Callback> entry;

        private ConditionServiceRegistration(Triple<Condition, AtomicBoolean, Callback> entry) {
            this.entry = entry;
        }

        @Override public void unregister() {
            checks.remove(entry);
        }
    }

    private final Set<Triple<Condition, AtomicBoolean, Callback>> checks = new CopyOnWriteArraySet<>();
    private final Scheduler scheduler;
    private final UUID taskId;

    @Inject public ConditionService(Scheduler scheduler) {
        Task task = Task.builder().execute(this::run).interval(10, TimeUnit.SECONDS).build();
        this.scheduler = scheduler;
        taskId = scheduler.submit(task).uniqueId();
    }

    @VisibleForTesting ConditionService() {
        scheduler = null;
        taskId = UUID.randomUUID();
    }

    @Override public void close() {
        final Optional<ScheduledTask> scheduledTask = scheduler.findTask(taskId);
        scheduledTask.ifPresent(ScheduledTask::cancel);
    }

    public ConditionServiceRegistration register(Condition condition, Callback callback) {
        Triple<Condition, AtomicBoolean, Callback> entry = Triple.of(new DelegatingCondition(condition), new AtomicBoolean(false),
                callback);
        checks.add(entry);
        return new ConditionServiceRegistration(entry);
    }

    @VisibleForTesting void run() {
        for (Triple<Condition, AtomicBoolean, Callback> check : checks) {
            final Condition condition = check.getLeft();
            final boolean isHot = condition.isHot();
            final boolean wasHot = check.getMiddle().get();
            if (!wasHot && isHot) {
                try {
                    check.getRight().call(condition.getEffectedPlayer());
                } catch (Exception e) {
                    LOG.error("Condition failed: {}", condition, e);
                }
                check.getMiddle().set(true);
            } else if (wasHot && !isHot) {
                check.getMiddle().set(false);
            }
        }
    }

}
