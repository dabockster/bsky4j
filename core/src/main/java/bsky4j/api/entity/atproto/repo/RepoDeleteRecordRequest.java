package bsky4j.api.entity.atproto.repo;

import bsky4j.api.entity.share.AuthRequest;
import bsky4j.api.entity.share.MapRequest;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class RepoDeleteRecordRequest extends AuthRequest implements MapRequest {

    RepoDeleteRecordRequest(String accessJwt) {
        super(accessJwt);
    }

    /**
     * The handle or DID of the repo.
     */
    private String repo;

    /**
     * The NSID of the record collection.
     */
    private String collection;

    /**
     * The key of the record.
     */
    private String rkey;

    /**
     * Compare and swap with the previous record by rid.
     */
    @Nullable
    private String swapRecord;

    /**
     * Compare and swap with the previous commit by cid.
     */
    @Nullable
    private String swapCommit;

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getRkey() {
        return rkey;
    }

    public void setRkey(String rkey) {
        this.rkey = rkey;
    }

    @Nullable
    public String getSwapRecord() {
        return swapRecord;
    }

    public void setSwapRecord(@Nullable String swapRecord) {
        this.swapRecord = swapRecord;
    }

    @Nullable
    public String getSwapCommit() {
        return swapCommit;
    }

    public void setSwapCommit(@Nullable String swapCommit) {
        this.swapCommit = swapCommit;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("repo", repo);
        map.put("collection", collection);
        map.put("rkey", rkey);
        if (swapRecord != null) {
            map.put("swapRecord", swapRecord);
        }
        if (swapCommit != null) {
            map.put("swapCommit", swapCommit);
        }
        return map;
    }

    // region
    public static RepoDeleteRecordRequestBuilder builder(String accessJwt) {
        return new RepoDeleteRecordRequestBuilder(accessJwt);
    }

    public static final class RepoDeleteRecordRequestBuilder {
        private final String accessJwt;
        private String repo;
        private String collection;
        private String rkey;
        @Nullable
        private String swapRecord;
        @Nullable
        private String swapCommit;

        private RepoDeleteRecordRequestBuilder(String accessJwt) {
            this.accessJwt = accessJwt;
        }

        public RepoDeleteRecordRequestBuilder repo(String repo) {
            this.repo = repo;
            return this;
        }

        public RepoDeleteRecordRequestBuilder collection(String collection) {
            this.collection = collection;
            return this;
        }

        public RepoDeleteRecordRequestBuilder rkey(String rkey) {
            this.rkey = rkey;
            return this;
        }

        public RepoDeleteRecordRequestBuilder swapRecord(@Nullable String swapRecord) {
            this.swapRecord = swapRecord;
            return this;
        }

        public RepoDeleteRecordRequestBuilder swapCommit(@Nullable String swapCommit) {
            this.swapCommit = swapCommit;
            return this;
        }

        public RepoDeleteRecordRequest build() {
            RepoDeleteRecordRequest repoDeleteRecordRequest = new RepoDeleteRecordRequest(accessJwt);
            repoDeleteRecordRequest.repo = this.repo;
            repoDeleteRecordRequest.collection = this.collection;
            repoDeleteRecordRequest.rkey = this.rkey;
            repoDeleteRecordRequest.swapRecord = this.swapRecord;
            repoDeleteRecordRequest.swapCommit = this.swapCommit;
            return repoDeleteRecordRequest;
        }
    }
    // endregion
}
