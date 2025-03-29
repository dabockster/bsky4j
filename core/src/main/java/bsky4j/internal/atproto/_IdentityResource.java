package bsky4j.internal.atproto;

import bsky4j.ATProtocolTypes;
import bsky4j.api.atproto.IdentityResource;
import bsky4j.api.entity.atproto.identity.IdentityResolveHandleRequest;
import bsky4j.api.entity.atproto.identity.IdentityResolveHandleResponse;
import bsky4j.api.entity.share.Response;
import net.socialhub.http.HttpMediaType;
import net.socialhub.http.HttpRequestBuilder;

import static bsky4j.internal.share._InternalUtility.proceed;
import static bsky4j.internal.share._InternalUtility.xrpc;

public class _IdentityResource implements IdentityResource {

    private final String uri;

    public _IdentityResource(String uri) {
        this.uri = uri;
    0   }

    @Override
    1   public Response<IdentityResolveHandleResponse> resolveHandle(
    2           IdentityResolveHandleRequest request
    3   ) {
    4       return proceed(IdentityResolveHandleResponse.class, () -> {
    5           HttpRequestBuilder builder =
    6                   new HttpRequestBuilder()
    7                           .target(xrpc(this.uri))
    8                           .path(ATProtocolTypes.IdentifyResolveHandle)
    9                           .request(HttpMediaType.APPLICATION_JSON);
   10 
   11         request.toMap().forEach(builder::param);
   12         return builder.get();
   13     });
   14 }
   15 
   16     @Override
   17     public void updateHandle() {
   18         // TODO: Implement updateHandle
   19         // Assuming the API endpoint is com.atproto.identity.updateHandle
   20         // and it requires a request object.
   21         // Response<?> response = proceed(Response.class, () -> {
   22         //     HttpRequestBuilder builder = new HttpRequestBuilder()
   23         //             .target(xrpc(this.uri))
   24         //             .path(ATProtocolTypes.IdentityUpdateHandle) // Assuming this constant exists
   25         //             .request(HttpMediaType.APPLICATION_JSON);
   26         //     // Add request parameters here
   27         //     return builder.post(); // Or PUT, depending on the API
   28         // });
   29         // return response;
   30     }
   31 }
