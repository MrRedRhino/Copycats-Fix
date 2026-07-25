package org.pipeman.copycats_fix;

import com.mojang.datafixers.DSL;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.pipeman.copycats_fix.fixers.etched.EtchedJukeboxFixer;
import org.pipeman.copycats_fix.fixers.exposure.PhotographFrameFix;

import java.util.List;
import java.util.function.Function;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CopycatsFix.MODID)
public class CopycatsFix {
    public static final String MODID = "copycats_fix";
    public static final List<MixinFixer> MIXIN_FIXERS = List.of(
            new MixinFixer("etched:album_jukebox", References.BLOCK_ENTITY, EtchedJukeboxFixer::fixJukebox),

            new MixinFixer("create:funnel", References.BLOCK_ENTITY, null),
            new MixinFixer("create:brass_funnel", References.BLOCK_ENTITY, null),
            new MixinFixer("create:basin", References.BLOCK_ENTITY, null),
            new MixinFixer("create:depot", References.BLOCK_ENTITY, null), // Item
            new MixinFixer("create:chute", References.BLOCK_ENTITY, null), // Item
            new MixinFixer("create:smart_chute", References.BLOCK_ENTITY, null), // Item, Filter
            new MixinFixer("create:smart_fluid_pipe", References.BLOCK_ENTITY, null), // Filter
            new MixinFixer("create:item_vault", References.BLOCK_ENTITY, null), // Filter
            new MixinFixer("create:attribute_filter", References.ITEM_STACK, null),
            new MixinFixer("create:tunnel", References.BLOCK_ENTITY, null), // Item, Filter
            new MixinFixer("create:brass_tunnel", References.BLOCK_ENTITY, null), // Item, Filter
            new MixinFixer("create:stock_ticker", References.BLOCK_ENTITY, null), // Item, Filter

            new MixinFixer("exposure:photograph_frame", References.ENTITY, PhotographFrameFix::fixFrame),
            new MixinFixer("exposure:glass_photograph_frame", References.ENTITY, PhotographFrameFix::fixFrame),
            // deployer
            new MixinFixer("create:super_glue", References.ENTITY, null), // to prevent the data fixer from freaking out
            new MixinFixer("connectiblechains:chain_knot", References.ENTITY, null) // same here
    );

    public CopycatsFix(IEventBus modEventBus, ModContainer modContainer) {

    }

    public record MixinFixer(String id, DSL.TypeReference reference, Function<Dynamic<?>, Dynamic<?>> fixer) {
    }
}
