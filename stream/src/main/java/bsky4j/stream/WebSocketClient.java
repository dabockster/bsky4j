package bsky4j.stream;

import bsky4j.util.HttpClientManager;
import bsky4j.util.Bsky4JClientConfiguration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optimized WebSocket client with improved performance, reliability, and monitoring.
 */
public class WebSocketClient {
    private static final Logger logger = Logger.getLogger(WebSocketClient.class.getName());
    
    // Shared HttpClient with optimized configuration
    private static final HttpClient SHARED_HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .build();
            
    // Connection statistics tracking
    private final ConcurrentHashMap<String, AtomicInteger> activeConnections = 
            new ConcurrentHashMap<>();
            
    private final ConcurrentHashMap<String, AtomicLong> connectionErrors = 
            new ConcurrentHashMap<>();
            
    private final ConcurrentHashMap<String, AtomicLong> messageLatencies = 
            new ConcurrentHashMap<>();
            
    private final ConcurrentHashMap<String, AtomicInteger> messageCounts = 
            new ConcurrentHashMap<>();
            
    private final URI baseUri;
    private final String authorization;
    private final ScheduledExecutorService statsExecutor = Executors.newSingleThreadScheduledExecutor();
    private final AtomicReference<WebSocket> currentWebSocket = new AtomicReference<>();
    private final Bsky4JClientConfiguration clientConfig;
    
    public WebSocketClient(URI baseUri, String authorization) {
        this(baseUri, authorization, Bsky4JClientConfiguration.builder()
                .connectTimeoutMs(15000)
                .readTimeoutMs(30000)
                .maxConnections(100)
                .maxConnectionsPerRoute(50)
                .build());
    }
    
    public WebSocketClient(URI baseUri, String authorization, Bsky4JClientConfiguration config) {
        this.baseUri = baseUri;
        this.authorization = authorization;
        this.clientConfig = config;
        
        // Schedule periodic statistics collection
        statsExecutor.scheduleAtFixedRate(this::collectStats, 0, 1, TimeUnit.MINUTES);
    }
    
    /**
     * Connects to the WebSocket server with optimized connection handling.
     */
    public CompletableFuture<Void> connect() {
        return connectInternal();
    }
    
