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
package ch.vorburger.minecraft.storeys.web;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import java.nio.file.Paths;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    private StoreysWebPlugin plugin;
    private ServiceReference<PluginInstance> pluginInstanceServiceRef;

    @Override public void start(BundleContext context) throws Exception {
        LOG.info("storeys.web start()");
        pluginInstanceServiceRef = context.getServiceReference(PluginInstance.class);
        PluginInstance realPluginInstance = context.getService(pluginInstanceServiceRef);

        plugin = new StoreysWebPlugin();
        plugin.start(realPluginInstance, Paths.get("config", "storeys"));
    }

    @Override public void stop(BundleContext context) throws Exception {
        LOG.info("storeys.web stop()");
        plugin.stop();
        context.ungetService(pluginInstanceServiceRef);
    }

}
