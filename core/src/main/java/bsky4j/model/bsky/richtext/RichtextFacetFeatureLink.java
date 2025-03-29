package bsky4j.model.bsky.richtext;

import com.google.gson.annotations.SerializedName;

public class RichtextFacetFeatureLink extends RichtextFacetFeature {
    public static final String TYPE = "app.bsky.richtext.facet#link";

    @SerializedName("$type")
    private String type = TYPE;

    public String getType() {
        return type;
    }
}
