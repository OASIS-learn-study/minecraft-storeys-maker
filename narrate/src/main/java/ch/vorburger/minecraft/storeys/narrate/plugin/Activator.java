package ch.vorburger.minecraft.storeys.narrate.plugin;

import ch.vorburger.minecraft.osgi.api.CommandRegistration;
import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.narrate.commands.NarrateCommand;
import ch.vorburger.minecraft.storeys.narrate.commands.StoryCommand;
import java.nio.file.Paths;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator {

    private ServiceReference<PluginInstance> pluginInstanceServiceReference;

    @Override
    public void start(BundleContext context) throws Exception {
        pluginInstanceServiceReference = context.getServiceReference(PluginInstance.class);
        PluginInstance osgiPlugin = context.getService(pluginInstanceServiceReference);

        context.registerService(CommandRegistration.class, new StoryCommand(osgiPlugin, Paths.get("config/storeys")), null);
        context.registerService(CommandRegistration.class, new NarrateCommand(osgiPlugin), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        context.ungetService(pluginInstanceServiceReference);
        // No need to unregister our services, because when a bundle stops,
        // any services that it registered will be automatically unregistered.
    }

}
