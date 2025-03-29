package bsky4j.stream;

import bsky4j.model.atproto.stream.FirehoseEvent;
import java.net.URI;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client for connecting to the ATProtocol firehose stream.
 * Provides real-time access to events from the network.
 */
public class FirehoseClient {
    private static final Logger LOGGER = Logger.getLogger(FirehoseClient.class.getName());
    private final WebSocketClient webSocketClient;
    private final Consumer<FirehoseEvent> eventHandler;
    private final Consumer<Throwable> errorHandler;

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
                        eventHandler.accept((FirehoseEvent) event);
                    }
                } catch (Exception e) {
                    errorHandler.accept(e);
                }
            },
            errorHandler
        );
    }

    /**
     * Connects to the firehose stream.
     *
     * @return A CompletableFuture that completes when the connection is established
     */
    public CompletableFuture<Void> connect() {
        LOGGER.info("Connecting to firehose stream...");
        return webSocketClient.connect()
            .thenRun(() -> LOGGER.info("Connected to firehose stream"))
            .exceptionally(error -> {
                errorHandler.accept(error);
                return null;
            });
    }

    /**
     * Closes the firehose connection.
     */
    public void close() {
        LOGGER.info("Closing firehose connection");
        webSocketClient.close();
    }
    
    /**
     * Checks if the firehose connection is currently open.
     *
     * @return true if the connection is open, false otherwise
     */
    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isConnected();
    }
}
