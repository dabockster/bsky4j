package bsky4j.api.xrpc;

import bsky4j.api.entity.xrpc.*;
import bsky4j.model.atprotocol.xrpc.*;
import bsky4j.model.atprotocol.xrpc.XRPCRequest;
import bsky4j.model.atprotocol.xrpc.XRPCResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import bsky4j.util.HttpClientManager;
import bsky4j.util.Bsky4JClientConfiguration;

/**
 * Optimized XRPC client with improved performance, reliability, and monitoring.
 */
public class XRPCClient {
    private static final Logger LOGGER = Logger.getLogger(XRPCClient.class.getName());
    
    // Thread-safe shared HttpClient with optimized configuration
    private static final HttpClient SHARED_HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .responseTimeout(Duration.ofSeconds(30))
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
            
    // Request statistics tracking
    private static final ConcurrentHashMap<String, AtomicInteger> requestCounts = 
            new ConcurrentHashMap<>();
            
    private static final ConcurrentHashMap<String, AtomicLong> requestLatencies = 
            new ConcurrentHashMap<>();
            
    private static final ConcurrentHashMap<String, AtomicInteger> requestErrors = 
            new ConcurrentHashMap<>();
            
    private final URI baseUri;
    private final String authorization;
    private final ScheduledExecutorService statsExecutor = Executors.newSingleThreadScheduledExecutor();
    private final AtomicReference<HttpClient> currentClient = new AtomicReference<>();
    private final Bsky4JClientConfiguration clientConfig;
    
    public XRPCClient(URI baseUri, String authorization) {
        this(baseUri, authorization, Bsky4JClientConfiguration.builder()
                .connectTimeoutMs(15000)
                .readTimeoutMs(30000)
                .maxConnections(100)
                .maxConnectionsPerRoute(50)
                .build());
    }
    
    public XRPCClient(URI baseUri, String authorization, Bsky4JClientConfiguration config) {
        this.baseUri = baseUri;
        this.authorization = authorization;
        this.clientConfig = config;
        
        // Initialize client
        currentClient.set(HttpClientManager.getInstance().getClient(config));
        
        // Schedule periodic statistics collection
        statsExecutor.scheduleAtFixedRate(this::collectStats, 0, 1, TimeUnit.MINUTES);
    }
    
    /**
     * Makes an XRPC call with retry logic and improved error handling.
     * 
     * @param method The XRPC method to call
     * @param params The request parameters
     * @param responseType The expected response type
     * @param <T> The type parameter for the response
     * @return A CompletableFuture containing the response
     */
    public <T> CompletableFuture<T> call(String method, Map<String, Object> params, Class<T> responseType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Track request statistics
                incrementRequestCount(method);
                
                // Create request
                XRPCRequest request = new XRPCRequest(method, params);
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(baseUri.resolve(method))
                        .header("Authorization", authorization)
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(request.toJson()))
                        .build();

                // Execute with retry logic
                return executeWithRetry(httpRequest, responseType, method, params);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "XRPC call failed: " + method, e);
                throw new XRPCException(e);
            }
        });
    }

    private void incrementRequestCount(String method) {
        requestCounts.computeIfAbsent(method, k -> new AtomicInteger())
                .incrementAndGet();
    }

    private void recordLatency(String method, long durationMs) {
        requestLatencies.computeIfAbsent(method, k -> new AtomicLong())
                .addAndGet(durationMs);
    }

    private <T> T executeWithRetry(HttpRequest request, Class<T> responseType, 
                                  String method, Map<String, Object> params) throws Exception {
        final int maxRetries = 3;
        final int baseDelayMs = 1000;
        long startTime = System.nanoTime();
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                HttpResponse<String> response = SHARED_HTTP_CLIENT.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() >= 400) {
                    if (response.statusCode() >= 500 && attempt < maxRetries - 1) {
                        int delay = baseDelayMs * (1 << attempt);
                        LOGGER.log(Level.WARNING, "Retrying XRPC call after " + delay + "ms");
                        Thread.sleep(delay);
                        continue;
                    }
                    throw new XRPCException(response.statusCode(), response.body(), 
                        method, params);
                }
                
                long durationMs = (System.nanoTime() - startTime) / 1_000_000;
                recordLatency(method, durationMs);
                
                return XRPCResponse.fromJson(response.body(), responseType);
            } catch (IOException | InterruptedException e) {
                if (attempt < maxRetries - 1) {
                    int delay = baseDelayMs * (1 << attempt);
                    LOGGER.log(Level.WARNING, "Retrying XRPC call after " + delay + "ms");
                    Thread.sleep(delay);
                    continue;
                }
                throw new XRPCException(e);
            }
        }
        
        throw new XRPCException("All retries failed", method, params);
    }

    /**
     * Makes a void XRPC call (no response expected).
     * 
     * @param method The XRPC method to call
     * @param params The request parameters
     * @return A CompletableFuture that completes when the call is done
     */
    public CompletableFuture<Void> callVoid(String method, Map<String, Object> params) {
        return call(method, params, Void.class);
    }

    /**
     * Gets statistics about XRPC requests including latency information.
     * 
     * @return A map containing request counts and average latencies
     */
    public static Map<String, Map<String, Long>> getRequestStatistics() {
        Map<String, Long> counts = requestCounts.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> (long)e.getValue().get()));
                    
        Map<String, Long> latencies = requestLatencies.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().get()));
                    
        return Map.of(
            "counts", counts,
            "latencies", latencies
        );
    }

    /**
     * Clears the request statistics.
     */
    public static void clearStatistics() {
        requestCounts.clear();
        requestLatencies.clear();
    }

    /**
     * Collects and logs statistics periodically.
     */
    private void collectStats() {
        try {
            Map<String, Map<String, Long>> stats = getRequestStatistics();
            StringBuilder sb = new StringBuilder("XRPC Statistics:\n");
            
            stats.get("counts").forEach((method, count) -> {
                sb.append("Method: ").append(method)
                  .append("\n  Requests: ").append(count)
                  .append("\n  Latency: ")
                  .append(stats.get("latencies").getOrDefault(method, 0L) / 1000.0)
                  .append("ms\n");
            });
            
            LOGGER.log(Level.INFO, sb.toString());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to collect statistics", e);
        }
    }

    /**
     * Closes the XRPC client and releases resources.
     */
    public void close() {
        statsExecutor.shutdown();
        try {
            if (!statsExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                statsExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            statsExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
