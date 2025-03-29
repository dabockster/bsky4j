package bsky4j.model.atproto.graph;

import com.google.gson.annotations.SerializedName;

public class GraphNode {
    @SerializedName("$type")
    private String type = "app.bsky.graph.node";

    private String did;
    private String handle;
    private String displayName;
    private String avatar;
    private String description;

    public String getType() {
        return type;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
