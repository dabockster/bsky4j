package bsky4j.util.json;

import bsky4j.model.bsky.embed.EmbedRecordViewArticle;
import bsky4j.model.bsky.embed.EmbedRecordViewExternal;
import bsky4j.model.bsky.embed.EmbedRecordViewImage;
import bsky4j.model.bsky.embed.EmbedRecordViewRecord;
import bsky4j.model.bsky.embed.EmbedRecordViewUnion;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

/**
 * Optimized deserializer for EmbedRecordViewUnion with improved performance and error handling.
 */
public class EmbedRecordViewDeserializer extends UnionDeserializer<EmbedRecordViewUnion> {
    private static final Map<String, TypeToken<? extends EmbedRecordViewUnion>> TYPES = new HashMap<>();
    
    static {
        TYPES.put(EmbedRecordViewArticle.TYPE, new TypeToken<EmbedRecordViewArticle>() {});
        TYPES.put(EmbedRecordViewExternal.TYPE, new TypeToken<EmbedRecordViewExternal>() {});
        TYPES.put(EmbedRecordViewImage.TYPE, new TypeToken<EmbedRecordViewImage>() {});
        TYPES.put(EmbedRecordViewRecord.TYPE, new TypeToken<EmbedRecordViewRecord>() {});
    }
    
    /**
     * Initializes the type map with supported types.
     */
    public EmbedRecordViewDeserializer() {
        initTypeMap(TYPES);
    }
}
