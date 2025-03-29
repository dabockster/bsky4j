package bsky4j.model.atproto.lexicon;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class LexiconParseRequest {
    @SerializedName("$schema")
    private String schema;

    public LexiconParseRequest(String schema) {
        this.schema = schema;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public Map<String, Object> toMap() {
        return Map.of("$schema", schema);
    }
}
