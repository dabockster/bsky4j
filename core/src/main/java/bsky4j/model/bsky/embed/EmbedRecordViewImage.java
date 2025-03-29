package bsky4j.model.bsky.embed;

import com.google.gson.annotations.SerializedName;

public class EmbedRecordViewImage extends EmbedRecordViewRecord {
    public static final String TYPE = "app.bsky.embed.record#image";

    @SerializedName("$type")
    private String type = TYPE;

    public String getType() {
        return type;
    }
}
