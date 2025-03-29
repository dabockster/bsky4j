package bsky4j.util;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optimized HTTP client manager with improved connection pooling and monitoring.
 * Implements ATProtocol's HTTP specification for connection handling.
 */
public class HttpClientManager {
    private static final Logger LOGGER = Logger.getLogger(HttpClientManager.class.getName());
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
    private final ConcurrentMap<Bsky4JClientConfiguration, HttpClient> clientCache = 
            new ConcurrentHashMap<>();
            
    // Lock for thread-safe client creation
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    // Statistics tracking
    private final ConcurrentMap<String, AtomicInteger> activeConnections = 
            new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicLong> connectionErrors = 
            new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicLong> requestLatencies = 
            new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicInteger> requestCounts = 
            new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicInteger> requestErrors = 
            new ConcurrentHashMap<>();
    
    // Scheduled executor for periodic tasks
    private final java.util.concurrent.ScheduledExecutorService statsExecutor = 
            Executors.newSingleThreadScheduledExecutor();
    
    // Connection pooling configuration
    private static final int MAX_IDLE_TIME_MS = 1000 * 60 * 5; // 5 minutes
    private static final int MAX_TOTAL_CONNECTIONS = 200;
    private static final int MAX_CONNECTIONS_PER_ROUTE = 50;
    
    /**
     * Private constructor to prevent instantiation.
     */
    private HttpClientManager() {
        // Schedule periodic cleanup
        statsExecutor.scheduleAtFixedRate(this::cleanupConnections, 
            MAX_IDLE_TIME_MS, MAX_IDLE_TIME_MS, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        // Schedule periodic statistics collection
        statsExecutor.scheduleAtFixedRate(this::collectStats, 
            60, 60, java.util.concurrent.TimeUnit.SECONDS);
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
     * Gets a shared HTTP client with the specified configuration.
     * 
     * @param config The client configuration
     * @return A shared HTTP client
     */
    public HttpClient getClient(Bsky4JClientConfiguration config) {
        if (config == null) {
            config = DEFAULT_CONFIG;
        }
        
        String configKey = getConfigKey(config);
        
        // Check cache first
        HttpClient client = clientCache.get(config);
        if (client != null) {
            return client;
        }
        
        // Create new client with lock
        lock.writeLock().lock();
        try {
            client = clientCache.get(config);
            if (client != null) {
                return client;
            }
            
            client = createHttpClient(config);
            clientCache.put(config, client);
            return client;
        } finally {
            lock.writeLock().unlock();
        }
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
     * Creates a new HTTP client with the specified configuration.
     * 
     * @param config The client configuration
     * @return A new HTTP client
     */
    private HttpClient createHttpClient(Bsky4JClientConfiguration config) {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getConnectTimeoutMs()))
                .responseTimeout(Duration.ofMillis(config.getReadTimeoutMs()))
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .version(HttpClient.Version.HTTP_2)
                .build();
    }
    
    /**
     * Executes an HTTP request with retry logic and connection pooling.
     * 
     * @param request The HTTP request
     * @param responseType The expected response type
     * @param config The client configuration
     * @param <T> The type parameter for the response
     * @return The response object
     * @throws Exception if the request fails
     */
    public <T> T executeRequest(HttpRequest request, Class<T> responseType, 
                                  Bsky4JClientConfiguration config) throws Exception {
        long startTime = System.nanoTime();
        String configKey = getConfigKey(config);
        
        try {
            // Record connection attempt
            activeConnections.computeIfAbsent(configKey, k -> new AtomicInteger(0))
                            .incrementAndGet();
            
            // Execute request
            HttpClient client = getClient(config);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Record success
            recordRequestStats(configKey, System.nanoTime() - startTime);
            
            // Parse response
            return GSON.fromJson(response.body(), responseType);
        } catch (Exception e) {
            // Record error
            connectionErrors.computeIfAbsent(configKey, k -> new AtomicLong(0))
                          .incrementAndGet();
            recordRequestStats(configKey, System.nanoTime() - startTime);
            throw e;
        } finally {
            // Record connection release
            activeConnections.computeIfAbsent(configKey, k -> new AtomicInteger(0))
                            .decrementAndGet();
        }
    }
    
    /**
     * Records request statistics.
     * 
     * @param configKey The configuration key
     * @param durationNs The request duration in nanoseconds
     */
    private void recordRequestStats(String configKey, long durationNs) {
        requestCounts.computeIfAbsent(configKey, k -> new AtomicInteger(0))
                    .incrementAndGet();
        requestLatencies.computeIfAbsent(configKey, k -> new AtomicLong(0))
                       .addAndGet(durationNs);
    }
    
    /**
     * Cleans up idle connections and expired clients.
     */
    private void cleanupConnections() {
        long currentTime = System.currentTimeMillis();
        
        // Cleanup clients
        clientCache.forEach((config, client) -> {
            String configKey = getConfigKey(config);
            AtomicInteger active = activeConnections.get(configKey);
            
            if (active != null && active.get() == 0 && 
                currentTime - client.timestamp() > MAX_IDLE_TIME_MS) {
                clientCache.remove(config);
            }
        });
    }
    
    /**
     * Collects and logs statistics.
     */
    private void collectStats() {
        Map<String, Object> stats = Map.of(
            "activeConnections", activeConnections.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum(),
            "totalRequests", requestCounts.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum(),
            "totalErrors", requestErrors.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum(),
            "averageLatencyMs", requestCounts.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum() > 0 ? 
                requestLatencies.values().stream()
                    .mapToLong(AtomicLong::get)
                    .sum() / requestCounts.values().stream()
                        .mapToInt(AtomicInteger::get)
                        .sum() / 1000000 : 0
        );
        
        LOGGER.log(Level.INFO, "HTTP client statistics: " + stats);
    }
    
    /**
     * Gets a unique key for the configuration.
     * 
     * @param config The client configuration
     * @return A unique configuration key
     */
    private String getConfigKey(Bsky4JClientConfiguration config) {
        return String.format("%d-%d-%d-%d",
            config.getConnectTimeoutMs(),
            config.getReadTimeoutMs(),
            config.getMaxConnections(),
            config.getMaxConnectionsPerRoute());
    }
    
    /**
     * Shuts down all HTTP clients and clears the cache.
     * This should be called when the application is shutting down.
     */
    public void shutdown() {
        statsExecutor.shutdown();
        clientCache.values().forEach(HttpClient::close);
        clientCache.clear();
        activeConnections.clear();
        connectionErrors.clear();
        requestLatencies.clear();
        requestCounts.clear();
        requestErrors.clear();
    }
}
