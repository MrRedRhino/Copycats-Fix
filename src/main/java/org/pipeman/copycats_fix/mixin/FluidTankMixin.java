package org.pipeman.copycats_fix.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidTank.class)
public class FluidTankMixin {
    @Inject(method = "readFromNBT", at = @At("HEAD"))
    public void fixFluidTag(HolderLookup.Provider lookupProvider, CompoundTag nbt, CallbackInfoReturnable<FluidTank> cir) {
        if (!nbt.contains("Fluid")) {
            String fluid = nbt.getCompound("Variant").getString("fluid");
            long amount = nbt.getLong("Amount") / 81;

            CompoundTag newTag = new CompoundTag();
            newTag.putInt("amount", (int) amount);
            newTag.putString("id", fluid);
            nbt.put("Fluid", newTag);
        }
    }
}
