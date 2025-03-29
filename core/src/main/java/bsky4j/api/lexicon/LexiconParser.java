package bsky4j.api.lexicon;

import bsky4j.model.atproto.lexicon.LexiconParseRequest;
import bsky4j.model.atproto.lexicon.LexiconParseResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LexiconParser {
    private final HttpClient httpClient;
    private final URI baseUri;
    private final String authorization;

    public LexiconParser(URI baseUri, String authorization) {
        this.httpClient = HttpClient.newHttpClient();
        this.baseUri = baseUri;
        this.authorization = authorization;
    }

    public CompletableFuture<LexiconParseResponse> parseLexicon(LexiconParseRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest requestBuilder = HttpRequest.newBuilder()
                    .uri(baseUri.resolve("/xrpc/lexicon.parse"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", authorization)
                    .POST(HttpRequest.BodyPublishers.ofString(request.toJson()))
                    .build();

                HttpResponse<String> response = httpClient.send(requestBuilder, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() != 200) {
                    throw new RuntimeException("HTTP error: " + response.statusCode());
                }
                
                return LexiconParseResponse.fromJson(response.body());
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse lexicon", e);
            }
        });
    }
}
