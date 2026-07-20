package org.pipeman.copycats_fix;

import com.mojang.datafixers.DSL;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.pipeman.copycats_fix.fixers.etched.EtchedJukeboxFixer;
import org.pipeman.copycats_fix.fixers.exposure.PhotographFrameFix;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Function;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CopycatsFix.MODID)
public class CopycatsFix {
    public static final String MODID = "copycats_fix";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final List<MixinFixer> MIXIN_FIXERS = List.of(
            new MixinFixer("etched:album_jukebox", References.BLOCK_ENTITY, EtchedJukeboxFixer::fixJukebox),

            new MixinFixer("create:funnel", References.BLOCK_ENTITY, null),
            new MixinFixer("create:basin", References.BLOCK_ENTITY, null),
            new MixinFixer("create:depot", References.BLOCK_ENTITY, null), // Item
            new MixinFixer("create:chute", References.BLOCK_ENTITY, null), // Item
            new MixinFixer("create:smart_chute", References.BLOCK_ENTITY, null), // Item, Filter
            new MixinFixer("create:smart_chute", References.BLOCK_ENTITY, null), // Item, Filter
            new MixinFixer("create:smart_fluid_pipe", References.BLOCK_ENTITY, null), // Filter
            new MixinFixer("create:smart_fluid_pipe", References.BLOCK_ENTITY, null), // Filter

            new MixinFixer("exposure:photograph_frame", References.ENTITY, PhotographFrameFix::fixFrame),
            new MixinFixer("exposure:glass_photograph_frame", References.ENTITY, PhotographFrameFix::fixFrame)
            // deployer
    );

    public CopycatsFix(IEventBus modEventBus, ModContainer modContainer) {

    }

    public record MixinFixer(String id, DSL.TypeReference reference, Function<Dynamic<?>, Dynamic<?>> fixer) {
    }
}
