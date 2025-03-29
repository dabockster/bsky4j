package bsky4j.internal.atproto;

import static bsky4j.internal.share._InternalUtility.proceed;
import static bsky4j.internal.share._InternalUtility.xrpc;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import bsky4j.ATProtocolTypes;
import bsky4j.api.atproto.ServerResource;
import bsky4j.api.entity.atproto.server.ServerCreateSessionRequest;
import bsky4j.api.entity.atproto.server.ServerCreateSessionResponse;
import bsky4j.api.entity.atproto.server.ServerGetSessionResponse;
import bsky4j.api.entity.atproto.server.ServerRefreshSessionResponse;
import bsky4j.api.entity.share.AuthRequest;
import bsky4j.api.entity.share.Response;
import bsky4j.util.cache.CacheManager;

import java.util.concurrent.TimeUnit;

/**
 * Optimized server resource implementation with improved performance and security.
 */
public class _ServerResource implements ServerResource {
    private static final String SESSION_CACHE_KEY = "session";
    private static final String TOKEN_CACHE_KEY = "token";
    
    private final String uri;
    private final CacheManager cache;
    
    public _ServerResource(String uri) {
        this.uri = uri;
        this.cache = CacheManager.getInstance();
    }
    
    @Override
    public void createAccount() {
        throw new IllegalStateException("not implemented.");
    }
    
    @Override
    public void createInviteCode() {
        throw new IllegalStateException("not implemented.");
    }
    
    @Override
    public Response<ServerCreateSessionResponse> createSession(ServerCreateSessionRequest request) {
        return proceed(ServerCreateSessionResponse.class, () -> {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(xrpc(this.uri))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(request.toJson()));
            
            return HttpClient.newHttpClient().send(builder.build(), BodyHandlers.ofString());
        }).thenApply(response -> {
            if (response.statusCode() == 200) {
                // Cache the session response
                cache.put(SESSION_CACHE_KEY, response.body(), 5, TimeUnit.MINUTES);
                return Response.success(ServerCreateSessionResponse.fromJson(response.body()));
            }
            return Response.error(response.statusCode(), response.body());
        });
    }
    
    @Override
    public void deleteAccount() {
        throw new IllegalStateException("not implemented.");
    }
    
    @Override
    public Response<Void> deleteSession(AuthRequest request) {
        return proceed(() -> {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(xrpc(this.uri))
                    .header("Authorization", request.getBearerToken())
                    .POST(BodyPublishers.noBody());
            
            return HttpClient.newHttpClient().send(builder.build(), BodyHandlers.discarding());
        }).thenApply(response -> {
            if (response.statusCode() == 200) {
                // Clear cached session and token
                cache.remove(SESSION_CACHE_KEY);
                cache.remove(TOKEN_CACHE_KEY);
            }
            return Response.fromStatusCode(response.statusCode());
        });
    }
    
    @Override
    public void describeServer() {
        throw new IllegalStateException("not implemented.");
    }
    
    @Override
    public Response<ServerGetSessionResponse> getSession(AuthRequest request) {
        return proceed(ServerGetSessionResponse.class, () -> {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(xrpc(this.uri))
                    .header("Authorization", request.getBearerToken())
                    .GET();
            
            return HttpClient.newHttpClient().send(builder.build(), BodyHandlers.ofString());
        }).thenApply(response -> {
            if (response.statusCode() == 200) {
                // Cache the session response
                cache.put(SESSION_CACHE_KEY, response.body(), 5, TimeUnit.MINUTES);
                return Response.success(ServerGetSessionResponse.fromJson(response.body()));
            }
            return Response.error(response.statusCode(), response.body());
        });
    }
    
    @Override
    public Response<ServerRefreshSessionResponse> refreshSession(AuthRequest request) {
        return proceed(ServerRefreshSessionResponse.class, () -> {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(xrpc(this.uri))
                    .header("Authorization", request.getBearerToken())
                    .POST(BodyPublishers.noBody());
            
            return HttpClient.newHttpClient().send(builder.build(), BodyHandlers.ofString());
        }).thenApply(response -> {
            if (response.statusCode() == 200) {
                // Update cached token
                cache.put(TOKEN_CACHE_KEY, request.getAccessJwt(), 5, TimeUnit.MINUTES);
                return Response.success(ServerRefreshSessionResponse.fromJson(response.body()));
            }
            return Response.error(response.statusCode(), response.body());
        });
    }
    
    @Override
    public void requestAccountDelete() {
        throw new IllegalStateException("not implemented.");
    }
    
    @Override
    public void requestPasswordReset() {
        throw new IllegalStateException("not implemented.");
    }
    
    @Override
    public void resetPassword() {
        throw new IllegalStateException("not implemented.");
    }
}
