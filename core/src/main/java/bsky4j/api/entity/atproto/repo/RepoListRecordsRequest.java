package bsky4j.api.entity.atproto.repo;

import bsky4j.api.entity.share.MapRequest;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class RepoListRecordsRequest implements MapRequest {

    /**
     * The handle or DID of the repo.
     */
    private String repo;

    /**
     * The NSID of the record collection.
     */
    private String collection;

    /**
     * The minimum record RKey to include in the response.
     */
    @Nullable
    private String minRkey;

    /**
     * The maximum record RKey to include in the response.
     */
    @Nullable
    private String maxRkey;

    /**
     * The maximum number of records to return.
     */
    @Nullable
    private Integer limit;

    /**
     * The cursor for pagination.
     */
    @Nullable
    private String cursor;

    public RepoListRecordsRequest(String repo, String collection) {
        this.repo = repo;
        this.collection = collection;
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

    public String getMinRkey() {
        return minRkey;
    }

    public void setMinRkey(@Nullable String minRkey) {
        this.minRkey = minRkey;
    }

    public String getMaxRkey() {
        return maxRkey;
    }

    public void setMaxRkey(@Nullable String maxRkey) {
        this.maxRkey = maxRkey;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(@Nullable Integer limit) {
        this.limit = limit;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(@Nullable String cursor) {
        this.cursor = cursor;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("repo", repo);
        map.put("collection", collection);
        
        if (minRkey != null) {
            map.put("minRkey", minRkey);
        }
        
        if (maxRkey != null) {
            map.put("maxRkey", maxRkey);
        }
        
        if (limit != null) {
            map.put("limit", limit);
        }
        
        if (cursor != null) {
            map.put("cursor", cursor);
        }
        
        return map;
    }

    // region
    public static RepoListRecordsRequestBuilder builder() {
        return new RepoListRecordsRequestBuilder();
    }

    public static final class RepoListRecordsRequestBuilder {
        private String repo;
        private String collection;
        @Nullable
        private String minRkey;
        @Nullable
        private String maxRkey;
        @Nullable
        private Integer limit;
        @Nullable
        private String cursor;

        private RepoListRecordsRequestBuilder() {
        }

        public RepoListRecordsRequestBuilder repo(String repo) {
            this.repo = repo;
            return this;
        }

        public RepoListRecordsRequestBuilder collection(String collection) {
            this.collection = collection;
            return this;
        }

        public RepoListRecordsRequestBuilder minRkey(@Nullable String minRkey) {
            this.minRkey = minRkey;
            return this;
        }

        public RepoListRecordsRequestBuilder maxRkey(@Nullable String maxRkey) {
            this.maxRkey = maxRkey;
            return this;
        }

        public RepoListRecordsRequestBuilder limit(@Nullable Integer limit) {
            this.limit = limit;
            return this;
        }

        public RepoListRecordsRequestBuilder cursor(@Nullable String cursor) {
            this.cursor = cursor;
            return this;
        }

        public RepoListRecordsRequest build() {
            RepoListRecordsRequest repoListRecordsRequest = new RepoListRecordsRequest(repo, collection);
            repoListRecordsRequest.minRkey = this.minRkey;
            repoListRecordsRequest.maxRkey = this.maxRkey;
            repoListRecordsRequest.limit = this.limit;
            repoListRecordsRequest.cursor = this.cursor;
            return repoListRecordsRequest;
        }
    }
    // endregion
}
