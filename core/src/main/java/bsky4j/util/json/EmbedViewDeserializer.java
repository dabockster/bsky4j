package bsky4j.util.json;

import bsky4j.model.bsky.embed.EmbedExternalView;
import bsky4j.model.bsky.embed.EmbedImagesView;
import bsky4j.model.bsky.embed.EmbedRecordView;
import bsky4j.model.bsky.embed.EmbedRecordWithMediaView;
import bsky4j.model.bsky.embed.EmbedViewUnion;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class EmbedViewDeserializer implements JsonDeserializer<EmbedViewUnion> {

    private static final Map<String, TypeToken<? extends EmbedViewUnion>> TYPE_MAP = new HashMap<>();

    static {
        TYPE_MAP.put(EmbedImagesView.TYPE, new TypeToken<EmbedImagesView>() {});
        TYPE_MAP.put(EmbedExternalView.TYPE, new TypeToken<EmbedExternalView>() {});
        TYPE_MAP.put(EmbedRecordView.TYPE, new TypeToken<EmbedRecordView>() {});
        TYPE_MAP.put(EmbedRecordWithMediaView.TYPE, new TypeToken<EmbedRecordWithMediaView>() {});
    }

    @Override
    public EmbedViewUnion deserialize(
            JsonElement json,
            Type typeOfT,
            JsonDeserializationContext context
    ) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        JsonElement type = obj.get("$type");

        if (type != null) {
            TypeToken<? extends EmbedViewUnion> typeToken = TYPE_MAP.get(type.getAsString());
            if (typeToken != null) {
                return context.deserialize(obj, typeToken.getType());
            }
        }
        return null;
    }
}
