package org.pipeman.copycats_fix.fixers.create;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;
import org.pipeman.copycats_fix.fixers.util.CustomDataFixUtil;

import java.util.List;

public class StockTickerFix extends DataFix {
    public StockTickerFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> blockEntityType = this.getInputSchema().getType(References.BLOCK_ENTITY);

        return this.writeFixAndRead(
                "Exposure ItemStack Componentization",
                blockEntityType,
                blockEntityType,
                dynamic -> {
                    String id = dynamic.get("id").asString("");
                    if (id.equals("create:stock_ticker")) {
                        return fixStockTicker(dynamic);
                    }

                    return dynamic;
                }
        );
    }

    private Dynamic<?> fixStockTicker(Dynamic<?> dynamic) {
        List<? extends Dynamic<?>> categories = dynamic.get("Categories").asList(CustomDataFixUtil::fixItem);
        return dynamic.set("Categories", dynamic.createList(categories.stream()));
    }
}
