package bsky4j.util.cache;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe cache manager with TTL support and statistics collection.
 */
public class CacheManager {
    private static final Logger LOGGER = Logger.getLogger(CacheManager.class.getName());
    private static final CacheManager INSTANCE = new CacheManager();
    
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    private final AtomicInteger hitCount = new AtomicInteger();
    private final AtomicInteger missCount = new AtomicInteger();
    
    private CacheManager() {
        // Schedule periodic cleanup
        cleanupExecutor.scheduleAtFixedRate(this::cleanup, 0, 1, TimeUnit.MINUTES);
    }
    
    public static CacheManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Puts a value in the cache with a TTL.
     * 
     * @param key The cache key
     * @param value The value to cache
     * @param ttl The time-to-live in the specified unit
     * @param unit The time unit for the TTL
     */
    public void put(String key, Object value, long ttl, TimeUnit unit) {
        long expiration = System.currentTimeMillis() + unit.toMillis(ttl);
        cache.put(key, new CacheEntry(value, expiration));
    }
    
    /**
     * Gets a value from the cache.
     * 
     * @param key The cache key
     * @return The cached value, or null if not found or expired
     */
    public Object get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            missCount.incrementAndGet();
            return null;
        }
        
        if (entry.isExpired()) {
            cache.remove(key);
            missCount.incrementAndGet();
            return null;
        }
        
        hitCount.incrementAndGet();
        return entry.getValue();
    }
    
    /**
     * Removes a value from the cache.
     * 
     * @param key The cache key
     */
    public void remove(String key) {
        cache.remove(key);
    }
    
    /**
     * Clears the entire cache.
     */
    public void clear() {
        cache.clear();
    }
    
    /**
     * Gets cache statistics.
     * 
     * @return Map containing cache statistics
     */
    public Map<String, Object> getStatistics() {
        return Map.of(
            "size", cache.size(),
            "hitCount", hitCount.get(),
            "missCount", missCount.get(),
            "hitRate", (double)hitCount.get() / (hitCount.get() + missCount.get())
        );
    }
    
    /**
     * Cleans up expired entries.
     */
    private void cleanup() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }
    
    /**
     * Cache entry with expiration.
     */
    private static class CacheEntry {
        private final Object value;
        private final long expiration;
        
        CacheEntry(Object value, long expiration) {
            this.value = value;
            this.expiration = expiration;
        }
        
        Object getValue() {
            return value;
        }
        
        boolean isExpired() {
            return isExpired(System.currentTimeMillis());
        }
        
        boolean isExpired(long now) {
            return now >= expiration;
        }
    }
}
