package org.pipeman.copycats_fix.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.BlockPosFormatAndRenamesFix;
import org.pipeman.copycats_fix.CopycatsFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BlockPosFormatAndRenamesFix.class)
public abstract class BlockPosFormatAndRenamesFixMixin extends DataFix {
    public BlockPosFormatAndRenamesFixMixin(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Inject(method = "makeRule", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0))
    private void create$addFixers(CallbackInfoReturnable<TypeRewriteRule> cir, @Local List<TypeRewriteRule> output) {
        for (CopycatsFix.MixinFixer fixer : CopycatsFix.MIXIN_FIXERS) {
            String id = fixer.id();
            DSL.TypeReference ref = fixer.reference();

            OpticFinder<?> opticfinder = DSL.namedChoice(id, this.getInputSchema().getChoiceType(ref, id));
            TypeRewriteRule rule = fixTypeEverywhereTyped("Exposure photograph frame fix",
                    getInputSchema().getType(ref),
                    typed -> typed.updateTyped(opticfinder, data ->
                            data.update(DSL.remainderFinder(), d -> fixer.fixer().apply(d))
                    )
            );
            output.add(rule);
        }
    }
}
