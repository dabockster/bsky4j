package bsky4j.api.lexicon;

import bsky4j.model.atproto.lexicon.LexiconParseRequest;
import bsky4j.model.atproto.lexicon.LexiconParseResponse;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class LexiconParser {
    private final XRPCClient xrpcClient;

    public LexiconParser(URI baseUri, String authorization) {
        this.xrpcClient = new XRPCClient(baseUri, authorization);
    }

    public CompletableFuture<LexiconParseResponse> parseLexicon(LexiconParseRequest request) {
        return xrpcClient.call("lexicon.parse", request.toMap(), LexiconParseResponse.class);
    }
}
