package bsky4j.model.bsky.actor;

import bsky4j.model.atproto.label.LabelDefsLabel;
import java.util.List;

public class ActorDefsViewerState {
    private Boolean muted;
    private Boolean blocked;
    private Boolean following;
    private Boolean followedBy;
    private Boolean blocking;
    private String indexedAt;
    private List<LabelDefsLabel> labels;

    public Boolean getMuted() {
        return muted;
    }

    public void setMuted(Boolean muted) {
        this.muted = muted;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    public Boolean getFollowing() {
        return following;
    }

    public void setFollowing(Boolean following) {
        this.following = following;
    }

    public Boolean getFollowedBy() {
        return followedBy;
    }

    public void setFollowedBy(Boolean followedBy) {
        this.followedBy = followedBy;
    }

    public Boolean getBlocking() {
        return blocking;
    }

    public void setBlocking(Boolean blocking) {
        this.blocking = blocking;
    }

    public String getIndexedAt() {
        return indexedAt;
    }

    public void setIndexedAt(String indexedAt) {
        this.indexedAt = indexedAt;
    }

    public List<LabelDefsLabel> getLabels() {
        return labels;
    }

    public void setLabels(List<LabelDefsLabel> labels) {
        this.labels = labels;
    }
}
