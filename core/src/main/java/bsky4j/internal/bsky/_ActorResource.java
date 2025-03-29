package bsky4j.internal.bsky;

import bsky4j.BlueskyTypes;
import bsky4j.api.bsky.ActorResource;
import bsky4j.api.entity.bsky.actor.ActorGetPreferencesRequest;
import bsky4j.api.entity.bsky.actor.ActorGetPreferencesResponse;
import bsky4j.api.entity.bsky.actor.ActorGetProfileRequest;
import bsky4j.api.entity.bsky.actor.ActorGetProfileResponse;
import bsky4j.api.entity.bsky.actor.ActorGetProfilesRequest;
import bsky4j.api.entity.bsky.actor.ActorGetProfilesResponse;
import bsky4j.api.entity.bsky.actor.ActorSearchActorsRequest;
import bsky4j.api.entity.bsky.actor.ActorSearchActorsResponse;
import bsky4j.api.entity.share.Response;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;

import static bsky4j.internal.share._InternalUtility.proceed;
import static bsky4j.internal.share._InternalUtility.xrpc;

public class _ActorResource implements ActorResource {

    private final String uri;
    private final HttpClient client;

    public _ActorResource(String uri) {
        this.uri = uri;
        this.client = HttpClient.newHttpClient();
    }

    @Override
    public Response<ActorSearchActorsResponse> searchActors(
            ActorSearchActorsRequest request
    ) {
        return proceed(ActorSearchActorsResponse.class, () -> {

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri, BlueskyTypes.ActorSearchActors))
                            .header("Authorization", request.getBearerToken());

            request.toMap().forEach((key, value) -> {
                builder.header(key, value);
            });

            HttpRequest httpRequest = builder.build();

            HttpResponse<String> httpResponse = client.send(httpRequest, BodyHandlers.ofString());

            return new Response<>(httpResponse.statusCode(), httpResponse.body());
        });
    }

    @Override
    public Response<ActorGetProfileResponse> getProfile(
            ActorGetProfileRequest request
    ) {
        return proceed(ActorGetProfileResponse.class, () -> {

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri, BlueskyTypes.ActorGetProfile))
                            .header("Authorization", request.getBearerToken());

            request.toMap().forEach((key, value) -> {
                builder.header(key, value);
            });

            HttpRequest httpRequest = builder.build();

            HttpResponse<String> httpResponse = client.send(httpRequest, BodyHandlers.ofString());

            return new Response<>(httpResponse.statusCode(), httpResponse.body());
        });
    }

    @Override
    public Response<ActorGetProfilesResponse> getProfiles(
            ActorGetProfilesRequest request
    ) {
        return proceed(ActorGetProfilesResponse.class, () -> {

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri, BlueskyTypes.ActorGetProfiles))
                            .header("Authorization", request.getBearerToken());

            request.getActors().forEach((actor) -> {
                builder.header("actors", actor);
            });

            HttpRequest httpRequest = builder.build();

            HttpResponse<String> httpResponse = client.send(httpRequest, BodyHandlers.ofString());

            return new Response<>(httpResponse.statusCode(), httpResponse.body());
        });
    }

    @Override
    public Response<ActorGetPreferencesResponse> getPreferences(
            ActorGetPreferencesRequest request
    ) {
        return proceed(ActorGetPreferencesResponse.class, () -> {

            HttpRequest.Builder builder =
                    HttpRequest.newBuilder()
                            .uri(xrpc(this.uri, BlueskyTypes.ActorGetPreferences))
                            .header("Authorization", request.getBearerToken());

            request.toMap().forEach((key, value) -> {
                builder.header(key, value);
            });

            HttpRequest httpRequest = builder.build();

            HttpResponse<String> httpResponse = client.send(httpRequest, BodyHandlers.ofString());

            return new Response<>(httpResponse.statusCode(), httpResponse.body());
        });
    }
}
