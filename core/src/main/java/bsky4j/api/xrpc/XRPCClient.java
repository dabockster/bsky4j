package bsky4j.api.xrpc;

import bsky4j.api.entity.xrpc.*;
import bsky4j.model.atproto.xrpc.*;
import bsky4j.model.atproto.xrpc.XRPCRequest;
import bsky4j.model.atprotocol.xrpc.XRPCResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class XRPCClient {
    private final HttpClient httpClient;
    private final URI baseUri;
    private final String authorization;

    public XRPCClient(URI baseUri, String authorization) {
        this.httpClient = HttpClient.newHttpClient();
        this.baseUri = baseUri;
        this.authorization = authorization;
    }

    public <T> CompletableFuture<T> call(String method, Map<String, Object> params, Class<T> responseType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                XRPCRequest request = new XRPCRequest(method, params);
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(baseUri.resolve(method))
                        .header("Authorization", authorization)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(request.toJson()))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() >= 400) {
                    throw new XRPCException(response.statusCode(), response.body());
                }
                
                return XRPCResponse.fromJson(response.body(), responseType);
            } catch (IOException | InterruptedException e) {
                throw new XRPCException(e);
            }
        });
    }

    public CompletableFuture<Void> callVoid(String method, Map<String, Object> params) {
        return call(method, params, Void.class);
    }
}
