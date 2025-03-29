package bsky4j.model.atproto.graph;

import com.google.gson.annotations.SerializedName;

public class GraphEdge {
    @SerializedName("$type")
    private String type = "app.bsky.graph.edge";

    private String from;
    private String to;
    @SerializedName("type")
    private String edgeType;
    private String createdAt;

    public String getType() {
        return type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getEdgeType() {
        return edgeType;
    }

    public void setEdgeType(String edgeType) {
        this.edgeType = edgeType;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
