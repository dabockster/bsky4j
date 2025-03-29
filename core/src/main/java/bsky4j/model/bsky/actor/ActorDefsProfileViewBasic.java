package bsky4j.model.bsky.actor;

import bsky4j.model.atproto.label.LabelDefsLabel;
import bsky4j.model.share.Blob;
import java.util.List;

/**
 * A reference to an actor in the network.
 */
public class ActorDefsProfileViewBasic {

    private String did;
    private String handle;
    private String displayName;
    private bsky4j.model.share.Blob avatar;
    private ActorDefsViewerState viewer;
    private List<LabelDefsLabel> labels;

    // region
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

    public bsky4j.model.share.Blob getAvatar() {
        return avatar;
    }

    public void setAvatar(bsky4j.model.share.Blob avatar) {
        this.avatar = avatar;
    }

    public ActorDefsViewerState getViewer() {
        return viewer;
    }

    public void setViewer(ActorDefsViewerState viewer) {
        this.viewer = viewer;
    }

    public List<LabelDefsLabel> getLabels() {
        return labels;
    }

    public void setLabels(List<LabelDefsLabel> labels) {
        this.labels = labels;
    }
    // endregion
}
