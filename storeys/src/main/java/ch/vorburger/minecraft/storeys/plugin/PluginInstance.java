/**
 * ch.vorburger.minecraft.osgi
 *
 * Copyright (C) 2016 - 2017 Michael Vorburger.ch <mike@vorburger.ch>
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
package ch.vorburger.minecraft.storeys.plugin;

import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

/**
 * Marker interface for a Sponge {@link Plugin} annotated class.
 *
 * <p>An instance of this class is registered in the OSGi service
 * registry.  This allows OSGi plugin bundles to obtain the
 * "plugin" (of type Object) which some Sponge APIs require
 * (<code>e.g. Task.Builder.submit(Object plugin)</code>).
 *
 * <p>When running under OSGi, this is (has to be, unfortunately)
 * the single MinecraftSpongePlugin, which is shared among all
 * OSGi plugin bundles.  To make integration with standard
 * non-OSGi Sponge easier, it is recommended that your own plugin's
 * {@link Plugin} annotated class implement this interface as well.
 *
 * @author Michael Vorburger.ch
 */
public interface PluginInstance {

    PluginContainer getPluginContainer();

}