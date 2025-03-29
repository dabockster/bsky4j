package bsky4j.internal.bsky;

import bsky4j.ATProtocolTypes;
import bsky4j.BlueskyTypes;
import bsky4j.api.bsky.FeedResource;
import bsky4j.api.entity.atproto.repo.RepoCreateRecordRequest;
import bsky4j.api.entity.atproto.repo.RepoDeleteRecordRequest;
import bsky4j.api.entity.bsky.feed.*;
import bsky4j.api.entity.share.Response;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static bsky4j.internal.share._InternalUtility.proceed;
import static bsky4j.internal.share._InternalUtility.xrpc;

public class _FeedResource implements FeedResource {

    private final String uri;

    public _FeedResource(String uri) {
        this.uri = uri;
    }

    @Override
    public Response<FeedGetAuthorFeedResponse> getAuthorFeed(
            FeedGetAuthorFeedRequest request
    ) {
        return proceed(FeedGetAuthorFeedResponse.class, () -> {

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri) + "/" + BlueskyTypes.FeedGetAuthorFeed)
                            .header("Authorization", request.getBearerToken());

            request.toMap().forEach((key, value) -> builder.header(key, value));

            return HttpClient.newHttpClient().sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .join();
        });
    }

    @Override
    public Response<FeedGetLikesResponse> getLikes(
            FeedGetLikesRequest request
    ) {
        return proceed(FeedGetLikesResponse.class, () -> {

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri) + "/" + BlueskyTypes.FeedGetLikes)
                            .header("Authorization", request.getBearerToken());

            request.toMap().forEach((key, value) -> builder.header(key, value));

            return HttpClient.newHttpClient().sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .join();
        });
    }

    @Override
    public Response<FeedGetPostThreadResponse> getPostThread(
            FeedGetPostThreadRequest request
    ) {
        return proceed(FeedGetPostThreadResponse.class, () -> {

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri) + "/" + BlueskyTypes.FeedGetPostThread)
                            .header("Authorization", request.getBearerToken());

            request.toMap().forEach((key, value) -> builder.header(key, value));

            return HttpClient.newHttpClient().sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .join();
        });
    }

    @Override
    public Response<FeedGetPostsResponse> getPosts(
            FeedGetPostsRequest request
    ) {
        return proceed(FeedGetPostsResponse.class, () -> {

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri) + "/" + BlueskyTypes.FeedGetPosts)
                            .header("Authorization", request.getBearerToken());

            request.getUris().forEach(u -> builder.header("uris", u));

            return HttpClient.newHttpClient().sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .join();
        });
    }

    @Override
    public Response<FeedSearchPostsResponse> searchPosts(
            FeedSearchPostsRequest request
    ) {
        return proceed(FeedSearchPostsResponse.class, () -> {

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri) + "/" + BlueskyTypes.FeedGetFeedSearchPosts)
                            .header("Authorization", request.getBearerToken());

            request.toMap().forEach((key, value) -> builder.header(key, value));

            return HttpClient.newHttpClient().sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .join();
        });
    }

    @Override
    public Response<FeedGetRepostedByResponse> getRepostedBy(
            FeedGetRepostedByRequest request
    ) {
        return proceed(FeedGetRepostedByResponse.class, () -> {

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri) + "/" + BlueskyTypes.FeedGetRepostedBy)
                            .header("Authorization", request.getBearerToken());

            request.toMap().forEach((key, value) -> builder.header(key, value));

            return HttpClient.newHttpClient().sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .join();
        });
    }

    @Override
    public Response<FeedGetTimelineResponse> getTimeline(
            FeedGetTimelineRequest request
    ) {
        return proceed(FeedGetTimelineResponse.class, () -> {

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri) + "/" + BlueskyTypes.FeedGetTimeline)
                            .header("Authorization", request.getBearerToken());

            request.toMap().forEach((key, value) -> builder.header(key, value));

            return HttpClient.newHttpClient().sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .join();
        });
    }

    @Override
    public Response<FeedGetFeedResponse> getFeed(
            FeedGetFeedRequest request
    ) {
        return proceed(FeedGetFeedResponse.class, () -> {

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri) + "/" + BlueskyTypes.FeedGetFeed)
                            .header("Authorization", request.getBearerToken());

            request.toMap().forEach((key, value) -> builder.header(key, value));

            return HttpClient.newHttpClient().sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .join();
        });
    }

    @Override
    public Response<FeedGetActorFeedsResponse> getActorFeeds(
            FeedGetActorFeedsRequest request
    ) {
        return proceed(FeedGetActorFeedsResponse.class, () -> {

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri) + "/" + BlueskyTypes.FeedGetActorFeeds)
                            .header("Authorization", request.getBearerToken());

            request.toMap().forEach((key, value) -> builder.header(key, value));

            return HttpClient.newHttpClient().sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .join();
        });
    }

    @Override
    public Response<FeedGetActorLikesResponse> getActorLikes(
            FeedGetActorLikesRequest request
    ) {
        return proceed(FeedGetActorLikesResponse.class, () -> {

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri) + "/" + BlueskyTypes.FeedGetActorLikes)
                            .header("Authorization", request.getBearerToken());

            request.toMap().forEach((key, value) -> builder.header(key, value));

            return HttpClient.newHttpClient().sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .join();
        });
    }

    @Override
    public Response<FeedGetFeedGeneratorResponse> getFeedGenerator(
            FeedGetFeedGeneratorRequest request
    ) {
        return proceed(FeedGetFeedGeneratorResponse.class, () -> {

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri) + "/" + BlueskyTypes.FeedGetFeedGenerator)
                            .header("Authorization", request.getBearerToken());

            request.toMap().forEach((key, value) -> builder.header(key, value));

            return HttpClient.newHttpClient().sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .join();
        });
    }

    @Override
    public Response<FeedGetFeedGeneratorsResponse> getFeedGenerators(
            FeedGetFeedGeneratorsRequest request
    ) {
        return proceed(FeedGetFeedGeneratorsResponse.class, () -> {

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri) + "/" + BlueskyTypes.FeedGetFeedGenerators)
                            .header("Authorization", request.getBearerToken());

            request.getFeeds().forEach(u -> builder.header("feeds", u));

            return HttpClient.newHttpClient().sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .join();
        });
    }

    @Override
    public Response<FeedLikeResponse> like(
            FeedLikeRequest request
    ) {
        return proceed(FeedLikeResponse.class, () -> {

            RepoCreateRecordRequest record =
                    RepoCreateRecordRequest.builder()
                            .accessJwt(request.getAccessJwt())
                            .repo(request.getDid())
                            .collection(BlueskyTypes.FeedLike)
                            .record(request.toLike())
                            .build();

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri) + "/" + ATProtocolTypes.RepoCreateRecord)
                            .header("Authorization", request.getBearerToken())
                            .header("Content-Type", "application/json");

            return HttpClient.newHttpClient().sendAsync(builder.POST(HttpRequest.BodyPublishers.ofString(record.toJson())), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .join();
        });
    }

    @Override
    public Response<Void> deleteLike(
            FeedDeleteLikeRequest request
    ) {
        return proceed(() -> {

            RepoDeleteRecordRequest record =
                    RepoDeleteRecordRequest.builder()
                            .accessJwt(request.getAccessJwt())
                            .repo(request.getDid())
                            .collection(BlueskyTypes.FeedLike)
                            .rkey(request.getRkey())
                            .build();

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri) + "/" + ATProtocolTypes.RepoDeleteRecord)
                            .header("Authorization", request.getBearerToken())
                            .header("Content-Type", "application/json");

            return HttpClient.newHttpClient().sendAsync(builder.POST(HttpRequest.BodyPublishers.ofString(record.toJson())), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .join();
        });
    }

    @Override
    public Response<FeedPostResponse> post(
            FeedPostRequest request
    ) {
        return proceed(FeedPostResponse.class, () -> {

            RepoCreateRecordRequest record =
                    RepoCreateRecordRequest.builder()
                            .accessJwt(request.getAccessJwt())
                            .repo(request.getDid())
                            .collection(BlueskyTypes.FeedPost)
                            .record(request.toPost())
                            .build();

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri) + "/" + ATProtocolTypes.RepoCreateRecord)
                            .header("Authorization", request.getBearerToken())
                            .header("Content-Type", "application/json");

            return HttpClient.newHttpClient().sendAsync(builder.POST(HttpRequest.BodyPublishers.ofString(record.toJson())), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .join();
        });
    }

    @Override
    public Response<Void> deletePost(
            FeedDeletePostRequest request
    ) {
        return proceed(() -> {

            RepoDeleteRecordRequest record =
                    RepoDeleteRecordRequest.builder()
                            .accessJwt(request.getAccessJwt())
                            .repo(request.getDid())
                            .collection(BlueskyTypes.FeedPost)
                            .rkey(request.getRkey())
                            .build();

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri) + "/" + ATProtocolTypes.RepoDeleteRecord)
                            .header("Authorization", request.getBearerToken())
                            .header("Content-Type", "application/json");

            return HttpClient.newHttpClient().sendAsync(builder.POST(HttpRequest.BodyPublishers.ofString(record.toJson())), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .join();
        });
    }

    @Override
    public Response<FeedRepostResponse> repost(
            FeedRepostRequest request
    ) {
        return proceed(FeedRepostResponse.class, () -> {

            RepoCreateRecordRequest record =
                    RepoCreateRecordRequest.builder()
                            .accessJwt(request.getAccessJwt())
                            .repo(request.getDid())
                            .collection(BlueskyTypes.FeedRepost)
                            .record(request.toRepost())
                            .build();

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri) + "/" + ATProtocolTypes.RepoCreateRecord)
                            .header("Authorization", request.getBearerToken())
                            .header("Content-Type", "application/json");

            return HttpClient.newHttpClient().sendAsync(builder.POST(HttpRequest.BodyPublishers.ofString(record.toJson())), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .join();
        });
    }

    @Override
    public Response<Void> deleteRepost(
            FeedDeleteRepostRequest request
    ) {
        return proceed(() -> {

            RepoDeleteRecordRequest record =
                    RepoDeleteRecordRequest.builder()
                            .accessJwt(request.getAccessJwt())
                            .repo(request.getDid())
                            .collection(BlueskyTypes.FeedRepost)
                            .rkey(request.getRkey())
                            .build();

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri) + "/" + ATProtocolTypes.RepoDeleteRecord)
                            .header("Authorization", request.getBearerToken())
                            .header("Content-Type", "application/json");

            return HttpClient.newHttpClient().sendAsync(builder.POST(HttpRequest.BodyPublishers.ofString(record.toJson())), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .join();
        });
    }
}
