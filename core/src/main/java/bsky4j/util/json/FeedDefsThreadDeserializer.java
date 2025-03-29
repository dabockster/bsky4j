package bsky4j.util.json;

import bsky4j.model.bsky.feed.FeedDefsNotFoundPost;
import bsky4j.model.bsky.feed.FeedDefsThreadUnion;
import bsky4j.model.bsky.feed.FeedDefsThreadViewPost;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class FeedDefsThreadDeserializer implements JsonDeserializer<FeedDefsThreadUnion> {

    private static final Map<String, TypeToken<? extends FeedDefsThreadUnion>> TYPE_MAP = new HashMap<>();

    static {
        TYPE_MAP.put(FeedDefsThreadViewPost.TYPE, new TypeToken<FeedDefsThreadViewPost>() {});
        TYPE_MAP.put(FeedDefsNotFoundPost.TYPE, new TypeToken<FeedDefsNotFoundPost>() {});
    }

    @Override
    public FeedDefsThreadUnion deserialize(
            JsonElement json,
            Type typeOfT,
            JsonDeserializationContext context
    ) throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();
        JsonElement type = obj.get("$type");

        if (type != null) {
            TypeToken<? extends FeedDefsThreadUnion> typeToken = TYPE_MAP.get(type.getAsString());
            if (typeToken != null) {
                return context.deserialize(obj, typeToken.getType());
            }
        }

        return null;
    }
}
