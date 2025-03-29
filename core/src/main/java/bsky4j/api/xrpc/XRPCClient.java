package bsky4j.api.xrpc;

import bsky4j.api.entity.xrpc.*;
import bsky4j.model.atproto.xrpc.*;
import bsky4j.model.atprotocol.xrpc.XRPCRequest;
import bsky4j.model.atprotocol.xrpc.XRPCResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optimized XRPC client with connection pooling, retry logic, and improved error handling.
 */
public class XRPCClient {
    private static final Logger LOGGER = Logger.getLogger(XRPCClient.class.getName());
    
    // Shared HttpClient with optimized configuration
    private static final HttpClient SHARED_HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .responseTimeout(Duration.ofSeconds(30))
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
            
    // Request statistics tracking
    private static final ConcurrentHashMap<String, AtomicInteger> requestCounts = 
            new ConcurrentHashMap<>();
    
    private final URI baseUri;
    private final String authorization;
    
    /**
     * Creates a new XRPC client with the specified base URI and authorization.
     *
     * @param baseUri The base URI for the XRPC service
     * @param authorization The authorization token
     */
    public XRPCClient(URI baseUri, String authorization) {
        this.baseUri = baseUri;
        this.authorization = authorization;
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
                requestCounts.computeIfAbsent(method, k -> new AtomicInteger())
                        .incrementAndGet();
                
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

    /**
     * Executes an HTTP request with retry logic.
     * 
     * @param request The HTTP request to execute
     * @param responseType The expected response type
     * @param method The XRPC method being called
     * @param params The request parameters
     * @param <T> The type parameter for the response
     * @return The parsed response
     * @throws Exception If the request fails after retries
     */
    private <T> T executeWithRetry(HttpRequest request, Class<T> responseType, 
                                  String method, Map<String, Object> params) throws Exception {
        final int maxRetries = 3;
        final int baseDelayMs = 1000;
        
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
     * Gets statistics about XRPC requests.
     * 
     * @return A map of method names to request counts
     */
    public static Map<String, Integer> getRequestStatistics() {
        return requestCounts.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().get()));
    }

    /**
     * Clears the request statistics.
     */
    public static void clearRequestStatistics() {
        requestCounts.clear();
    }
}
