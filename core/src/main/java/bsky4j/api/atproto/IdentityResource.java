package bsky4j.api.atproto;

import bsky4j.api.entity.atproto.identity.IdentityGetRecommendedDidCredentialsResponse;
import bsky4j.api.entity.atproto.identity.IdentityRefreshIdentityRequest;
import bsky4j.api.entity.atproto.identity.IdentityRefreshIdentityResponse;
import bsky4j.api.entity.atproto.identity.IdentityResolveDidRequest;
import bsky4j.api.entity.atproto.identity.IdentityResolveDidResponse;
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
     * Resolves a DID.
     */
    Response<IdentityResolveDidResponse> resolveDid(IdentityResolveDidRequest request);

    /**
     * Get recommended DID credentials.
     */
    Response<IdentityGetRecommendedDidCredentialsResponse> getRecommendedDidCredentials();

    /**
     * Updates the handle of the account.
     */
    void updateHandle();

    /**
     * Request that the server re-resolve an identity (DID and handle).
     */
    Response<IdentityRefreshIdentityResponse> refreshIdentity(IdentityRefreshIdentityRequest request);

    /**
     * Request a PLC operation signature.
     */
    void requestPlcOperationSignature();

    /**
     * Sign a PLC operation.
     */
    void signPlcOperation();

    /**
     * Submit a PLC operation.
     */
    void submitPlcOperation();

    /**
     * Resolves an identity (DID or Handle) to a full identity (DID document and verified handle).
     */
    Response<IdentityResolveHandleResponse> resolveIdentity();
}
