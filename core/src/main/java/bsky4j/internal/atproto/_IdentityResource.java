package bsky4j.internal.atproto;

import bsky4j.api.entity.atproto.IdentityResolveHandleRequest;
import bsky4j.api.entity.atproto.IdentityResolveHandleResponse;
import bsky4j.api.entity.share.Response;
import bsky4j.internal._ATProtocol;
import com.google.gson.Gson;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class _IdentityResource implements IdentityResource {

    private final _ATProtocol atProtocol;
    private final HttpClient httpClient;
    private final Gson gson;

    public _IdentityResource(_ATProtocol atProtocol) {
        this.atProtocol = atProtocol;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    @Override
    public Response<IdentityResolveHandleResponse> resolveHandle(IdentityResolveHandleRequest request) {
        try {
            String json = gson.toJson(request.toMap());
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(atProtocol.getBaseUrl() + "/xrpc/com.atproto.identity.resolveHandle"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            IdentityResolveHandleResponse result = gson.fromJson(response.body(), IdentityResolveHandleResponse.class);
            return Response.of(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve handle", e);
        }
    }

    @Override
    public void updateHandle() {
        // TODO: Implement updateHandle
    }
}
