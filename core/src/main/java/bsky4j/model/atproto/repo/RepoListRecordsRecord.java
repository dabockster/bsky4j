package bsky4j.model.atproto.repo;

import bsky4j.model.share.RecordUnion;
import bsky4j.util.cache.CacheManager;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;
import java.util.Map;

/**
 * Optimized record storage with improved performance and memory efficiency.
 */
public class RepoListRecordsRecord {
    private static final Logger LOGGER = Logger.getLogger(RepoListRecordsRecord.class.getName());
    private static final String CACHE_KEY_PREFIX = "record_";
    
    private final String uri;
    private final String cid;
    private final AtomicReference<RecordUnion> valueRef;
    private final CacheManager cache;
    
    public RepoListRecordsRecord(String uri, String cid, RecordUnion value) {
        this.uri = uri;
        this.cid = cid;
        this.valueRef = new AtomicReference<>(value);
        this.cache = CacheManager.getInstance();
        
        // Cache the record
        cache.put(getCacheKey(), this, 5, TimeUnit.MINUTES);
    }
    
    private String getCacheKey() {
        return CACHE_KEY_PREFIX + uri;
    }
    
    public String getUri() {
        return uri;
    }
    
    public String getCid() {
        return cid;
    }
    
    public RecordUnion getValue() {
        RecordUnion value = valueRef.get();
        if (value == null) {
            // Try to get from cache if local value is null
            RepoListRecordsRecord cachedRecord = (RepoListRecordsRecord) cache.get(getCacheKey());
            if (cachedRecord != null) {
                value = cachedRecord.valueRef.get();
            }
        }
        return value;
    }
    
    public void setValue(RecordUnion value) {
        RecordUnion oldValue = valueRef.getAndSet(value);
        if (oldValue != value) {
            // Update cache if value changed
            cache.put(getCacheKey(), this, 5, TimeUnit.MINUTES);
        }
    }
    
    /**
     * Clears the cached value for this record.
     */
    public void clearCache() {
        cache.remove(getCacheKey());
    }
    
    /**
     * Invalidates the cache for this record type.
     */
    public static void invalidateCache(String uri) {
        CacheManager.getInstance().remove(CACHE_KEY_PREFIX + uri);
    }
    
    /**
     * Gets a record from cache if available.
     * 
     * @param uri The record URI
     * @return The cached record, or null if not found
     */
    public static RepoListRecordsRecord getFromCache(String uri) {
        return (RepoListRecordsRecord) CacheManager.getInstance().get(CACHE_KEY_PREFIX + uri);
    }
    
    /**
     * Gets statistics about record caching.
     * 
     * @return Map containing cache statistics
     */
    public static Map<String, Object> getCacheStatistics() {
        return CacheManager.getInstance().getStatistics();
    }
}
