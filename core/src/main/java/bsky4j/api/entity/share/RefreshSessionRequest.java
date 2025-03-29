package bsky4j.api.entity.share;

import com.google.gson.annotations.SerializedName;

/**
 * Request object for refreshing authentication tokens.
 */
public class RefreshSessionRequest {
    @SerializedName("refreshJwt")
    private final String refreshJwt;
    
    public RefreshSessionRequest(String refreshJwt) {
        this.refreshJwt = refreshJwt;
    }
    
    public String getRefreshJwt() {
        return refreshJwt;
    }
}
