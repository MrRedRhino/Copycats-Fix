package org.pipeman.copycats_fix.mixin;

import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.DataFixers;
import org.pipeman.copycats_fix.fixers.create.VaultFix;
import org.pipeman.copycats_fix.fixers.etched.EtchedDiscFixer;
import org.pipeman.copycats_fix.fixers.exposure.ExposureItemComponentizationFix;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiFunction;

@Mixin(DataFixers.class)
public class DataFixersMixin {
    @Shadow
    @Final
    private static BiFunction<Integer, Schema, Schema> SAME_NAMESPACED;

    @Inject(method = "addFixers", at = @At("TAIL"))
    private static void addFixers(DataFixerBuilder builder, CallbackInfo ci) {
        Schema schema = builder.addSchema(3818, 7, SAME_NAMESPACED);
        builder.addFixer(new ExposureItemComponentizationFix(schema));

        Schema schema2 = builder.addSchema(3818, 8, SAME_NAMESPACED);
        builder.addFixer(new VaultFix(schema2));

        Schema schema3 = builder.addSchema(3818, 9, SAME_NAMESPACED);
        builder.addFixer(new EtchedDiscFixer(schema3));
    }
}
