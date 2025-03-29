package bsky4j.internal.bsky;

import bsky4j.ATProtocolTypes;
import bsky4j.BlueskyTypes;
import bsky4j.api.bsky.GraphResource;
import bsky4j.api.entity.atproto.repo.RepoCreateRecordRequest;
import bsky4j.api.entity.atproto.repo.RepoDeleteRecordRequest;
import bsky4j.api.entity.bsky.graph.GraphBlockRequest;
import bsky4j.api.entity.bsky.graph.GraphBlockResponse;
import bsky4j.api.entity.bsky.graph.GraphDeleteBlockRequest;
import bsky4j.api.entity.bsky.graph.GraphDeleteFollowRequest;
import bsky4j.api.entity.bsky.graph.GraphFollowRequest;
import bsky4j.api.entity.bsky.graph.GraphFollowResponse;
import bsky4j.api.entity.bsky.graph.GraphGetBlocksRequest;
import bsky4j.api.entity.bsky.graph.GraphGetBlocksResponse;
import bsky4j.api.entity.bsky.graph.GraphGetFollowersRequest;
import bsky4j.api.entity.bsky.graph.GraphGetFollowersResponse;
import bsky4j.api.entity.bsky.graph.GraphGetFollowsRequest;
import bsky4j.api.entity.bsky.graph.GraphGetFollowsResponse;
import bsky4j.api.entity.bsky.graph.GraphGetMutesRequest;
import bsky4j.api.entity.bsky.graph.GraphGetMutesResponse;
import bsky4j.api.entity.bsky.graph.GraphMuteActorRequest;
import bsky4j.api.entity.bsky.graph.GraphUnmuteActorRequest;
import bsky4j.api.entity.share.Response;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;

import static bsky4j.internal.share._InternalUtility.proceed;
import static bsky4j.internal.share._InternalUtility.xrpc;

public class _GraphResource implements GraphResource {

    private final String uri;

    public _GraphResource(String uri) {
        this.uri = uri;
    }

    @Override
    public Response<GraphFollowResponse> follow(
            GraphFollowRequest request
    ) {
        return proceed(GraphFollowResponse.class, () -> {

            RepoCreateRecordRequest record =
                    RepoCreateRecordRequest.builder()
                            .accessJwt(request.getAccessJwt())
                            .repo(request.getDid())
                            .collection(BlueskyTypes.GraphFollow)
                            .record(request.toFollow())
                            .build();

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(xrpc(this.uri) + ATProtocolTypes.RepoCreateRecord)
                    .header("Authorization", request.getBearerToken())
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(record.toJson()))
                    .build();

            return HttpClient.newHttpClient().sendAsync(httpRequest, BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(Response::fromJson);
        });
    }

