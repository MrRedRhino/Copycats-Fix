package org.pipeman.copycats_fix.fixers.exposure;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;
import org.pipeman.copycats_fix.fixers.util.CustomDataFixUtil;

public class ExposureItemComponentizationFix extends DataFix {

    public ExposureItemComponentizationFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> itemStackType = this.getInputSchema().getType(References.ITEM_STACK);

        return this.writeFixAndRead(
                "Exposure ItemStack Componentization",
                itemStackType,
                itemStackType,
                dynamic -> {
                    String id = dynamic.get("id").asString("");

                    if (id.equals("exposure:photograph")) {
                        return fixPhotograph(dynamic);
                    }

                    if (id.equals("exposure:album")) {
                        return fixAlbum(dynamic);
                    }

                    return dynamic;
                }
        );
    }

    private static Dynamic<?> fixPhotograph(Dynamic<?> dynamic) {
        Dynamic<?> tag = dynamic.get("tag").orElseEmptyMap();

        if (tag.get("photograph_frame").result().isEmpty()) {
            String legacyId = tag.get("Id").asString(null);
            if (legacyId != null) {
                Dynamic<?> photographFrame = tag.emptyMap()
                        .set("identifier", tag.createString(legacyId));
                tag = tag.set("photograph_frame", photographFrame);
                dynamic = dynamic.set("tag", tag);
            }
        }

        return CustomDataFixUtil.withCustomData(dynamic, (customData, components) -> {
            return components
                    .set("exposure:photograph_frame", customData.get("photograph_frame").orElseEmptyMap())
                    .set("exposure:photograph_type", customData.get("photograph_type").orElseEmptyMap());
        });
    }

    private static Dynamic<?> fixAlbum(Dynamic<?> dynamic) {
        return CustomDataFixUtil.withCustomData(dynamic, (customData, components) -> {
            Dynamic<?> albumContent = customData.get("album_content").orElseEmptyMap();
            Dynamic<?> pages = albumContent.createList(albumContent.get("pages").orElseEmptyList().asStream()
                    .map(ExposureItemComponentizationFix::fixAlbumPage));

            return components.set("exposure:album_content", albumContent.set("pages", pages));
        });
    }

    private static Dynamic<?> fixAlbumPage(Dynamic<?> page) {
        if (page.get("photograph").result().isEmpty()) return page;

        Dynamic<?> photograph = fixNestedPhotograph(page.get("photograph").orElseEmptyMap());
        return page.set("photograph", photograph);
    }

    // Nested photographs live inside the album's own custom data, so unlike top-level item
    // stacks they're never touched by vanilla's Count/tag componentization - fix both here.
    // They also only ever used the mod's own "photograph_type"/"photograph_frame" tag keys,
    // not the "minecraft:custom_data" wrapping vanilla applies to top-level modded item stacks.
    private static Dynamic<?> fixNestedPhotograph(Dynamic<?> photograph) {
        Dynamic<?> fixed = fixLegacyTag(photograph);

        if (photograph.get("Count").result().isPresent()) {
            int count = photograph.get("Count").asInt(1);
            fixed = fixed.set("count", photograph.createInt(count)).remove("Count");
        }

        return fixed;
    }

    // Renames "tag" keys into a "components" map (prefixed with "exposure:"). A no-op if "tag"
    // is already gone. Doesn't touch Count/count - vanilla's own componentization already
    // handles that for real, top-level item stacks.
    private static Dynamic<?> fixLegacyTag(Dynamic<?> dynamic) {
        if (dynamic.get("tag").result().isEmpty()) return dynamic;

        Dynamic<?> components = renameTagKeysToComponents(dynamic.get("tag").orElseEmptyMap());
        return dynamic.set("components", components).remove("tag");
    }

    private static Dynamic<?> renameTagKeysToComponents(Dynamic<?> tag) {
        return tag.updateMapValues(pair -> Pair.of(
                pair.getFirst().createString("exposure:" + pair.getFirst().asString("")),
                pair.getSecond()
        ));
    }
}
