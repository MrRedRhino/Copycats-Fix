package org.pipeman.copycats_fix.fixers.exposure;

import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;

public class PhotographFrameFix {
    public static <T> Dynamic<T> fixFrame(Dynamic<T> dynamic) {
        int newVersion = SharedConstants.getCurrentVersion().getDataVersion().getVersion();

        Dynamic<?> fixedItem = DataFixers
                .getDataFixer()
                .update(References.ITEM_STACK, dynamic.get("Item").orElseEmptyMap(), 0, newVersion);

        return dynamic.set("Item", fixedItem);
    }
}
