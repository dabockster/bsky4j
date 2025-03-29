package bsky4j.util.json;

import bsky4j.model.bsky.actor.ActorProfile;
import bsky4j.model.bsky.feed.FeedLike;
import bsky4j.model.bsky.feed.FeedPost;
import bsky4j.model.bsky.feed.FeedRepost;
import bsky4j.model.bsky.graph.GraphBlock;
import bsky4j.model.bsky.graph.GraphFollow;
import bsky4j.model.share.RecordUnion;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

/**
 * Optimized deserializer for RecordUnion with improved performance and error handling.
 */
public class RecordDeserializer extends UnionDeserializer<RecordUnion> {
    private static final Map<String, TypeToken<? extends RecordUnion>> TYPES = new HashMap<>();
    private static final String[] SUPPORTED_TYPES = {
        ActorProfile.TYPE,
        FeedPost.TYPE,
        FeedLike.TYPE,
        FeedRepost.TYPE,
        GraphFollow.TYPE,
        GraphBlock.TYPE
    };
    
    static {
        TYPES.put(ActorProfile.TYPE, new TypeToken<ActorProfile>() {});
        TYPES.put(FeedPost.TYPE, new TypeToken<FeedPost>() {});
        TYPES.put(FeedLike.TYPE, new TypeToken<FeedLike>() {});
        TYPES.put(FeedRepost.TYPE, new TypeToken<FeedRepost>() {});
        TYPES.put(GraphFollow.TYPE, new TypeToken<GraphFollow>() {});
        TYPES.put(GraphBlock.TYPE, new TypeToken<GraphBlock>() {});
    }
    
    /**
     * Initializes the type map with supported types.
     */
    public RecordDeserializer() {
        initTypeMap(TYPES);
    }
    
    /**
     * Gets the list of supported record types.
     * @return Array of supported type names
     */
    public static String[] getSupportedTypes() {
        return SUPPORTED_TYPES;
    }
    
    /**
     * Checks if a record type is supported.
     * @param typeName The type name to check
     * @return true if the type is supported
     */
    public static boolean isSupportedType(String typeName) {
        return TYPES.containsKey(typeName);
    }
}
