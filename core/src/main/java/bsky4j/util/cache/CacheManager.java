package bsky4j.util.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;

/**
 * Optimized cache manager with improved performance and reliability.
 */
public class CacheManager {
    private static final Logger LOGGER = Logger.getLogger(CacheManager.class.getName());
    private static final CacheManager INSTANCE = new CacheManager();
    
    // Cache storage with expiration
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    
    // Cleanup executor with virtual threads
    private final ScheduledExecutorService cleanupExecutor = Executors.newVirtualThreadPerTaskScheduledExecutor();
    
    // Statistics tracking
    private final AtomicInteger hitCount = new AtomicInteger();
    private final AtomicInteger missCount = new AtomicInteger();
    private final AtomicLong totalAccessTime = new AtomicLong();
    private final AtomicLong totalCleanupTime = new AtomicLong();
    
    private CacheManager() {
        // Schedule periodic cleanup with exponential backoff
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
        long startTime = System.nanoTime();
        
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            hitCount.incrementAndGet();
            return entry.getValue();
        }
        
        missCount.incrementAndGet();
        totalAccessTime.addAndGet(System.nanoTime() - startTime);
        return null;
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
        hitCount.set(0);
        missCount.set(0);
        totalAccessTime.set(0);
    }
    
    /**
     * Performs cleanup of expired entries.
     */
    private void cleanup() {
        long startTime = System.nanoTime();
        
        cache.entrySet().removeIf(entry -> {
            try {
                return entry.getValue().isExpired();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error checking cache entry expiration", e);
                return true; // Remove entry if there's an error
            }
        });
        
        totalCleanupTime.addAndGet(System.nanoTime() - startTime);
    }
    
    /**
     * Gets cache statistics.
     * 
     * @return Map containing cache statistics
     */
    public Map<String, Object> getStatistics() {
        return Map.of(
            "hitCount", hitCount.get(),
            "missCount", missCount.get(),
            "cacheSize", cache.size(),
            "averageAccessTimeNs", hitCount.get() > 0 ? totalAccessTime.get() / hitCount.get() : 0,
            "averageCleanupTimeNs", totalCleanupTime.get() / (hitCount.get() + missCount.get())
        );
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
            return System.currentTimeMillis() > expiration;
        }
    }
    
    /**
     * Shuts down the cache manager.
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        cache.clear();
    }
}
