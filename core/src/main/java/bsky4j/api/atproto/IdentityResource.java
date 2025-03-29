package bsky4j.api.atproto;

import bsky4j.api.entity.atproto.identity.IdentityResolveHandleRequest;
import bsky4j.api.entity.atproto.identity.IdentityResolveHandleResponse;
import bsky4j.api.entity.share.Response;

/**
 * ATProtocol/Identity
 * <a href="https://atproto.com/lexicons/com-atproto-identity">Reference</a>
 */
public interface IdentityResource {

    /**
     * Provides the DID of a repo.
     */
    Response<IdentityResolveHandleResponse> resolveHandle(IdentityResolveHandleRequest request);

    /**
     * Resolves an identity (handle) to a full identity (DID document and verified handle).
     */
    Response<IdentityResolveHandleResponse> resolveIdentity(String handle);

    /**
     * Updates the handle of the account.
     */
    void updateHandle();
}
