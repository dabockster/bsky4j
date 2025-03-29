package bsky4j.util.json;

import bsky4j.model.bsky.actor.ActorDefsAdultContentPref;
import bsky4j.model.bsky.actor.ActorDefsContentLabelPref;
import bsky4j.model.bsky.actor.ActorDefsPreferencesUnion;
import bsky4j.model.bsky.actor.ActorDefsSavedFeedsPref;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

/**
 * Optimized deserializer for ActorDefsPreferencesUnion with improved performance and error handling.
 */
public class ActorDefsPreferencesDeserializer extends UnionDeserializer<ActorDefsPreferencesUnion> {
    private static final Map<String, TypeToken<? extends ActorDefsPreferencesUnion>> TYPES = new HashMap<>();
    
    static {
        TYPES.put(ActorDefsAdultContentPref.TYPE, new TypeToken<ActorDefsAdultContentPref>() {});
        TYPES.put(ActorDefsContentLabelPref.TYPE, new TypeToken<ActorDefsContentLabelPref>() {});
        TYPES.put(ActorDefsSavedFeedsPref.TYPE, new TypeToken<ActorDefsSavedFeedsPref>() {});
    }
    
    /**
     * Initializes the type map with supported types.
     */
    public ActorDefsPreferencesDeserializer() {
        initTypeMap(TYPES);
    }
}
