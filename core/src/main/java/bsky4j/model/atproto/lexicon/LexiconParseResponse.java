package bsky4j.model.atproto.lexicon;

import com.google.gson.annotations.SerializedName;

public class LexiconParseResponse {
    @SerializedName("$schema")
    private String schema;

    @SerializedName("$defs")
    private Map<String, Object> definitions;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public Map<String, Object> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(Map<String, Object> definitions) {
        this.definitions = definitions;
    }

    public static LexiconParseResponse fromJson(String json) {
        return new Gson().fromJson(json, LexiconParseResponse.class);
    }
}
