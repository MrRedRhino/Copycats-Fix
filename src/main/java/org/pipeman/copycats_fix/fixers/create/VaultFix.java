package org.pipeman.copycats_fix.fixers.create;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;

import java.util.List;

public class VaultFix extends DataFix {
    public VaultFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> blockEntityType = this.getInputSchema().getType(References.BLOCK_ENTITY);

        return writeFixAndRead(
                "Vault Fix",
                blockEntityType,
                blockEntityType,
                dynamic -> {
                    String id = dynamic.get("id").asString("");

                    if (id.equals("create:item_vault")) {
                        Dynamic<?> inventory = dynamic.get("Inventory").orElseEmptyMap();

                        List<Dynamic<?>> items = inventory.get("Items").asList(dynamic1 -> dynamic1);
                        items.replaceAll(VaultFix::fixItem);

                        return dynamic.set("Inventory", inventory.set("Items", dynamic.createList(items.stream())));
                    }

                    return dynamic;
                }
        ); // DataFixers.getDataFixer().update(References.ITEM_STACK, null, 0, SharedConstants.getCurrentVersion().getDataVersion().getVersion())
    }

    private static <T> Dynamic<T> fixItem(Dynamic<T> input) {
        return DataFixers.getDataFixer().update(References.ITEM_STACK, input, 0, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
    }
}
