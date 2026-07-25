package org.pipeman.copycats_fix.fixers.exposure;

import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;

import java.util.Optional;

public class PhotographFrameFix {
    public static <T> Dynamic<T> fixFrame(Dynamic<T> dynamic) {
        int newVersion = SharedConstants.getCurrentVersion().getDataVersion().getVersion();

        Dynamic<?> item = dynamic.get("Item").orElseEmptyMap();
        Dynamic<?> tag = item.get("tag").orElseEmptyMap();

        if (tag.get("photograph_frame").result().isEmpty()) {
            String legacyId = tag.get("Id").asString(null);
            if (legacyId != null) {
                Dynamic<?> photographFrame = tag.emptyMap()
                        .set("identifier", tag.createString(legacyId));
                tag = tag.set("photograph_frame", photographFrame);
                item = item.set("tag", tag);
            }
        }

        Dynamic<?> fixedItem = DataFixers.getDataFixer()
                .update(References.ITEM_STACK, item, 0, newVersion);

        return dynamic.set("Item", (Dynamic<T>) fixedItem);
    }
}
