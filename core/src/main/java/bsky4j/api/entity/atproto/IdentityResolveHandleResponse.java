package bsky4j.api.entity.atproto;

import com.google.gson.annotations.SerializedName;

public class IdentityResolveHandleResponse {
    @SerializedName("did")
    private String did;

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public static IdentityResolveHandleResponse fromJson(String json) {
        return new Gson().fromJson(json, IdentityResolveHandleResponse.class);
    }
}
