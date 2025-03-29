package bsky4j;

import bsky4j.api.atproto.IdentityResource;
import bsky4j.api.atproto.RepoResource;
import bsky4j.api.atproto.ServerResource;
import bsky4j.api.atproto.XRPCResource;
import bsky4j.api.atproto.LexiconResource;
import bsky4j.api.atproto.ATURIResolver;
import bsky4j.api.atproto.ATProtocolException;

public interface ATProtocol {

    IdentityResource identity();

    ServerResource server();

    RepoResource repo();

    XRPCResource xrpc();

    LexiconResource lexicon();

    ATURIResolver uriResolver();

    ATProtocolException getLastError();

    void clearLastError();
}
