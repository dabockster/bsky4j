package bsky4j.api.atproto;

import bsky4j.api.entity.atproto.repo.RepoCreateRecordRequest;
import bsky4j.api.entity.atproto.repo.RepoCreateRecordResponse;
import bsky4j.api.entity.atproto.repo.RepoDeleteRecordRequest;
import bsky4j.api.entity.atproto.repo.RepoGetRecordRequest;
import bsky4j.api.entity.atproto.repo.RepoGetRecordResponse;
import bsky4j.api.entity.atproto.repo.RepoListRecordsRequest;
import bsky4j.api.entity.atproto.repo.RepoApplyWritesRequest;
import bsky4j.api.entity.atproto.repo.RepoDescribeRepoResponse;
import bsky4j.api.entity.atproto.repo.RepoCreateRecordRequest;
import bsky4j.api.entity.atproto.repo.RepoCreateRecordResponse;
import bsky4j.api.entity.atproto.repo.RepoDeleteRecordRequest;
import bsky4j.api.entity.atproto.repo.RepoGetRecordRequest;
import bsky4j.api.entity.atproto.repo.RepoGetRecordResponse;
import bsky4j.api.entity.atproto.repo.RepoListMissingBlobsRequest;
import bsky4j.api.entity.atproto.repo.RepoListMissingBlobsResponse;
import bsky4j.api.entity.atproto.repo.RepoListRecordsRequest;
import bsky4j.api.entity.atproto.repo.RepoListRecordsResponse;
import bsky4j.api.entity.atproto.repo.RepoPutRecordRequest;
import bsky4j.api.entity.atproto.repo.RepoUploadBlobRequest;
import bsky4j.api.entity.atproto.repo.RepoUploadBlobResponse;
import bsky4j.api.entity.share.Response;

/**
 * ATProtocol/Repo
 * <a href="https://atproto.com/lexicons/com-atproto-repo">Reference</a>
 */
public interface RepoResource {

    /**
     * Apply a batch transaction of creates, updates, and deletes.
     */
    Response<Void> applyWrites(RepoApplyWritesRequest request);

    /**
     * Create a new record.
     */
    Response<RepoCreateRecordResponse> createRecord(RepoCreateRecordRequest request);

    /**
     * Delete a record, or ensure it doesn't exist.
     */
    Response<Void> deleteRecord(RepoDeleteRecordRequest request);

    /**
     * Get information about the repo, including the list of collections.
     */
    Response<RepoDescribeRepoResponse> describeRepo(String repo);

    /**
     * Get a record.
     */
    Response<RepoGetRecordResponse> getRecord(RepoGetRecordRequest request);

    /**
     * List missing blobs in a repo.
     */
    Response<RepoListMissingBlobsResponse> listMissingBlobs(RepoListMissingBlobsRequest request);

    /**
     * List a range of records in a collection.
     */
    Response<RepoListRecordsResponse> listRecords(RepoListRecordsRequest request);

    /**
     * Write a record, creating or updating it as needed.
     */
    Response<Void> putRecord(RepoPutRecordRequest request);

    /**
     * Upload a new blob to be added to repo in a later request.
     */
    Response<RepoUploadBlobResponse> uploadBlob(RepoUploadBlobRequest request);
}
