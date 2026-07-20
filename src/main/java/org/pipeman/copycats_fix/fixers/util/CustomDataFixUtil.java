package org.pipeman.copycats_fix.fixers.util;

import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;

import java.util.Optional;
import java.util.function.BiFunction;

public class CustomDataFixUtil {

    // Unwraps "minecraft:custom_data" (a no-op if it's already gone) and hands the caller
    // the custom data alongside a "components" map with that key removed, ready to have the
    // mod's own componentized keys set onto it.
    public static Dynamic<?> withCustomData(Dynamic<?> dynamic, BiFunction<Dynamic<?>, Dynamic<?>, Dynamic<?>> apply) {
        Optional<? extends Dynamic<?>> customDataOptional = dynamic.get("components").get("minecraft:custom_data").result();
        if (customDataOptional.isEmpty()) return dynamic;
        Dynamic<?> customData = customDataOptional.get();

        Dynamic<?> componentsBase = dynamic.get("components").orElseEmptyMap().remove("minecraft:custom_data");
        return dynamic.set("components", apply.apply(customData, componentsBase));
    }

    public static <T> Dynamic<T> fixItem(Dynamic<T> input) {
        return DataFixers.getDataFixer().update(References.ITEM_STACK, input, 0, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
    }
}