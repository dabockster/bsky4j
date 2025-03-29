package bsky4j.api.entity.lexicon;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class LexiconSchema {
    @SerializedName("$schema")
    private String schema;

    @SerializedName("$id")
    private String id;

    @SerializedName("$defs")
    private Map<String, Object> definitions;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(Map<String, Object> definitions) {
        this.definitions = definitions;
    }
}
