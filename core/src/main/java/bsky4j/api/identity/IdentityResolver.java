package bsky4j.api.identity;

import bsky4j.model.atproto.identity.IdentityResolveRequest;
import bsky4j.model.atproto.identity.IdentityResolveResponse;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class IdentityResolver {
    private final XRPCClient xrpcClient;

    public IdentityResolver(URI baseUri, String authorization) {
        this.xrpcClient = new XRPCClient(baseUri, authorization);
    }

    public CompletableFuture<IdentityResolveResponse> resolveIdentity(IdentityResolveRequest request) {
        return xrpcClient.call("identity.resolve", request.toMap(), IdentityResolveResponse.class);
    }
}
