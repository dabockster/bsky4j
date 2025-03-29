package bsky4j.util.json;

import bsky4j.model.bsky.actor.ActorProfile;
import bsky4j.model.bsky.feed.FeedLike;
import bsky4j.model.bsky.feed.FeedPost;
import bsky4j.model.bsky.feed.FeedRepost;
import bsky4j.model.bsky.graph.GraphBlock;
import bsky4j.model.bsky.graph.GraphFollow;
import bsky4j.model.share.RecordUnion;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class RecordDeserializer implements JsonDeserializer<RecordUnion> {

    private static final Map<String, TypeToken<? extends RecordUnion>> TYPE_MAP = new HashMap<>();

    static {
        TYPE_MAP.put(ActorProfile.TYPE, new TypeToken<ActorProfile>() {});
        TYPE_MAP.put(FeedPost.TYPE, new TypeToken<FeedPost>() {});
        TYPE_MAP.put(FeedLike.TYPE, new TypeToken<FeedLike>() {});
        TYPE_MAP.put(FeedRepost.TYPE, new TypeToken<FeedRepost>() {});
        TYPE_MAP.put(GraphFollow.TYPE, new TypeToken<GraphFollow>() {});
        TYPE_MAP.put(GraphBlock.TYPE, new TypeToken<GraphBlock>() {});
    }

    @Override
    public RecordUnion deserialize(
            JsonElement json,
            Type typeOfT,
            JsonDeserializationContext context
    ) throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();
        JsonElement type = obj.get("$type");

        if (type != null) {
            TypeToken<? extends RecordUnion> typeToken = TYPE_MAP.get(type.getAsString());
            if (typeToken != null) {
                return context.deserialize(obj, typeToken.getType());
            }
        }
        return null;
    }
}
