package org.pipeman.copycats_fix.fixers.create;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.util.datafix.fixes.References;
import org.pipeman.copycats_fix.fixers.util.CustomDataFixUtil;

public class FilterBlockSlotFix extends DataFix {
    public FilterBlockSlotFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> itemStackType = this.getInputSchema().getType(References.BLOCK_ENTITY);

        return this.writeFixAndRead(
                "Block Filter Slot",
                itemStackType,
                itemStackType,
                dynamic -> {
                    if (dynamic.get("Filter").result().isPresent()) {
                        return dynamic.set("Filter", CustomDataFixUtil.fixItem(dynamic.get("Filter").orElseEmptyMap()));
                    }

                    if (dynamic.get("Item").result().isPresent()) {
                        return dynamic.set("Item", CustomDataFixUtil.fixItem(dynamic.get("Item").orElseEmptyMap()));
                    }

                    return dynamic;
                }
        );
    }
}
