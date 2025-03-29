package bsky4j.util;

import net.socialhub.http.HttpClient;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages HTTP clients for the ATProtocol implementation.
 * Provides optimized connection pooling and resource management.
 */
public class HttpClientManager {
    private static final Logger LOGGER = Logger.getLogger(HttpClientManager.class.getName());
    
    // Singleton instance
    private static final HttpClientManager INSTANCE = new HttpClientManager();
    
    // Default client configuration
    private static final Bsky4JClientConfiguration DEFAULT_CONFIG = 
            Bsky4JClientConfiguration.builder().build();
    
    // Cache of HTTP clients by configuration
    private final ConcurrentHashMap<Bsky4JClientConfiguration, HttpClient> clientCache = 
            new ConcurrentHashMap<>();
            
    // Lock for thread-safe client creation
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    private HttpClientManager() {
        // Private constructor for singleton
    }
    
    /**
     * Gets the singleton instance of the HTTP client manager.
     * 
     * @return The HTTP client manager instance
     */
    public static HttpClientManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Gets a shared HTTP client with the default configuration.
     * 
     * @return A shared HTTP client
     */
    public HttpClient getClient() {
        return getClient(DEFAULT_CONFIG);
    }
    
    /**
     * Gets a shared HTTP client with the specified configuration.
     * Clients are cached by configuration to avoid creating unnecessary instances.
     * 
     * @param config The client configuration
     * @return A shared HTTP client
     */
    public HttpClient getClient(Bsky4JClientConfiguration config) {
        // Fast path - check if client already exists
        HttpClient client = clientCache.get(config);
        if (client != null) {
            return client;
        }
        
        // Slow path - create new client with read-write lock
        lock.readLock().lock();
        try {
            // Double-check if client was created while waiting for lock
            client = clientCache.get(config);
            if (client != null) {
                return client;
            }
            
            // Upgrade to write lock
            lock.readLock().unlock();
            lock.writeLock().lock();
            
            try {
                // Triple-check after acquiring write lock
                client = clientCache.get(config);
                if (client != null) {
                    return client;
                }
                
                // Create new client
                LOGGER.log(Level.FINE, "Creating new HTTP client with custom configuration");
                client = new HttpClient(config);
                clientCache.put(config, client);
                return client;
            } finally {
                // Downgrade to read lock
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Shuts down all HTTP clients and clears the cache.
     * This should be called when the application is shutting down.
     */
    public void shutdown() {
        lock.writeLock().lock();
        try {
            LOGGER.log(Level.INFO, "Shutting down all HTTP clients");
            clientCache.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
