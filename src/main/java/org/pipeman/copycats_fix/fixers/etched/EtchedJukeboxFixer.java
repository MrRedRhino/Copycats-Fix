package org.pipeman.copycats_fix.fixers.etched;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;
import org.pipeman.copycats_fix.fixers.util.CustomDataFixUtil;

import java.util.List;

public class EtchedJukeboxFixer extends DataFix {
    public EtchedJukeboxFixer(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> blockEntityType = this.getInputSchema().getType(References.BLOCK_ENTITY);

        return writeFixAndRead(
                "Etched Jukebox Fix",
                blockEntityType,
                blockEntityType,
                dynamic -> {
                    String id = dynamic.get("id").asString("");

                    if (id.equals("etched:album_jukebox")) {
                        List<Dynamic<?>> items = dynamic.get("Items").asList(CustomDataFixUtil::fixItem);
                        return dynamic.set("Items", dynamic.createList(items.stream()));
                    }

                    return dynamic;
                }
        );
    }

    public static <T> Dynamic<T> fixJukebox(Dynamic<T> dynamic) {
        List<Dynamic<?>> items = dynamic.get("Items").asList(CustomDataFixUtil::fixItem);
        return dynamic.set("Items", dynamic.createList(items.stream()));
    }
}
