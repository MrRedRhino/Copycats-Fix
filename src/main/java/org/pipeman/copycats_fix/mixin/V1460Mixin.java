package org.pipeman.copycats_fix.mixin;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.V1460;
import org.pipeman.copycats_fix.CopycatsFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(V1460.class)
public class V1460Mixin {
    @Inject(
            at = {@At("RETURN")},
            method = {"registerEntities"}
    )
    private void create$registerEntitiesToBeFixed(Schema schema, CallbackInfoReturnable<Map<String, Supplier<TypeTemplate>>> ci) {
        Map<String, Supplier<TypeTemplate>> map = ci.getReturnValue();

        CopycatsFix.MIXIN_FIXERS.stream()
                .filter(f -> f.reference() == References.ENTITY)
                .map(CopycatsFix.MixinFixer::id)
                .forEach(id -> schema.registerSimple(map, id));
    }

    @Inject(
            at = {@At("RETURN")},
            method = {"registerBlockEntities"}
    )
    private void create$registerBlockEntitiesToBeFixed(Schema schema, CallbackInfoReturnable<Map<String, Supplier<TypeTemplate>>> ci) {
        Map<String, Supplier<TypeTemplate>> map = ci.getReturnValue();

        CopycatsFix.MIXIN_FIXERS.stream()
                .filter(f -> f.reference() == References.BLOCK_ENTITY)
                .map(CopycatsFix.MixinFixer::id)
                .forEach(id -> schema.registerSimple(map, id));
    }
}
