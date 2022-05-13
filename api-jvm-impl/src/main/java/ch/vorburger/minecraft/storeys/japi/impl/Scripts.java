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
package ch.vorburger.minecraft.storeys.japi.impl;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.japi.Script;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Scripts {

    private final Map<Object, Unregisterable> unregisterables = new ConcurrentHashMap<>();
    private final PluginInstance plugin;

    @Inject public Scripts(PluginInstance plugin) {
        this.plugin = plugin;
    }

    public void register(Object key, Script script) {
        EventsImpl e = new EventsImpl(plugin);
        if (unregisterables.putIfAbsent(key, e) != null) {
            throw new IllegalArgumentException("Key already registered, must unregister() it first: " + key);
        }
        script.init(e);
    }

    public void unregister(Object key) {
        Unregisterable unregisterable = unregisterables.remove(key);
        if (unregisterable != null) {
            unregisterable.unregister();
        }
    }

    public Collection<Unregisterable> getUnregisterables() {
        return unregisterables.values();
    }
}
