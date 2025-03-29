package bsky4j.model.bsky.richtext;

import com.google.gson.annotations.SerializedName;

public class RichtextFacetFeatureMention extends RichtextFacetFeature {
    public static final String TYPE = "app.bsky.richtext.facet#mention";

    @SerializedName("$type")
    private String type = TYPE;

    public String getType() {
        return type;
    }
}
