package bsky4j.internal;

import bsky4j.PLCDirectory;
import bsky4j.api.entity.share.Response;
import bsky4j.model.plc.DIDDetails;
import bsky4j.model.plc.DIDLog;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static bsky4j.internal.share._InternalUtility.proceed;

public class _PLCDirectory implements PLCDirectory {

    private final String uri;

    public _PLCDirectory(String uri) {
        this.uri = uri;
    }

    @Override
    public Response<DIDDetails> getDIDDetails(String did) {
        return proceed(DIDDetails.class, () -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(this.uri + "/" + did))
                    .GET()
                    .build();

            try {
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                return response.body();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Response<List<DIDLog>> getDIDLogs(String did) {
        return proceed(new TypeToken<List<DIDLog>>() {
        }, () -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(this.uri + "/" + did + "/log"))
                    .GET()
                    .build();

            try {
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                return response.body();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
