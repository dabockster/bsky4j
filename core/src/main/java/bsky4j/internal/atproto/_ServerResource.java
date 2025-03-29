package bsky4j.internal.atproto;

import static bsky4j.internal.share._InternalUtility.proceed;
import static bsky4j.internal.share._InternalUtility.xrpc;

import net.socialhub.http.HttpMediaType;
import net.socialhub.http.HttpRequestBuilder;

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
            HttpRequestBuilder builder = new HttpRequestBuilder()
                    .target(xrpc(this.uri))
                    .path(ATProtocolTypes.ServerCreateSession)
                    .request(HttpMediaType.APPLICATION_JSON)
                    .json(request.toJson());
            
            return builder.post();
        }).thenApply(response -> {
            if (response.isSuccess()) {
                // Cache the session response
                cache.put(SESSION_CACHE_KEY, response.get(), 5, TimeUnit.MINUTES);
                return response;
            }
            return response;
        });
    }
    
    @Override
    public void deleteAccount() {
        throw new IllegalStateException("not implemented.");
    }
    
    @Override
    public Response<Void> deleteSession(AuthRequest request) {
        return proceed(() -> {
            HttpRequestBuilder builder = new HttpRequestBuilder()
                    .target(xrpc(this.uri))
                    .path(ATProtocolTypes.ServerDeleteSession)
                    .request(HttpMediaType.APPLICATION_JSON)
                    .header("Authorization", request.getBearerToken());
            
            return builder.post();
        }).thenApply(response -> {
            if (response.isSuccess()) {
                // Clear cached session and token
                cache.remove(SESSION_CACHE_KEY);
                cache.remove(TOKEN_CACHE_KEY);
            }
            return response;
        });
    }
    
    @Override
    public void describeServer() {
        throw new IllegalStateException("not implemented.");
    }
    
    @Override
    public Response<ServerGetSessionResponse> getSession(AuthRequest request) {
        return proceed(ServerGetSessionResponse.class, () -> {
            HttpRequestBuilder builder = new HttpRequestBuilder()
                    .target(xrpc(this.uri))
                    .path(ATProtocolTypes.ServerGetSession)
                    .request(HttpMediaType.APPLICATION_JSON)
                    .header("Authorization", request.getBearerToken());
            
            return builder.get();
        }).thenApply(response -> {
            if (response.isSuccess()) {
                // Cache the session response
                cache.put(SESSION_CACHE_KEY, response.get(), 5, TimeUnit.MINUTES);
            }
            return response;
        });
    }
    
    @Override
    public Response<ServerRefreshSessionResponse> refreshSession(AuthRequest request) {
        return proceed(ServerRefreshSessionResponse.class, () -> {
            HttpRequestBuilder builder = new HttpRequestBuilder()
                    .target(xrpc(this.uri))
                    .path(ATProtocolTypes.ServerRefreshSession)
                    .request(HttpMediaType.APPLICATION_JSON)
                    .header("Authorization", request.getBearerToken());
            
            return builder.post();
        }).thenApply(response -> {
            if (response.isSuccess()) {
                // Update cached token
                cache.put(TOKEN_CACHE_KEY, request.getAccessJwt(), 5, TimeUnit.MINUTES);
            }
            return response;
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
