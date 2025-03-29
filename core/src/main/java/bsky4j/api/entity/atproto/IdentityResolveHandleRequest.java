package bsky4j.api.entity.atproto;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class IdentityResolveHandleRequest {
    @SerializedName("handle")
    private String handle;

    public IdentityResolveHandleRequest(String handle) {
        this.handle = handle;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public Map<String, String> toMap() {
        return Map.of("handle", handle);
    }
}
