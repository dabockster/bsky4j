package bsky4j.stream;

import bsky4j.api.entity.stream.*;
import bsky4j.model.atproto.stream.*;
import bsky4j.model.atprotocol.stream.StreamEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebSocketClient {
    private static final Logger LOGGER = Logger.getLogger(WebSocketClient.class.getName());
    
    // Shared HttpClient for all WebSocketClient instances
    private static final HttpClient SHARED_HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .build();
            
    private final URI baseUri;
    private final String authorization;
    private WebSocket webSocket;
    private final Consumer<StreamEvent> eventHandler;
    private final Consumer<Throwable> errorHandler;
    
    private final AtomicBoolean isConnecting = new AtomicBoolean(false);
    private final AtomicBoolean shouldReconnect = new AtomicBoolean(true);
    private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
    
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final int INITIAL_RECONNECT_DELAY_MS = 1000;

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
                    StringBuilder messageBuffer = new StringBuilder();
                    
                    @Override
                    public void onText(WebSocket webSocket, CharSequence data, boolean last) {
                        WebSocketClient.this.webSocket = webSocket;
                        
                        // Append data to buffer
                        messageBuffer.append(data);
                        
                        // Process complete message
                        if (last) {
                            try {
                                StreamEvent event = StreamEvent.fromJson(messageBuffer.toString());
                                eventHandler.accept(event);
                            } catch (Exception e) {
                                errorHandler.accept(e);
                            } finally {
                                messageBuffer.setLength(0); // Clear buffer
                            }
                        }
                        
                        // Request more data
                        webSocket.request(1);
                    }

                    @Override
                    public void onBinary(WebSocket webSocket, byte[] data, int offset, int length, boolean last) {
                        // Handle binary data if needed
                        webSocket.request(1);
                    }

                    @Override
                    public void onClose(WebSocket webSocket, int statusCode, String reason) {
                        LOGGER.info("WebSocket closed: " + statusCode + " " + reason);
                        isConnecting.set(false);
                        
                        // Attempt reconnection if needed
                        if (shouldReconnect.get()) {
                            scheduleReconnect();
                        }
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        errorHandler.accept(error);
                        isConnecting.set(false);
                        
                        // Attempt reconnection if needed
                        if (shouldReconnect.get()) {
                            scheduleReconnect();
                        }
                    }
                    
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        WebSocketClient.this.webSocket = webSocket;
                        reconnectAttempts = 0; // Reset reconnect attempts on successful connection
                        isConnecting.set(false);
                        webSocket.request(1);
                    }
                });
    }
    
    private void scheduleReconnect() {
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            LOGGER.severe("Maximum reconnection attempts reached");
            return;
        }
        
        // Exponential backoff
        long delay = INITIAL_RECONNECT_DELAY_MS * (long)Math.pow(2, reconnectAttempts);
        reconnectAttempts++;
        
        LOGGER.info("Scheduling reconnect attempt " + reconnectAttempts + " in " + delay + "ms");
        reconnectExecutor.schedule(() -> {
            if (shouldReconnect.get()) {
                connectInternal();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    public void send(String message) {
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.sendText(message, true);
        }
    }

    public void close() {
        shouldReconnect.set(false);
        
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Closing connection");
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
     * Checks if the WebSocket connection is currently open.
     *
     * @return true if the connection is open, false otherwise
     */
    public boolean isConnected() {
        return webSocket != null && webSocket.isOpen();
    }
}