    private CompletableFuture<Void> connectInternal() {
        return SHARED_HTTP_CLIENT.newWebSocketBuilder()
                .header("Authorization", authorization)
                .buildAsync(baseUri, new WebSocket.Listener() {
                    private final StringBuilder messageBuffer = new StringBuilder();
                    private final AtomicBoolean isComplete = new AtomicBoolean(false);
                    
                    @Override
                    public void onText(WebSocket webSocket, CharSequence data, boolean last) {
                        long startTime = System.nanoTime();
                        
                        try {
                            if (last) {
                                String completeMessage = messageBuffer.toString() + data.toString();
                                messageBuffer.setLength(0);
                                
                                // Process complete message
                                processMessage(completeMessage);
                                
                                long durationNs = System.nanoTime() - startTime;
                                recordMessageStats(durationNs, false);
                            } else {
                                messageBuffer.append(data);
                            }
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "Error processing WebSocket message", e);
                            recordMessageStats(0, true);
                        }
                    }
                    
                    @Override
                    public void onBinary(WebSocket webSocket, byte[] data, int offset, int length, boolean last) {
                        if (last) {
                            // Process complete binary message
                            processBinaryMessage(data, offset, length);
                        } else {
                            // Buffer binary data
                            // Note: This is a simplified example - in production you would need a proper buffer management
                        }
                    }
                    
                    @Override
                    public void onPing(WebSocket webSocket, byte[] data) {
                        // Handle ping - typically responds with a pong
                        webSocket.sendPong(data);
                    }
                    
                    @Override
                    public void onPong(WebSocket webSocket, byte[] data) {
                        // Handle pong - typically logs or monitors latency
                    }
                    
                    @Override
                    public void onClose(WebSocket webSocket, int statusCode, String reason) {
                        logger.log(Level.INFO, "WebSocket connection closed: " + reason);
                        recordConnectionStats(false);
                        
                        // Attempt reconnection with exponential backoff
                        scheduleReconnection();
                    }
                    
                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        logger.log(Level.SEVERE, "WebSocket error", error);
                        recordConnectionStats(false);
                        
                        // Attempt reconnection with exponential backoff
                        scheduleReconnection();
                    }
                });
    }
    
    /**
     * Processes a complete WebSocket message.
     * 
     * @param message The complete message
     */
    private void processMessage(String message) {
        try {
            // Parse and process the message
            // Note: This is a simplified example - in production you would implement proper message handling
            
            // Example: Parse JSON message
            // JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            // handleJsonMessage(json);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error processing message", e);
        }
    }
    
    /**
     * Processes a complete binary WebSocket message.
     * 
     * @param data The binary data
     * @param offset The offset in the data array
     * @param length The length of the data
     */
    private void processBinaryMessage(byte[] data, int offset, int length) {
        try {
            // Process binary message
            // Note: This is a simplified example - in production you would implement proper binary message handling
            
            // Example: Process binary data
            // byte[] completeData = Arrays.copyOfRange(data, offset, offset + length);
            // handleBinaryData(completeData);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error processing binary message", e);
        }
    }
    
    /**
     * Schedules reconnection with exponential backoff.
     */
    private void scheduleReconnection() {
        int maxRetries = 5;
        int baseDelayMs = 1000;
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                int delay = baseDelayMs * (1 << attempt);
                Thread.sleep(delay);
                
                // Attempt reconnection
                connectInternal();
                return;
            } catch (Exception e) {
                logger.log(Level.WARNING, "Reconnection attempt " + (attempt + 1) + " failed", e);
            }
        }
        
        logger.log(Level.SEVERE, "Failed to reconnect after " + maxRetries + " attempts");
    }
    
    /**
     * Records connection statistics.
     * 
     * @param success Whether the connection was successful
     */
    private void recordConnectionStats(boolean success) {
        activeConnections.computeIfAbsent(baseUri.toString(), k -> new AtomicInteger(0))
                        .incrementAndGet();
        
        if (!success) {
            connectionErrors.computeIfAbsent(baseUri.toString(), k -> new AtomicLong(0))
                          .incrementAndGet();
        }
    }
    
    /**
     * Records message statistics.
     * 
     * @param durationNs The message processing duration in nanoseconds
     * @param isError Whether the message processing was an error
     */
    private void recordMessageStats(long durationNs, boolean isError) {
        messageCounts.computeIfAbsent(baseUri.toString(), k -> new AtomicInteger(0))
                    .incrementAndGet();
        
        messageLatencies.computeIfAbsent(baseUri.toString(), k -> new AtomicLong(0))
                        .addAndGet(durationNs);
        
        if (isError) {
            connectionErrors.computeIfAbsent(baseUri.toString(), k -> new AtomicLong(0))
                          .incrementAndGet();
        }
    }
    
    /**
     * Collects and logs statistics.
     */
    private void collectStats() {
        try {
            StringBuilder stats = new StringBuilder("WebSocket Client Statistics:\n");
            
            // Connection statistics
            stats.append("  Active Connections: ").append(activeConnections.values().stream()
                    .mapToInt(AtomicInteger::get)
                    .sum()).append("\n");
            
            stats.append("  Total Connection Errors: ").append(connectionErrors.values().stream()
                    .mapToLong(AtomicLong::get)
                    .sum()).append("\n");
            
            // Message statistics
            stats.append("  Total Messages: ").append(messageCounts.values().stream()
                    .mapToInt(AtomicInteger::get)
                    .sum()).append("\n");
            
            long totalLatency = messageLatencies.values().stream()
                    .mapToLong(AtomicLong::get)
                    .sum();
            
            int totalMessages = messageCounts.values().stream()
                    .mapToInt(AtomicInteger::get)
                    .sum();
            
            if (totalMessages > 0) {
                double avgLatency = (double) totalLatency / totalMessages / 1_000_000;
                stats.append("  Average Message Latency: ").append(String.format("%.2f", avgLatency)).append("ms\n");
            }
            
            logger.log(Level.INFO, stats.toString());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to collect statistics", e);
        }
    }
    
    /**
     * Shuts down the WebSocket client and clears resources.
     */
    public void shutdown() {
        statsExecutor.shutdown();
        try {
            if (!statsExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                statsExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            statsExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        WebSocket current = currentWebSocket.get();
        if (current != null) {
            current.sendClose(WebSocket.NORMAL_CLOSURE, "Client shutdown");
        }
        
        HttpClientManager.getInstance().shutdown();
    }
}
