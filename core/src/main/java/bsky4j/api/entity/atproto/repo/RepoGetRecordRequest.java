package bsky4j.api.entity.atproto.repo;

import bsky4j.api.entity.share.MapRequest;
import bsky4j.util.ATUriParser;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class RepoGetRecordRequest implements MapRequest {

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
     * (Library optional)
     */
    private String uri;

    /**
     * The CID of the version of the record. If not specified, then return the most recent version.
     */
    @Nullable
    private String cid;

    public RepoGetRecordRequest(String repo, String collection, String rkey) {
        this.repo = repo;
        this.collection = collection;
        this.rkey = rkey;
    }

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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(@Nullable String cid) {
        this.cid = cid;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("repo", repo);
        map.put("collection", collection);
        map.put("rkey", getRkey());
        
        if (cid != null) {
            map.put("cid", cid);
        }
        
        return map;
    }

    // region
    public static RepoGetRecordRequestBuilder builder() {
        return new RepoGetRecordRequestBuilder();
    }

    public static final class RepoGetRecordRequestBuilder {
        private String repo;
        private String collection;
        private String rkey;
        private String uri;
        private String cid;

        private RepoGetRecordRequestBuilder() {
        }

        public RepoGetRecordRequestBuilder repo(String repo) {
            this.repo = repo;
            return this;
        }

        public RepoGetRecordRequestBuilder collection(String collection) {
            this.collection = collection;
            return this;
        }

        public RepoGetRecordRequestBuilder rkey(String rkey) {
            this.rkey = rkey;
            return this;
        }

        public RepoGetRecordRequestBuilder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public RepoGetRecordRequestBuilder cid(@Nullable String cid) {
            this.cid = cid;
            return this;
        }

        public RepoGetRecordRequest build() {
            RepoGetRecordRequest repoGetRecordRequest = new RepoGetRecordRequest(repo, collection, rkey);
            repoGetRecordRequest.uri = this.uri;
            repoGetRecordRequest.cid = this.cid;
            return repoGetRecordRequest;
        }
    }
}
