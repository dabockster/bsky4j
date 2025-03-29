package bsky4j.stream;

import bsky4j.api.entity.stream.*;
import bsky4j.model.atprotocol.stream.*;
import bsky4j.model.atprotocol.stream.StreamEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Optimized WebSocket client with improved performance, reliability, and monitoring.
 */
public class WebSocketClient {
    private static final Logger LOGGER = Logger.getLogger(WebSocketClient.class.getName());
    
    // Thread-safe shared HttpClient with optimized configuration
    private static final HttpClient SHARED_HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .readTimeout(Duration.ofSeconds(30))
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .build();
            
    private final URI baseUri;
    private final String authorization;
    private final AtomicReference<WebSocket> webSocketRef = new AtomicReference<>();
    private final Consumer<StreamEvent> eventHandler;
    private final Consumer<Throwable> errorHandler;
    
    private final AtomicBoolean isConnecting = new AtomicBoolean(false);
    private final AtomicBoolean shouldReconnect = new AtomicBoolean(true);
    private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
    
    // Message processing executor with bounded queue
    private final ThreadPoolExecutor messageExecutor = new ThreadPoolExecutor(
        Runtime.getRuntime().availableProcessors(),
        Runtime.getRuntime().availableProcessors() * 2,
        60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(1000),
        r -> {
            Thread t = new Thread(r, "WebSocketMessageHandler");
            t.setDaemon(true);
            return t;
        },
        (r, executor) -> {
            if (!executor.isShutdown()) {
                LOGGER.log(Level.WARNING, "Message queue full, dropping message");
                try {
                    r.run(); // Process message in current thread if possible
                } catch (Exception e) {
                    errorHandler.accept(e);
                }
            }
        });
    
    // Connection statistics
    private final AtomicLong connectionTime = new AtomicLong(0);
    private final AtomicLong messageCount = new AtomicLong(0);
    private final AtomicLong bytesReceived = new AtomicLong(0);
    private final AtomicLong bytesSent = new AtomicLong(0);
    
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final int INITIAL_RECONNECT_DELAY_MS = 1000;
    private static final int MAX_MESSAGE_QUEUE_SIZE = 1000;
    
    public WebSocketClient(URI baseUri, String authorization, Consumer<StreamEvent> eventHandler) {
        this(baseUri, authorization, eventHandler, error -> 
            LOGGER.log(Level.SEVERE, "WebSocket error", error));
    }
    
    public WebSocketClient(URI baseUri, String authorization, Consumer<StreamEvent> eventHandler, 
                           Consumer<Throwable> errorHandler) {
        this.baseUri = baseUri;
        this.authorization = authorization;
        this.eventHandler = eventHandler;
        this.errorHandler = errorHandler;
        
        // Schedule periodic statistics collection
        reconnectExecutor.scheduleAtFixedRate(this::collectStats, 0, 1, TimeUnit.MINUTES);
    }

    public CompletableFuture<Void> connect() {
        if (isConnecting.getAndSet(true)) {
            return CompletableFuture.completedFuture(null); // Already connecting
        }
        
        shouldReconnect.set(true);
        reconnectAttempts = 0;
        
        return connectInternal();
    }
    
