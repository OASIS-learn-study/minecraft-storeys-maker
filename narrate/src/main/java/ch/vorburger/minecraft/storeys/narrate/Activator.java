package ch.vorburger.minecraft.storeys.narrate;

import ch.vorburger.minecraft.storeys.narrate.commands.*;
import ch.vorburger.minecraft.osgi.api.CommandRegistration;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        context.registerService(CommandRegistration.class, new NarrateCommand(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {

    }

}