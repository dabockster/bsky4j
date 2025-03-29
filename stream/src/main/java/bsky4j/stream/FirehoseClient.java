package bsky4j.stream;

import bsky4j.model.atproto.stream.FirehoseEvent;
import java.net.URI;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optimized client for connecting to the ATProtocol firehose stream.
 * Provides real-time access to events from the network with improved performance and reliability.
 */
public class FirehoseClient {
    private static final Logger LOGGER = Logger.getLogger(FirehoseClient.class.getName());
    private final WebSocketClient webSocketClient;
    private final Consumer<FirehoseEvent> eventHandler;
    private final Consumer<Throwable> errorHandler;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean isReconnecting = new AtomicBoolean(false);
    private final AtomicInteger messageCount = new AtomicInteger(0);

    /**
     * Creates a new FirehoseClient with default error handling.
     *
     * @param baseUri The URI of the firehose endpoint
     * @param authorization Authorization token for authentication
     * @param eventHandler Consumer that processes firehose events
     */
    public FirehoseClient(URI baseUri, String authorization, Consumer<FirehoseEvent> eventHandler) {
        this(baseUri, authorization, eventHandler, 
             error -> LOGGER.log(Level.SEVERE, "Firehose error", error));
    }

    /**
     * Creates a new FirehoseClient with custom error handling.
     *
     * @param baseUri The URI of the firehose endpoint
     * @param authorization Authorization token for authentication
     * @param eventHandler Consumer that processes firehose events
     * @param errorHandler Consumer that handles errors
     */
    public FirehoseClient(URI baseUri, String authorization, 
                         Consumer<FirehoseEvent> eventHandler,
                         Consumer<Throwable> errorHandler) {
        this.eventHandler = eventHandler;
        this.errorHandler = errorHandler;
        
        this.webSocketClient = new WebSocketClient(
            baseUri, 
            authorization, 
            event -> {
                try {
                    if (event instanceof FirehoseEvent) {
                        messageCount.incrementAndGet();
                        eventHandler.accept((FirehoseEvent) event);
                    }
                } catch (Exception e) {
                    errorHandler.accept(e);
                }
            },
            error -> {
                errorHandler.accept(error);
                if (!isReconnecting.getAndSet(true)) {
                    LOGGER.info("Attempting to reconnect to firehose stream");
                    webSocketClient.connect();
                }
            }
        );
    }

    /**
     * Connects to the firehose stream with improved error handling and monitoring.
     *
     * @return A CompletableFuture that completes when the connection is established
     */
    public CompletableFuture<Void> connect() {
        LOGGER.log(Level.INFO, "Connecting to firehose stream...");
        return webSocketClient.connect()
            .thenRun(() -> {
                isConnected.set(true);
                LOGGER.log(Level.INFO, "Connected to firehose stream");
            })
            .exceptionally(error -> {
                isConnected.set(false);
                errorHandler.accept(error);
                return null;
            });
    }

    /**
     * Closes the firehose connection with proper resource cleanup.
     */
    public void close() {
        LOGGER.log(Level.INFO, "Closing firehose connection");
        isConnected.set(false);
        isReconnecting.set(false);
        webSocketClient.close();
    }

    /**
     * Checks if the firehose connection is currently open.
     *
     * @return true if the connection is open, false otherwise
     */
    public boolean isConnected() {
        return isConnected.get() && webSocketClient != null && webSocketClient.isConnected();
    }

    /**
     * Gets the number of messages processed by the firehose client.
     *
     * @return The number of messages processed
     */
    public int getMessageCount() {
        return messageCount.get();
    }

    /**
     * Gets the current connection status with detailed information.
     *
     * @return A string describing the connection status
     */
    public String getConnectionStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Firehose Status: ").append(isConnected() ? "Connected" : "Disconnected");
        sb.append("\nMessages Processed: ").append(getMessageCount());
        sb.append("\nReconnecting: ").append(isReconnecting.get());
        return sb.toString();
    }
}
