package org.pipeman.copycats_fix.fixers.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;

import java.util.Optional;
import java.util.function.BiFunction;

public class CustomDataFixUtil {

    // Unwraps "minecraft:custom_data" (a no-op if it's already gone) and hands the caller
    // the custom data alongside a "components" map with that key removed, ready to have the
    // mod's own componentized keys set onto it.
    public static Dynamic<?> withCustomData(Dynamic<?> dynamic, BiFunction<Dynamic<?>, Dynamic<?>, Dynamic<?>> apply) {
        Optional<? extends Dynamic<?>> customDataOptional = dynamic.get("components").get("minecraft:custom_data").result();
        if (customDataOptional.isEmpty()) return dynamic;
        Dynamic<?> customData = customDataOptional.get();

        Dynamic<?> componentsBase = dynamic.get("components").orElseEmptyMap().remove("minecraft:custom_data");
        return dynamic.set("components", apply.apply(customData, componentsBase));
    }

    public static <T> Dynamic<T> fixItem(Dynamic<T> input) {
        return DataFixers.getDataFixer().update(References.ITEM_STACK, input, 3465, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
    }

    public static String extractText(String json) {
        try {
            return extractText(JsonParser.parseString(json));
        } catch (JsonSyntaxException e) {
            return json;
        }
    }

    public static String extractText(JsonElement element) {
        if (element.isJsonPrimitive()) return element.getAsString();

        if (element.isJsonArray()) {
            StringBuilder text = new StringBuilder();
            element.getAsJsonArray().forEach(child -> text.append(extractText(child)));
            return text.toString();
        }

        if (element.isJsonObject()) {
            StringBuilder text = new StringBuilder();
            if (element.getAsJsonObject().has("text")) text.append(element.getAsJsonObject().get("text").getAsString());
            if (element.getAsJsonObject().has("extra"))
                text.append(extractText(element.getAsJsonObject().get("extra")));
            return text.toString();
        }

        return "";
    }
}