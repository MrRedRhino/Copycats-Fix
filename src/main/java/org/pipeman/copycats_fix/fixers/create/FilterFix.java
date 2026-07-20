package org.pipeman.copycats_fix.fixers.create;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;
import org.pipeman.copycats_fix.fixers.util.CustomDataFixUtil;

import java.util.List;
import java.util.Map;

public class FilterFix extends DataFix {
    private static final String[] WHITELIST_MODES = {"whitelist_disj", "whitelist_conj", "blacklist"};
    private static final String[] DYE = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
            "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
    private static final Map<String, String> ATTRIBUTE_TAG_NAMES = Map.of(
            "create:added_by", "modId",
            "create:book_author", "author",
            "create:book_copy", "generation", // int
            "create:has_color", "color", // int, needs to be converted to lowercase name
            "create:has_enchant", "enchantId",
            "create:has_fluid", "fluidId",
            "create:in_item_group", "group",
            "create:in_tag", "tag",
            "create:has_name", "name",
            "create:shulker_fill_level", "level"
    );

    public FilterFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    private static Dynamic<?> fixFilter(Dynamic<?> customData, Dynamic<?> components) {

        List<Dynamic<?>> attributes = customData.get("MatchedAttributes")
                .asList(FilterFix::fixMatchedAttribute);

        return components
                .set("create:attribute_filter_whitelist_mode", components.createString(WHITELIST_MODES[customData.get("WhitelistMode").asInt(0)]))
                .set("create:attribute_filter_matched_attributes", components.createList(attributes.stream()));
    }

    private static Dynamic<?> fixMatchedAttribute(Dynamic<?> attribute) {
        String attributeId = attribute.get("attributeId").asString("");

        Dynamic<?> type = attribute.emptyMap()
                .set("type", attribute.createString(attributeId))
                .set("value", fixAttributeValue(attribute, attributeId));

        return attribute.emptyMap()
                .set("attribute", type)
                .set("inverted", attribute.get("Inverted").orElseEmptyMap());
    }

    // Singleton attributes (e.g. "placeable", "consumable") never had a tag to begin with,
    // so they fall through to an empty value. "has_color" stored a DyeColor id, which the
    // new format wants as the color's name instead.
    private static Dynamic<?> fixAttributeValue(Dynamic<?> attribute, String attributeId) {
        String tagName = ATTRIBUTE_TAG_NAMES.get(attributeId);
        if (tagName == null) return attribute.emptyMap();

        if (attributeId.equals("create:has_color")) {
            int colorId = attribute.get(tagName).asInt(0);
            return attribute.createString(DYE[Math.floorMod(colorId, DYE.length)]);
        }

        return attribute.get(tagName).orElseEmptyMap();
    }

    private static Dynamic<?> fixPackageFilter(Dynamic<?> customData, Dynamic<?> components) {
        Dynamic<?> value = components.createString(customData.get("Address").asString(""));
        return components.set("create:package_address", value);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> itemStackType = this.getInputSchema().getType(References.ITEM_STACK);

        return this.writeFixAndRead(
                "Attribute Filter",
                itemStackType,
                itemStackType,
                dynamic -> {
                    String id = dynamic.get("id").asString("");

                    if (id.equals("create:attribute_filter")) {
                        return CustomDataFixUtil.withCustomData(dynamic, FilterFix::fixFilter);
                    }

                    if (id.equals("create:package_filter")) {
                        return CustomDataFixUtil.withCustomData(dynamic, FilterFix::fixPackageFilter);
                    }

                    return dynamic;
                }
        );
    }
}
