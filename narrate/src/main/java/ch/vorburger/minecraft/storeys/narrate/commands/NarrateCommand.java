package ch.vorburger.minecraft.storeys.narrate.commands;

import ch.vorburger.minecraft.osgi.api.CommandRegistration;
import ch.vorburger.minecraft.storeys.util.NamedObjects;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.registry.RegistryModule;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

public class NarrateCommand implements CommandRegistration, CommandExecutor {

    private final NamedObjects namedObjects = new NamedObjects();

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

        // Make sure name can always be seen, even if we are not closely look at entity
        entity.offer(Keys.CUSTOM_NAME_VISIBLE, true);
        entity.offer(Keys.DISPLAY_NAME, Text.of("TEST"));

        return CommandResult.success();
    }
}