package bsky4j.stream;

import bsky4j.api.entity.stream.*;
import bsky4j.model.atproto.stream.*;
import bsky4j.model.atprotocol.stream.StreamEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class WebSocketClient {
    private final HttpClient httpClient;
    private final URI baseUri;
    private final String authorization;
    private WebSocket webSocket;
    private final Consumer<StreamEvent> eventHandler;

    public WebSocketClient(URI baseUri, String authorization, Consumer<StreamEvent> eventHandler) {
        this.httpClient = HttpClient.newHttpClient();
        this.baseUri = baseUri;
        this.authorization = authorization;
        this.eventHandler = eventHandler;
    }

    public CompletableFuture<Void> connect() {
        return httpClient.newWebSocketBuilder()
                .header("Authorization", authorization)
                .buildAsync(baseUri, new WebSocket.Listener() {
                    @Override
                    public void onText(WebSocket webSocket, CharSequence data, boolean last) {
                        StreamEvent event = StreamEvent.fromJson(data.toString());
                        eventHandler.accept(event);
                    }

                    @Override
                    public void onBinary(WebSocket webSocket, byte[] data, int offset, int length, boolean last) {
                        // Handle binary data if needed
                    }

                    @Override
                    public void onClose(WebSocket webSocket, int statusCode, String reason) {
                        // Handle connection close
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        // Handle errors
                    }
                });
    }

    public void send(String message) {
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.sendText(message, true);
        }
    }

    public void close() {
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Closing connection");
        }
    }
}
