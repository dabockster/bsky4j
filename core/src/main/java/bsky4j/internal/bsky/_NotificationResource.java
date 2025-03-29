package bsky4j.internal.bsky;

import bsky4j.BlueskyTypes;
import bsky4j.api.bsky.NotificationResource;
import bsky4j.api.entity.bsky.notification.NotificationGetUnreadCountRequest;
import bsky4j.api.entity.bsky.notification.NotificationGetUnreadCountResponse;
import bsky4j.api.entity.bsky.notification.NotificationListNotificationsRequest;
import bsky4j.api.entity.bsky.notification.NotificationListNotificationsResponse;
import bsky4j.api.entity.bsky.notification.NotificationUpdateSeenRequest;
import bsky4j.api.entity.share.Response;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.Map;

import static bsky4j.internal.share._InternalUtility.proceed;

public class _NotificationResource implements NotificationResource {

    private final String uri;
    private final HttpClient client;

    public _NotificationResource(String uri) {
        this.uri = uri;
        this.client = HttpClient.newHttpClient();
    }

    @Override
    public Response<NotificationGetUnreadCountResponse> getUnreadCount(
            NotificationGetUnreadCountRequest request
    ) {
        return proceed(NotificationGetUnreadCountResponse.class, () -> {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(xrpc(this.uri) + BlueskyTypes.NotificationGetUnreadCount))
                    .header("Authorization", request.getBearerToken())
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(httpRequest, BodyHandlers.ofString());
            return new Response<>(response.statusCode(), response.body());
        });
    }

    @Override
    public Response<NotificationListNotificationsResponse> listNotifications(
            NotificationListNotificationsRequest request
    ) {
        return proceed(NotificationListNotificationsResponse.class, () -> {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(xrpc(this.uri) + BlueskyTypes.NotificationListNotifications))
                    .header("Authorization", request.getBearerToken())
                    .GET();

            request.toMap().forEach((key, value) -> builder.header(key, value));
            HttpRequest httpRequest = builder.build();

            HttpResponse<String> response = client.send(httpRequest, BodyHandlers.ofString());
            return new Response<>(response.statusCode(), response.body());
        });
    }

    @Override
    public Response<Void> updateSeen(
            NotificationUpdateSeenRequest request
    ) {
        return proceed(() -> {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(xrpc(this.uri) + BlueskyTypes.NotificationUpdateSeen))
                    .header("Authorization", request.getBearerToken())
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(request.toJson()))
                    .build();

            HttpResponse<Void> response = client.send(httpRequest, BodyHandlers.discarding());
            return new Response<>(response.statusCode(), null);
        });
    }
}
