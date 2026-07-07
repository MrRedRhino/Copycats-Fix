package org.pipeman.copycats_fix.mixin;

import com.copycatsplus.copycats.content.copycat.bytes.CopycatByteBlock;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(CopycatByteBlock.class)
public class CopycatByteBlockMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Shadow
    @Final
    public static Map<String, CopycatByteBlock.Byte> byteMap;

    @Inject(method = "getVectorFromProperty", at = @At(value = "HEAD"), cancellable = true)
    private static void get(BlockState state, String property, CallbackInfoReturnable<Vec3i> cir) {
        if (byteMap.get(property) == null) {
            CopycatByteBlock.Byte bite = byteMap.get("top_northeast");
            cir.setReturnValue( new Vec3i(bite.x() ? 1 : 0, bite.y() ? 1 : 0, bite.z() ? 1 : 0));
        }
    }
}
