package bsky4j.internal;

import bsky4j.ATProtocol;
import bsky4j.ATProtocolException;
import bsky4j.api.atproto.ATURIResolver;
import bsky4j.api.atproto.IdentityResource;
import bsky4j.api.atproto.LexiconResource;
import bsky4j.api.atproto.RepoResource;
import bsky4j.api.atproto.ServerResource;
import bsky4j.api.atproto.XRPCResource;
import bsky4j.internal.atproto._ATURIResolver;
import bsky4j.internal.atproto._IdentityResource;
import bsky4j.internal.atproto._LexiconResource;
import bsky4j.internal.atproto._RepoResource;
import bsky4j.internal.atproto._ServerResource;
import bsky4j.internal.atproto._XRPCResource;

import java.net.http.HttpClient;

/**
 * Implementation of the ATProtocol interface with optimized HTTP client usage.
 */
public class _ATProtocol implements ATProtocol {

    protected final IdentityResource identity;
    protected final ServerResource server;
    protected final RepoResource repo;
    protected final XRPCResource xrpc;
    protected final LexiconResource lexicon;
    protected final ATURIResolver uriResolver;
    
    protected ATProtocolException lastError;
    protected final HttpClient httpClient;

    /**
     * Creates a new ATProtocol instance with the specified URI and HTTP client.
     *
     * @param uri The base URI for the ATProtocol service
     * @param httpClient The shared HTTP client to use
     */
    public _ATProtocol(String uri, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.identity = new _IdentityResource(uri, httpClient);
        this.server = new _ServerResource(uri, httpClient);
        this.repo = new _RepoResource(uri, httpClient);
        this.xrpc = new _XRPCResource(uri, httpClient);
        this.lexicon = new _LexiconResource(uri, httpClient);
        this.uriResolver = new _ATURIResolver();
    }

    /**
     * Creates a new ATProtocol instance with the specified URI and default HTTP client.
     *
     * @param uri The base URI for the ATProtocol service
     */
    public _ATProtocol(String uri) {
        this(uri, HttpClient.newHttpClient());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IdentityResource identity() {
        return identity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServerResource server() {
        return server;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RepoResource repo() {
        return repo;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public XRPCResource xrpc() {
        return xrpc;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public LexiconResource lexicon() {
        return lexicon;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ATURIResolver uriResolver() {
        return uriResolver;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ATProtocolException getLastError() {
        return lastError;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void clearLastError() {
        this.lastError = null;
    }
    
    /**
     * Gets the shared HTTP client used by this ATProtocol instance.
     *
     * @return The shared HTTP client
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }
}
