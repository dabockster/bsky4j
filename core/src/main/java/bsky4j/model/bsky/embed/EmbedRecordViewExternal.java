package bsky4j.model.bsky.embed;

import com.google.gson.annotations.SerializedName;

public class EmbedRecordViewExternal extends EmbedRecordViewRecord {
    public static final String TYPE = "app.bsky.embed.record#viewExternal";

    @SerializedName("$type")
    private String type = TYPE;

    public String getType() {
        return type;
    }
}
