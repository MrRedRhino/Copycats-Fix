package org.pipeman.copycats_fix.fixers.create;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;
import org.pipeman.copycats_fix.fixers.util.CustomDataFixUtil;

import java.util.List;

public class ClipboardFix extends DataFix {
    private static final List<String> CLIPBOARD_TYPES = List.of("empty", "written", "editing");

    public ClipboardFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> itemStackType = this.getInputSchema().getType(References.ITEM_STACK);

        return writeFixAndRead(
                "Clipboard Fix",
                itemStackType,
                itemStackType,
                dynamic -> {
                    String id = dynamic.get("id").asString("");

                    if (id.equals("create:clipboard")) {
                        return CustomDataFixUtil.withCustomData(dynamic, (customData, components) -> {
                            List<? extends Dynamic<?>> pages = components.get("create:clipboard_pages").asList(p -> {
                                List<Dynamic<?>> entries = p.get("Entries").asList(e -> e.emptyMap()
                                        .set("checked", e.get("Checked").orElseEmptyMap())
                                        .set("icon", e.emptyMap())
                                        .set("item_amount", e.createInt(0))
                                        .set("text", e.createString(CustomDataFixUtil.extractText(e.get("Text").asString("{}")))));

                                return p.createList(entries.stream());
                            });

                            return components
                                    .set("create:clipboard_content", components.emptyMap()
                                            .set("previously_opened_page", customData.get("PreviouslyOpenedPage").orElseEmptyMap())
                                            .set("read_only", components.createBoolean(false))
                                            .set("type", components.get("create:clipboard_type").orElseEmptyMap())
                                            .set("pages", components.createList(pages.stream()))
                                    );
                        });
                    }

                    return dynamic;
                }
        );
    }
}
