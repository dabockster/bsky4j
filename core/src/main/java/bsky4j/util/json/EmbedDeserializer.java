package bsky4j.util.json;

import bsky4j.BlueskyTypes;
import bsky4j.model.bsky.embed.EmbedExternal;
import bsky4j.model.bsky.embed.EmbedImages;
import bsky4j.model.bsky.embed.EmbedRecord;
import bsky4j.model.bsky.embed.EmbedRecordWithMedia;
import bsky4j.model.bsky.embed.EmbedUnion;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class EmbedDeserializer implements JsonDeserializer<EmbedUnion> {

    private static final Map<String, TypeToken<? extends EmbedUnion>> TYPE_MAP = new HashMap<>();

    static {
        TYPE_MAP.put(BlueskyTypes.EmbedImages, new TypeToken<EmbedImages>() {});
        TYPE_MAP.put(BlueskyTypes.EmbedExternal, new TypeToken<EmbedExternal>() {});
        TYPE_MAP.put(BlueskyTypes.EmbedRecord, new TypeToken<EmbedRecord>() {});
        TYPE_MAP.put(BlueskyTypes.EmbedRecordWithMedia, new TypeToken<EmbedRecordWithMedia>() {});
    }

    @Override
    public EmbedUnion deserialize(
            JsonElement json,
            Type typeOfT,
            JsonDeserializationContext context
    ) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        JsonElement type = obj.get("$type");

        if (type != null) {
            TypeToken<? extends EmbedUnion> typeToken = TYPE_MAP.get(type.getAsString());
            if (typeToken != null) {
                return context.deserialize(obj, typeToken.getType());
            }
        }
        return null;
    }
}
