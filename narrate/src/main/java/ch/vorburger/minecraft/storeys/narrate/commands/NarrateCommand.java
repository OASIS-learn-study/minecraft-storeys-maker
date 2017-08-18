package ch.vorburger.minecraft.storeys.narrate.commands;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.narrate.Narrator;
import ch.vorburger.minecraft.storeys.util.NamedObjects;
import ch.vorburger.minecraft.utils.Command;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

public class NarrateCommand implements Command {

    private final NamedObjects namedObjects = new NamedObjects();
    private final Narrator narrator;

    public NarrateCommand(PluginInstance plugin) {
        narrator = new Narrator(plugin);
    }

    @Override
    public List<String> aliases() {
        return ImmutableList.of("narrate", "story");
    }

    @Override
    public CommandCallable callable() {
        return CommandSpec.builder()
            .description(Text.of("Narrate a story"))
            .executor(this).build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        // TODO read this from the story file, as a /say
        src.sendMessage(Text.of("Once upon a time.. there was a pig"));

        Server server = Sponge.getGame().getServer();
        String worldName = server.getDefaultWorldName();
        World world = server.getWorld(worldName).orElseThrow(() -> new IllegalArgumentException("No world named: " + worldName));

        String entityName = "MsPig";
        Entity entity = namedObjects.getEntity(world, entityName).orElseThrow(() -> new IllegalArgumentException("No entity named: " + entityName));

        // Duh - STOP MOVING! ;)
        // TODO later probably remove this
        entity.setVelocity(new Vector3d(0.0, 0.0, 0.0));

        narrator.narrate(entity, "hello, world.  I'm a pig.  But this is probably getting too long?");

        return CommandResult.success();
    }
}
