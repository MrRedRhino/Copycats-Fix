package org.pipeman.copycats_fix;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ModdedItemComponentizationFix extends DataFix {
    private static final DateTimeFormatter LEGACY_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");

    public ModdedItemComponentizationFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> itemStackType = this.getInputSchema().getType(References.ITEM_STACK);

        return this.writeFixAndRead(
                "Modded ItemStack Componentization",
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
        Optional<? extends Dynamic<?>> customDataOptional = dynamic.get("components").get("minecraft:custom_data").result();
        if (customDataOptional.isEmpty()) return dynamic;
        Dynamic<?> customData = customDataOptional.get();

        String type = customData.get("Type").asString("black_and_white");

        Dynamic<?> pos = customData.createList(customData.get("Pos").orElseEmptyMap().asStream()
                .map(element -> customData.createDouble(element.asDouble(0))));

        // Old saves never recorded camera pitch/yaw/shutter speed; fall back to neutral defaults.
        Dynamic<?> extraData = customData.emptyMap()
                .set("focal_length", customData.get("FocalLength").orElseEmptyMap())
                .set("light_level", customData.get("LightLevel").orElseEmptyMap())
                .set("pos", pos)
                .set("biome", customData.get("Biome").orElseEmptyMap())
                .set("weather", customData.get("Weather").orElseEmptyMap())
                .set("pitch", customData.createFloat(0.0f))
                .set("yaw", customData.createFloat(0.0f))
                .set("shutter_speed", customData.createString("1/60"))
                .set("day_time", customData.get("DayTime").orElseEmptyMap())
                .set("dimension", customData.get("Dimension").orElseEmptyMap())
                .set("timestamp", customData.createLong(parseLegacyTimestamp(customData.get("Timestamp").asString(""))));

        Dynamic<?> photographer = customData.emptyMap()
                .set("name", customData.get("Photographer").orElseEmptyMap())
                .set("uuid", customData.get("PhotographerId").orElseEmptyMap());

        Dynamic<?> photographFrame = customData.emptyMap()
                .set("identifier", customData.get("Id").orElseEmptyMap())
                .set("extra_data", extraData)
                .set("photographer", photographer)
                .set("type", customData.createString(type));

        Dynamic<?> components = dynamic.get("components").orElseEmptyMap()
                .remove("minecraft:custom_data")
                .set("exposure:photograph_frame", photographFrame)
                .set("exposure:photograph_type", customData.createString(type));

        return dynamic.set("components", components);
    }

    private static Dynamic<?> fixAlbum(Dynamic<?> dynamic) {
        Optional<? extends Dynamic<?>> customDataOptional = dynamic.get("components").get("minecraft:custom_data").result();
        if (customDataOptional.isEmpty()) return dynamic;
        Dynamic<?> customData = customDataOptional.get();

        Dynamic<?> albumContent = customData.get("album_content").orElseEmptyMap();
        Dynamic<?> pages = albumContent.createList(albumContent.get("pages").orElseEmptyList().asStream()
                .map(ModdedItemComponentizationFix::fixAlbumPage));

        Dynamic<?> components = dynamic.get("components").orElseEmptyMap()
                .remove("minecraft:custom_data")
                .set("exposure:album_content", albumContent.set("pages", pages));

        return dynamic.set("components", components);
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

        Dynamic<?> components = fixed.get("components").orElseEmptyMap();
        String type = components.get("exposure:photograph_type").asString("black_and_white");
        Dynamic<?> frame = components.get("exposure:photograph_frame").orElseEmptyMap()
                .set("type", components.createString(type));

        return fixed.set("components", components.set("exposure:photograph_frame", frame));
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

    // Legacy timestamps carried no timezone info, so this assumes UTC.
    private static long parseLegacyTimestamp(String timestamp) {
        if (timestamp.isEmpty()) return 0L;
        return LocalDateTime.parse(timestamp, LEGACY_TIMESTAMP_FORMAT).toEpochSecond(ZoneOffset.UTC);
    }
}
