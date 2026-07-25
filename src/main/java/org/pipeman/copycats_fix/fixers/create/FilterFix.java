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
import java.util.function.Function;

public class FilterFix extends DataFix {
    private static final String[] WHITELIST_MODES = {"whitelist_disj", "whitelist_conj", "blacklist"};
    private static final String[] DYE = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
            "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
    private static final Map<String, String> ATTRIBUTE_TAG_NAMES = Map.ofEntries(
            Map.entry("create:added_by", "modId"),
            Map.entry("create:book_author", "author"),
            Map.entry("create:book_copy", "generation"), // int
            Map.entry("create:has_color", "color"), // int, needs to be converted to lowercase name
            Map.entry("create:has_enchant", "enchantId"),
            Map.entry("create:has_fluid", "fluidId"),
            Map.entry("create:in_item_group", "group"),
            Map.entry("create:in_tag", "tag"),
            Map.entry("create:has_name", "name"),
            Map.entry("create:shulker_fill_level", "level"),

            Map.entry("legacy:added_by", "id"),
            Map.entry("legacy:has_enchant", "id"),
            Map.entry("legacy:shulker_fill_level", "id"),
            Map.entry("legacy:has_color", "id"),
            Map.entry("legacy:has_fluid", "id")
    );

    private static final Map<String, Function<Dynamic<?>, Dynamic<?>>> LEGACY_ATTRIBUTES = Map.of(
            "in_item_group", d -> fixAttributeValue(d, "create:in_item_group"),
            "added_by", d -> fixAttributeValue(d, "legacy:added_by"),
            "has_enchant", d -> fixAttributeValue(d, "legacy:has_enchant"),
            "shulker_fill_level", d -> fixAttributeValue(d, "legacy:shulker_fill_level"),
            "has_color", d -> fixAttributeValue(d, "legacy:has_color"),
            "has_fluid", d -> fixAttributeValue(d, "legacy:has_fluid"),
            "in_tag", d -> d.createString(d.get("space").asString("") + ":" + d.get("path").asString(""))
    );

    public FilterFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    private static Dynamic<?> fixFilter(Dynamic<?> customData, Dynamic<?> components) {
        List<Dynamic<?>> attributes = customData.get("MatchedAttributes")
                .asList(FilterFix::fixMatchedAttribute);

        return components
                .set("create:attribute_filter_whitelist_mode", components.createString(WHITELIST_MODES[customData.get("WhitelistMode").asInt(0)]))
                .set("create:attribute_filter_matched_attributes", components.createList(attributes.stream()))
                .set("create:filter_items", components.emptyList());
    }

    private static Dynamic<?> fixMatchedAttribute(Dynamic<?> attribute) {
        String attributeId = attribute.get("attributeId").asString(null);
        Dynamic<?> attributeValue;

        if (attributeId == null) {
            Map<String, Dynamic<?>> traits = attribute.get("standard_trait").asMap(d -> d.asString(""), d -> d);
            if (!traits.isEmpty()) {
                String trait = traits.keySet().iterator().next();
                attributeId = "create:" + trait.toLowerCase();
                attributeValue = fixAttributeValue(traits.get(trait), attributeId);
            } else {
                Map<String, Dynamic<?>> attributeMap = attribute.asMap(d -> d.asString(""), d -> d);

                Map.Entry<String, Dynamic<?>> filterKey = attributeMap.entrySet().stream()
                        .filter(e -> !e.getKey().equals("Inverted"))
                        .findFirst()
                        .orElseThrow();

                Function<Dynamic<?>, Dynamic<?>> legacyFixer = LEGACY_ATTRIBUTES.get(filterKey.getKey());
                if (legacyFixer != null) attributeValue = legacyFixer.apply(filterKey.getValue());
                else attributeValue = fixAttributeValue(attribute, "create:" + filterKey.getKey());
                attributeId = "create:" + filterKey.getKey();
            }
        } else {
            attributeValue = fixAttributeValue(attribute, attributeId);
        }

        Dynamic<?> type = attribute.emptyMap()
                .set("type", attribute.createString(attributeId))
                .set("value", attributeValue);

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

        if (attributeId.equals("create:has_color") || attributeId.equals("legacy:has_color")) {
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
