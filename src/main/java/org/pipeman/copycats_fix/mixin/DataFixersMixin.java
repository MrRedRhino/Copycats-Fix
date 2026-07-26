package org.pipeman.copycats_fix.mixin;

import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.DataFixers;
import org.pipeman.copycats_fix.fixers.create.*;
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

        Schema schema4 = builder.addSchema(3818, 10, SAME_NAMESPACED);
        builder.addFixer(new FilterFix(schema4));

        Schema schema5 = builder.addSchema(3818, 11, SAME_NAMESPACED);
        builder.addFixer(new FilterBlockSlotFix(schema5));

        Schema schema6 = builder.addSchema(3818, 12, SAME_NAMESPACED);
        builder.addFixer(new ClipboardFix(schema6));

        Schema schema7 = builder.addSchema(3818, 13, SAME_NAMESPACED);
        builder.addFixer(new StockTickerFix(schema7));
    }
}
