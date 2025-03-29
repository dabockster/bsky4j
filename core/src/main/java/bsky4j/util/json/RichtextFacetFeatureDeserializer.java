package bsky4j.util.json;

import bsky4j.model.bsky.richtext.RichtextFacetFeatureLink;
import bsky4j.model.bsky.richtext.RichtextFacetFeatureMention;
import bsky4j.model.bsky.richtext.RichtextFacetFeatureTag;
import bsky4j.model.bsky.richtext.RichtextFacetFeatureUnion;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

/**
 * Optimized deserializer for RichtextFacetFeatureUnion with improved performance and error handling.
 */
public class RichtextFacetFeatureDeserializer extends UnionDeserializer<RichtextFacetFeatureUnion> {
    private static final Map<String, TypeToken<? extends RichtextFacetFeatureUnion>> TYPES = new HashMap<>();
    
    static {
        TYPES.put(RichtextFacetFeatureLink.TYPE, new TypeToken<RichtextFacetFeatureLink>() {});
        TYPES.put(RichtextFacetFeatureMention.TYPE, new TypeToken<RichtextFacetFeatureMention>() {});
        TYPES.put(RichtextFacetFeatureTag.TYPE, new TypeToken<RichtextFacetFeatureTag>() {});
    }
    
    /**
     * Initializes the type map with supported types.
     */
    public RichtextFacetFeatureDeserializer() {
        initTypeMap(TYPES);
    }
}
