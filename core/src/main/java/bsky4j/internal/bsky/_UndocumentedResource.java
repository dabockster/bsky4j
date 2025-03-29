package bsky4j.internal.bsky;

import bsky4j.BlueskyTypes;
import bsky4j.api.bsky.UndocumentedResource;
import bsky4j.api.entity.bsky.undoc.UndocGetPopularRequest;
import bsky4j.api.entity.bsky.undoc.UndocGetPopularResponse;
import bsky4j.api.entity.bsky.undoc.UndocSearchFeedsRequest;
import bsky4j.api.entity.bsky.undoc.UndocSearchFeedsResponse;
import bsky4j.api.entity.share.Response;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static bsky4j.internal.share._InternalUtility.proceed;
import static bsky4j.internal.share._InternalUtility.xrpc;

public class _UndocumentedResource implements UndocumentedResource {

    private final String uri;

    public _UndocumentedResource(String uri) {
        this.uri = uri;
    }

    @Override
    public Response<UndocGetPopularResponse> getPopular(
            UndocGetPopularRequest request
    ) {
        return proceed(UndocGetPopularResponse.class, () -> {

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri))
                            .header("Authorization", request.getBearerToken());

            request.toMap().forEach((key, value) -> builder.header(key, value));

            HttpRequest httpRequest = builder.GET().build();

            CompletableFuture<HttpResponse<String>> response = java.net.http.HttpClient.newHttpClient().sendAsync(httpRequest, BodyHandlers.ofString());

            return new Response<>(response.thenApply(HttpResponse::body).join());
        });
    }
}
