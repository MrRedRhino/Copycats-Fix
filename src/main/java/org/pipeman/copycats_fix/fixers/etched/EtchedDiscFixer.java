package org.pipeman.copycats_fix.fixers.etched;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;
import org.pipeman.copycats_fix.fixers.util.CustomDataFixUtil;

import java.util.List;
import java.util.stream.Stream;

public class EtchedDiscFixer extends DataFix {
    private static final List<String> PATTERNS = List.of("flat", "cross", "eye", "parallel", "star", "gold");

    public EtchedDiscFixer(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> itemStackType = this.getInputSchema().getType(References.ITEM_STACK);

        return this.writeFixAndRead(
                "Etched Disc Componentization",
                itemStackType,
                itemStackType,
                dynamic -> {
                    String id = dynamic.get("id").asString("");

                    if (id.equals("etched:etched_music_disc")) {
                        return fixEtchedMusicDisc(dynamic);
                    }

                    return dynamic;
                }
        );
    }

    private static Dynamic<?> fixEtchedMusicDisc(Dynamic<?> dynamic) {
        return CustomDataFixUtil.withCustomData(dynamic, (customData, components) -> components
                .set("etched:disc_appearance", fixDiscAppearance(customData))
                .set("etched:music", customData.createList(Stream.of(fixMusic(customData.get("Music").orElseEmptyMap())))));
    }

    // Title used to be a JSON-serialized chat Component; the new component just wants
    // its plain text. DataFixers run before registries exist, so Component.Serializer
    // (which needs a HolderLookup.Provider) isn't usable here - the JSON is walked by hand.
    private static Dynamic<?> fixMusic(Dynamic<?> music) {
        String title = CustomDataFixUtil.extractText(music.get("Title").asString(""));
        return music.set("Title", music.createString(title));
    }

    // Legacy colors were plain RGB ints; the new components expect ARGB, so the alpha
    // channel is forced on the same way the mod itself now does it: 0xFF000000 | rgb.
    private static Dynamic<?> fixDiscAppearance(Dynamic<?> customData) {
        Dynamic<?> labelColor = customData.get("LabelColor").orElseEmptyMap();
        int patternIndex = customData.get("Pattern").asInt(0);

        return customData
                .remove("DiscColor")
                .remove("LabelColor")
                .remove("Pattern")
                .remove("Music")
                .set("discColor", customData.createInt(toArgb(customData.get("DiscColor").asInt(0))))
                .set("labelPrimaryColor", customData.createInt(toArgb(labelColor.get("Primary").asInt(0))))
                .set("labelSecondaryColor", customData.createInt(toArgb(labelColor.get("Secondary").asInt(0))))
                .set("pattern", customData.createString(PATTERNS.get(Math.clamp(patternIndex, 0, PATTERNS.size() - 1))));
    }

    private static int toArgb(int rgb) {
        return 0xFF000000 | rgb;
    }
}