    @Override
    public Response<Void> deleteFollow(
            GraphDeleteFollowRequest request
    ) {
        return proceed(() -> {

            RepoDeleteRecordRequest record =
                    RepoDeleteRecordRequest.builder()
                            .accessJwt(request.getAccessJwt())
                            .repo(request.getDid())
                            .collection(BlueskyTypes.GraphFollow)
                            .rkey(request.getRkey())
                            .build();

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(xrpc(this.uri) + ATProtocolTypes.RepoDeleteRecord)
                    .header("Authorization", request.getBearerToken())
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(record.toJson()))
                    .build();

            return HttpClient.newHttpClient().sendAsync(httpRequest, BodyHandlers.ofString())
                    .thenApply(HttpResponse::statusCode)
                    .thenApply(code -> code == 200 ? CompletableFuture.completedFuture(null) : CompletableFuture.failedFuture(new RuntimeException("Failed to delete follow")));
        });
    }

    @Override
    public Response<GraphGetFollowersResponse> getFollowers(
            GraphGetFollowersRequest request
    ) {
        return proceed(GraphGetFollowersResponse.class, () -> {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(xrpc(this.uri) + BlueskyTypes.GraphGetFollowers)
                    .header("Authorization", request.getBearerToken())
                    .GET()
                    .build();

            request.toMap().forEach((key, value) -> httpRequest = httpRequest.uri(xrpc(this.uri) + BlueskyTypes.GraphGetFollowers + "?" + key + "=" + value));

            return HttpClient.newHttpClient().sendAsync(httpRequest, BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(Response::fromJson);
        });
    }

    @Override
    public Response<GraphGetFollowsResponse> getFollows(
            GraphGetFollowsRequest request
    ) {
        return proceed(GraphGetFollowsResponse.class, () -> {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(xrpc(this.uri) + BlueskyTypes.GraphGetFollows)
                    .header("Authorization", request.getBearerToken())
                    .GET()
                    .build();

            request.toMap().forEach((key, value) -> httpRequest = httpRequest.uri(xrpc(this.uri) + BlueskyTypes.GraphGetFollows + "?" + key + "=" + value));

            return HttpClient.newHttpClient().sendAsync(httpRequest, BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(Response::fromJson);
        });
    }

    @Override
    public Response<GraphGetMutesResponse> getMutes(
            GraphGetMutesRequest request
    ) {
        return proceed(GraphGetMutesResponse.class, () -> {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(xrpc(this.uri) + BlueskyTypes.GraphGetMutes)
                    .header("Authorization", request.getBearerToken())
                    .GET()
                    .build();

            request.toMap().forEach((key, value) -> httpRequest = httpRequest.uri(xrpc(this.uri) + BlueskyTypes.GraphGetMutes + "?" + key + "=" + value));

            return HttpClient.newHttpClient().sendAsync(httpRequest, BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(Response::fromJson);
        });
    }

    @Override
    public Response<Void> muteActor(
            GraphMuteActorRequest request
    ) {
        return proceed(() -> {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(xrpc(this.uri) + BlueskyTypes.GraphMuteActor)
                    .header("Authorization", request.getBearerToken())
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(request.toJson()))
                    .build();

            return HttpClient.newHttpClient().sendAsync(httpRequest, BodyHandlers.ofString())
                    .thenApply(HttpResponse::statusCode)
                    .thenApply(code -> code == 200 ? CompletableFuture.completedFuture(null) : CompletableFuture.failedFuture(new RuntimeException("Failed to mute actor")));
        });
    }

    @Override
    public Response<Void> unmuteActor(
            GraphUnmuteActorRequest request
    ) {
        return proceed(() -> {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(xrpc(this.uri) + BlueskyTypes.GraphUnmuteActor)
                    .header("Authorization", request.getBearerToken())
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(request.toJson()))
                    .build();

            return HttpClient.newHttpClient().sendAsync(httpRequest, BodyHandlers.ofString())
                    .thenApply(HttpResponse::statusCode)
                    .thenApply(code -> code == 200 ? CompletableFuture.completedFuture(null) : CompletableFuture.failedFuture(new RuntimeException("Failed to unmute actor")));
        });
    }

    @Override
    public Response<GraphBlockResponse> block(
            GraphBlockRequest request
    ) {
        return proceed(GraphBlockResponse.class, () -> {

            RepoCreateRecordRequest record =
                    RepoCreateRecordRequest.builder()
                            .accessJwt(request.getAccessJwt())
                            .repo(request.getDid())
                            .collection(BlueskyTypes.GraphBlock)
                            .record(request.toBlock())
                            .build();

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(xrpc(this.uri) + ATProtocolTypes.RepoCreateRecord)
                    .header("Authorization", request.getBearerToken())
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(record.toJson()))
                    .build();

            return HttpClient.newHttpClient().sendAsync(httpRequest, BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(Response::fromJson);
        });
    }

    @Override
    public Response<Void> deleteBlock(
            GraphDeleteBlockRequest request
    ) {
        return proceed(() -> {

            RepoDeleteRecordRequest record =
                    RepoDeleteRecordRequest.builder()
                            .accessJwt(request.getAccessJwt())
                            .repo(request.getDid())
                            .collection(BlueskyTypes.GraphBlock)
                            .rkey(request.getRkey())
                            .build();

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(xrpc(this.uri) + ATProtocolTypes.RepoDeleteRecord)
                    .header("Authorization", request.getBearerToken())
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(record.toJson()))
                    .build();

            return HttpClient.newHttpClient().sendAsync(httpRequest, BodyHandlers.ofString())
                    .thenApply(HttpResponse::statusCode)
                    .thenApply(code -> code == 200 ? CompletableFuture.completedFuture(null) : CompletableFuture.failedFuture(new RuntimeException("Failed to delete block")));
        });
    }

    @Override
    public Response<GraphGetBlocksResponse> getBlocks(
            GraphGetBlocksRequest request
    ) {
        return proceed(GraphGetBlocksResponse.class, () -> {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(xrpc(this.uri) + BlueskyTypes.GraphGetBlocks)
                    .header("Authorization", request.getBearerToken())
                    .GET()
                    .build();

            request.toMap().forEach((key, value) -> httpRequest = httpRequest.uri(xrpc(this.uri) + BlueskyTypes.GraphGetBlocks + "?" + key + "=" + value));

            return HttpClient.newHttpClient().sendAsync(httpRequest, BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(Response::fromJson);
        });
    }
}
