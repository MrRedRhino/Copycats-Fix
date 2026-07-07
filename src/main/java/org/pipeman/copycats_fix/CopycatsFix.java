package org.pipeman.copycats_fix;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CopycatsFix.MODID)
public class CopycatsFix {
    public static final String MODID = "copycats_fix";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CopycatsFix(IEventBus modEventBus, ModContainer modContainer) {

    }
}
