package bsky4j.util;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ReentrantReadWriteLock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optimized HTTP client manager with improved performance and reliability.
 */
public class HttpClientManager {
    private static final Logger LOGGER = Logger.getLogger(HttpClientManager.class.getName());
    
    // Singleton instance
    private static final HttpClientManager INSTANCE = new HttpClientManager();
    
    // Default client configuration
    private static final Bsky4JClientConfiguration DEFAULT_CONFIG = 
            Bsky4JClientConfiguration.builder()
                .connectTimeoutMs(15000)
                .readTimeoutMs(30000)
                .maxConnections(100)
                .maxConnectionsPerRoute(50)
                .build();
    
    // Cache of HTTP clients by configuration
    private final ConcurrentHashMap<Bsky4JClientConfiguration, HttpClient> clientCache = 
            new ConcurrentHashMap<>();
            
    // Lock for thread-safe client creation
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    // Statistics tracking
    private final ConcurrentHashMap<String, AtomicInteger> activeConnections = 
            new ConcurrentHashMap<>();
    
    private final ConcurrentHashMap<String, AtomicLong> connectionErrors = 
            new ConcurrentHashMap<>();
    
    private final ConcurrentHashMap<String, AtomicLong> requestLatencies = 
            new ConcurrentHashMap<>();
    
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = 
            new ConcurrentHashMap<>();
    
    private final java.util.concurrent.ScheduledExecutorService statsExecutor = 
            Executors.newSingleThreadScheduledExecutor();
    
    // Thread-safe shared HttpClient with optimized configuration
    private static final HttpClient SHARED_HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .readTimeout(Duration.ofSeconds(30))
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .build();
    
    private HttpClientManager() {
        // Private constructor for singleton
        statsExecutor.scheduleAtFixedRate(this::collectStats, 0, 1, TimeUnit.MINUTES);
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
            recordConnectionStats(config.toString(), true);
            return client;
        }
        
        // Slow path - create new client with read-write lock
        lock.readLock().lock();
        try {
            // Double-check if client was created while waiting for lock
            client = clientCache.get(config);
            if (client != null) {
                recordConnectionStats(config.toString(), true);
                return client;
            }
            
            // Create new client with optimized configuration
            client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(config.getConnectTimeoutMs()))
                    .readTimeout(Duration.ofMillis(config.getReadTimeoutMs()))
                    .executor(Executors.newVirtualThreadPerTaskExecutor())
                    .build();
            
            clientCache.put(config, client);
            recordConnectionStats(config.toString(), true);
            
            // Schedule periodic cleanup
            scheduleCleanup(config);
            
            return client;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Records connection statistics.
     * @param configKey The configuration key
     * @param success Whether the connection was successful
     */
    private void recordConnectionStats(String configKey, boolean success) {
        activeConnections.computeIfAbsent(configKey, k -> new AtomicInteger(0))
                        .incrementAndGet();
        
        if (!success) {
            connectionErrors.computeIfAbsent(configKey, k -> new AtomicLong(0))
                          .incrementAndGet();
        }
    }
    
    /**
     * Records request statistics.
     * @param configKey The configuration key
     * @param durationNs The request duration in nanoseconds
     */
    public void recordRequestStats(String configKey, long durationNs) {
        requestCounts.computeIfAbsent(configKey, k -> new AtomicInteger(0))
                    .incrementAndGet();
        
        requestLatencies.computeIfAbsent(configKey, k -> new AtomicLong(0))
                        .addAndGet(durationNs);
    }
    
    /**
     * Schedules periodic cleanup for the client.
     * @param config The client configuration
     */
    private void scheduleCleanup(Bsky4JClientConfiguration config) {
        String key = config.toString();
        
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                // Check if client is still in use
                AtomicInteger count = activeConnections.get(key);
                if (count != null && count.get() == 0) {
                    // Remove from cache if not in use
                    lock.writeLock().lock();
                    try {
                        clientCache.remove(config);
                        activeConnections.remove(key);
                        connectionErrors.remove(key);
                        requestLatencies.remove(key);
                        requestCounts.remove(key);
                    } finally {
                        lock.writeLock().unlock();
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to clean up HTTP client", e);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }
    
    /**
     * Collects and logs statistics.
     */
    private void collectStats() {
        try {
            StringBuilder stats = new StringBuilder("HTTP Client Statistics:\n");
            
            // Connection statistics
            stats.append("  Active Connections: ").append(activeConnections.values().stream()
                    .mapToInt(AtomicInteger::get)
                    .sum()).append("\n");
            
            stats.append("  Total Errors: ").append(connectionErrors.values().stream()
                    .mapToLong(AtomicLong::get)
                    .sum()).append("\n");
            
            // Request statistics
            stats.append("  Total Requests: ").append(requestCounts.values().stream()
                    .mapToInt(AtomicInteger::get)
                    .sum()).append("\n");
            
            long totalLatency = requestLatencies.values().stream()
                    .mapToLong(AtomicLong::get)
                    .sum();
            
            int totalRequests = requestCounts.values().stream()
                    .mapToInt(AtomicInteger::get)
                    .sum();
            
            if (totalRequests > 0) {
                double avgLatency = (double) totalLatency / totalRequests / 1_000_000;
                stats.append("  Average Latency: ").append(String.format("%.2f", avgLatency)).append("ms\n");
            }
            
            LOGGER.log(Level.INFO, stats.toString());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to collect statistics", e);
        }
    }
    
    /**
     * Shuts down all HTTP clients and clears the cache.
     * This should be called when the application is shutting down.
     */
    public void shutdown() {
        lock.writeLock().lock();
        try {
            clientCache.values().forEach(client -> {
                try {
                    client.sendAsync(HttpRequest.newBuilder()
                        .uri(java.net.URI.create("http://localhost:0"))
                        .build(), 
                        HttpResponse.BodyHandlers.ofString())
                        .thenRun(() -> {}); // Force shutdown
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to shutdown HTTP client", e);
                }
            });
            
            clientCache.clear();
            activeConnections.clear();
            connectionErrors.clear();
            requestLatencies.clear();
            requestCounts.clear();
            
            statsExecutor.shutdown();
            try {
                if (!statsExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    statsExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                statsExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
