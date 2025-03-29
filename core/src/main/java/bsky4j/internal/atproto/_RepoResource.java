package bsky4j.internal.atproto;

import bsky4j.ATProtocolTypes;
import bsky4j.api.atproto.RepoResource;
import bsky4j.api.entity.atproto.repo.RepoCreateRecordRequest;
import bsky4j.api.entity.atproto.repo.RepoCreateRecordResponse;
import bsky4j.api.entity.atproto.repo.RepoDeleteRecordRequest;
import bsky4j.api.entity.atproto.repo.RepoGetRecordRequest;
import bsky4j.api.entity.atproto.repo.RepoGetRecordResponse;
import bsky4j.api.entity.atproto.repo.RepoListRecordsRequest;
import bsky4j.api.entity.atproto.repo.RepoListRecordsResponse;
import bsky4j.api.entity.atproto.repo.RepoUploadBlobByFileRequest;
import bsky4j.api.entity.atproto.repo.RepoUploadBlobByStreamRequest;
import bsky4j.api.entity.atproto.repo.RepoUploadBlobRequest;
import bsky4j.api.entity.atproto.repo.RepoUploadBlobResponse;
import bsky4j.api.entity.share.Response;
import bsky4j.util.Bsky4JClientConfiguration;

import java.io.File;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;

import static bsky4j.internal.share._InternalUtility.proceed;
import static bsky4j.internal.share._InternalUtility.xrpc;

public class _RepoResource implements RepoResource {

    private final String uri;

    public _RepoResource(String uri) {
        this.uri = uri;
    }


    @Override
    public void applyWrites() {
        throw new IllegalStateException("not implemented.");
    }

    @Override
    public Response<RepoCreateRecordResponse> createRecord(
            RepoCreateRecordRequest request
    ) {
        return proceed(RepoCreateRecordResponse.class, () -> {

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(xrpc(this.uri).resolve(ATProtocolTypes.RepoCreateRecord))
                    .header("Authorization", request.getBearerToken())
                    .header("Content-Type", "application/json");

            builder.POST(BodyPublishers.ofString(request.toJson()));

            return HttpClient.newHttpClient().send(builder.build(), BodyHandlers.ofString());
        });
    }

    @Override
    public Response<Void> deleteRecord(
            RepoDeleteRecordRequest request
    ) {
        return proceed(() -> {

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(xrpc(this.uri).resolve(ATProtocolTypes.RepoDeleteRecord))
                    .header("Authorization", request.getBearerToken())
                    .header("Content-Type", "application/json");

            builder.POST(BodyPublishers.ofString(request.toJson()));

            return HttpClient.newHttpClient().send(builder.build(), BodyHandlers.ofString());
        });
    }

    @Override
    public void describeRepo() {
        throw new IllegalStateException("not implemented.");
    }

    @Override
    public Response<RepoGetRecordResponse> getRecord(
            RepoGetRecordRequest request
    ) {
        return proceed(RepoGetRecordResponse.class, () -> {

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(xrpc(this.uri).resolve(ATProtocolTypes.RepoGetRecord));

            request.toMap().forEach((key, value) -> builder.uri(builder.uri().resolve("?").resolve(key).resolve("=").resolve(value)));

            return HttpClient.newHttpClient().send(builder.build(), BodyHandlers.ofString());
        });
    }

    @Override
    public Response<RepoListRecordsResponse> listRecords(
            RepoListRecordsRequest request
    ) {
        return proceed(RepoListRecordsResponse.class, () -> {

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(xrpc(this.uri).resolve(ATProtocolTypes.RepoListRecords));

            request.toMap().forEach((key, value) -> builder.uri(builder.uri().resolve("?").resolve(key).resolve("=").resolve(value)));

            return HttpClient.newHttpClient().send(builder.build(), BodyHandlers.ofString());
        });
    }

    @Override
    public void putRecord() {
        throw new IllegalStateException("not implemented.");
    }

    @Override
    public Response<RepoUploadBlobResponse> uploadBlob(
            RepoUploadBlobRequest request
    ) {
        return proceed(RepoUploadBlobResponse.class, () -> {

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(xrpc(this.uri).resolve(ATProtocolTypes.RepoUploadBlob));

            // From file
            if (request instanceof RepoUploadBlobByFileRequest) {
                RepoUploadBlobByFileRequest f = (RepoUploadBlobByFileRequest) request;
                builder.header("Authorization", f.getBearerToken());
                builder.POST(BodyPublishers.ofFile(new File(f.getFile()).toPath()));
            }

            // From InputStream
            if (request instanceof RepoUploadBlobByStreamRequest) {
                RepoUploadBlobByStreamRequest s = (RepoUploadBlobByStreamRequest) request;
                builder.header("Authorization", s.getBearerToken());
                builder.POST(BodyPublishers.ofInputStream(() -> s.getStream()));
            }

            return HttpClient.newHttpClient().send(builder.build(), BodyHandlers.ofString());
        });
    }
}
