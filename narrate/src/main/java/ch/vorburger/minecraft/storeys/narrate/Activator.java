package ch.vorburger.minecraft.storeys.narrate;

import ch.vorburger.minecraft.osgi.api.CommandRegistration;
import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.narrate.commands.NarrateCommand;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator {

    private ServiceReference<PluginInstance> pluginInstanceServiceReference;

    @Override
    public void start(BundleContext context) throws Exception {
        pluginInstanceServiceReference = context.getServiceReference(PluginInstance.class);
        PluginInstance osgiPlugin = context.getService(pluginInstanceServiceReference);
        context.registerService(CommandRegistration.class, new NarrateCommand(osgiPlugin), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        context.ungetService(pluginInstanceServiceReference);
    }

}
