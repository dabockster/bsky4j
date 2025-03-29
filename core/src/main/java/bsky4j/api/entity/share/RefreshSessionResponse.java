package bsky4j.api.entity.share;

import com.google.gson.annotations.SerializedName;

/**
 * Response object for token refresh operations.
 */
public class RefreshSessionResponse {
    @SerializedName("accessJwt")
    private String accessJwt;
    
    @SerializedName("refreshJwt")
    private String refreshJwt;
    
    @SerializedName("handle")
    private String handle;
    
    @SerializedName("did")
    private String did;
    
    public String getAccessJwt() {
        return accessJwt;
    }
    
    public String getRefreshJwt() {
        return refreshJwt;
    }
    
    public String getHandle() {
        return handle;
    }
    
    public String getDid() {
        return did;
    }
}
