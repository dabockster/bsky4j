package bsky4j.stream;

import bsky4j.model.atproto.stream.FirehoseEvent;
import java.net.URI;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class FirehoseClient {
    private final WebSocketClient webSocketClient;

    public FirehoseClient(URI baseUri, String authorization, Consumer<FirehoseEvent> eventHandler) {
        this.webSocketClient = new WebSocketClient(baseUri, authorization, event -> {
            if (event instanceof FirehoseEvent) {
                eventHandler.accept((FirehoseEvent) event);
            }
        });
    }

    public CompletableFuture<Void> connect() {
        return webSocketClient.connect();
    }

    public void close() {
        webSocketClient.close();
    }
}
