package bsky4j.model.bsky.richtext;

import com.google.gson.annotations.SerializedName;

public class RichtextFacetFeatureTag extends RichtextFacetFeature {
    public static final String TYPE = "app.bsky.richtext.facet#tag";

    @SerializedName("$type")
    private String type = TYPE;

    public String getType() {
        return type;
    }
}