    private CompletableFuture<Void> connectInternal() {
        return SHARED_HTTP_CLIENT.newWebSocketBuilder()
                .header("Authorization", authorization)
                .buildAsync(baseUri, new WebSocket.Listener() {
                    private final StringBuilder messageBuffer = new StringBuilder(1024);
                    private final AtomicInteger bufferCount = new AtomicInteger(0);
                    
                    @Override
                    public void onText(WebSocket webSocket, CharSequence data, boolean last) {
                        WebSocketClient.this.webSocketRef.set(webSocket);
                        
                        // Append data to buffer
                        messageBuffer.append(data);
                        bufferCount.incrementAndGet();
                        bytesReceived.addAndGet(data.length());
                        
                        // Process complete message
                        if (last) {
                            try {
                                StreamEvent event = StreamEvent.fromJson(messageBuffer.toString());
                                messageExecutor.submit(() -> {
                                    try {
                                        eventHandler.accept(event);
                                        messageCount.incrementAndGet();
                                    } catch (Exception e) {
                                        errorHandler.accept(e);
                                    }
                                });
                                
                                // Track message processing
                                if (messageCount.get() % 1000 == 0) {
                                    LOGGER.log(Level.FINE, "Processed " + messageCount.get() + " messages");
                                }
                            } catch (Exception e) {
                                errorHandler.accept(e);
                            } finally {
                                messageBuffer.setLength(0);
                                bufferCount.set(0);
                            }
                        }
                        
                        // Request more data
                        webSocket.request(1);
                    }

                    @Override
                    public void onBinary(WebSocket webSocket, byte[] data, int offset, int length, boolean last) {
                        bytesReceived.addAndGet(length);
                        webSocket.request(1);
                    }

                    @Override
                    public void onClose(WebSocket webSocket, int statusCode, String reason) {
                        LOGGER.log(Level.INFO, "WebSocket closed: {0} {1}", new Object[]{statusCode, reason});
                        isConnecting.set(false);
                        connectionTime.addAndGet(System.currentTimeMillis() - connectionStart.get());
                        
                        // Attempt reconnection if needed
                        if (shouldReconnect.get()) {
                            scheduleReconnect();
                        }
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        errorHandler.accept(error);
                        isConnecting.set(false);
                        connectionTime.addAndGet(System.currentTimeMillis() - connectionStart.get());
                        
                        // Attempt reconnection if needed
                        if (shouldReconnect.get()) {
                            scheduleReconnect();
                        }
                    }
                    
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        WebSocketClient.this.webSocketRef.set(webSocket);
                        reconnectAttempts = 0;
                        isConnecting.set(false);
                        connectionStart.set(System.currentTimeMillis());
                        webSocket.request(1);
                        
                        LOGGER.info("WebSocket connection established");
                    }
                });
    }

    private void collectStats() {
        try {
            StringBuilder stats = new StringBuilder("WebSocket Client Statistics:\n");
            stats.append("  Uptime: ").append(getUptime()).append("\n");
            stats.append("  Messages: ").append(messageCount.get()).append("\n");
            stats.append("  Bytes Received: ").append(bytesReceived.get()).append("\n");
            stats.append("  Bytes Sent: ").append(bytesSent.get()).append("\n");
            stats.append("  Queue Size: ").append(messageExecutor.getQueue().size()).append("\n");
            
            LOGGER.log(Level.INFO, stats.toString());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to collect statistics", e);
        }
    }

    private String getUptime() {
        long uptime = System.currentTimeMillis() - connectionStart.get();
        long hours = uptime / (1000 * 60 * 60);
        long minutes = (uptime / (1000 * 60)) % 60;
        long seconds = (uptime / 1000) % 60;
        return String.format("%dh %dm %ds", hours, minutes, seconds);
    }

    private final AtomicLong connectionStart = new AtomicLong(0);
    private int reconnectAttempts;

    private void scheduleReconnect() {
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            LOGGER.severe("Maximum reconnection attempts reached");
            return;
        }
        
        // Exponential backoff with jitter
        long delay = INITIAL_RECONNECT_DELAY_MS * (long)Math.pow(2, reconnectAttempts);
        delay += (long)(delay * 0.2 * Math.random()); // Add 20% jitter
        reconnectAttempts++;
        
        LOGGER.log(Level.INFO, "Scheduling reconnect attempt {0} in {1}ms", 
                new Object[]{reconnectAttempts, delay});
        reconnectExecutor.schedule(() -> {
            if (shouldReconnect.get()) {
                connectInternal();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    public void send(String message) {
        WebSocket webSocket = webSocketRef.get();
        if (webSocket != null && webSocket.isOpen()) {
            try {
                bytesSent.addAndGet(message.length());
                webSocket.sendText(message, true);
            } catch (Exception e) {
                errorHandler.accept(e);
                scheduleReconnect();
            }
        }
    }

    public void close() {
        shouldReconnect.set(false);
        
        WebSocket webSocket = webSocketRef.getAndSet(null);
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Closing connection");
        }
        
        // Shutdown executors
        messageExecutor.shutdown();
        try {
            if (!messageExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                messageExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            messageExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        reconnectExecutor.shutdown();
        try {
            if (!reconnectExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                reconnectExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            reconnectExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Gets statistics about WebSocket connections.
     * @return Map containing connection statistics
     */
    public Map<String, Object> getConnectionStats() {
        return Map.of(
            "uptime", getUptime(),
            "messages", messageCount.get(),
            "bytesReceived", bytesReceived.get(),
            "bytesSent", bytesSent.get(),
            "queueSize", messageExecutor.getQueue().size(),
            "activeConnections", webSocketRef.get() != null
        );
    }

    /**
     * Clears the connection statistics.
     */
    public void clearStats() {
        messageCount.set(0);
        bytesReceived.set(0);
        bytesSent.set(0);
        connectionTime.set(0);
    }
}
