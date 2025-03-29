package bsky4j;

import bsky4j.internal._ATProtocol;
import bsky4j.util.Bsky4JClientConfiguration;
import bsky4j.util.HttpClientManager;

/**
 * Factory for creating ATProtocol instances with optimized connection handling.
 */
public class ATProtocolFactory {

    /**
     * Creates a new ATProtocol instance with the default configuration.
     *
     * @param uri The base URI for the ATProtocol service
     * @return A new ATProtocol instance
     */
    public static ATProtocol getInstance(String uri) {
        return new _ATProtocol(uri, HttpClientManager.getInstance().getClient());
    }
    
    /**
     * Creates a new ATProtocol instance with a custom configuration.
     *
     * @param uri The base URI for the ATProtocol service
     * @param config The client configuration to use
     * @return A new ATProtocol instance
     */
    public static ATProtocol getInstance(String uri, Bsky4JClientConfiguration config) {
        return new _ATProtocol(uri, HttpClientManager.getInstance().getClient(config));
    }
    
    /**
     * Shuts down all HTTP clients managed by this factory.
     * This should be called when the application is shutting down.
     */
    public static void shutdown() {
        HttpClientManager.getInstance().shutdown();
    }
}
