package bsky4j.util;

import net.socialhub.http.HttpClientConfiguration.HttpClientDefaultConfiguration;
import net.socialhub.http.HttpMediaType;

/**
 * Enhanced HTTP client configuration for bsky4j with optimized connection handling.
 */
public class Bsky4JClientConfiguration extends HttpClientDefaultConfiguration {

    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 15000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 30000;
    private static final int DEFAULT_MAX_CONNECTIONS = 50;
    private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 20;
    
    private final int connectTimeoutMs;
    private final int readTimeoutMs;
    private final int maxConnections;
    private final int maxConnectionsPerRoute;
    
    /**
     * Creates a new configuration with default settings.
     */
    public Bsky4JClientConfiguration() {
        this(DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS, 
             DEFAULT_MAX_CONNECTIONS, DEFAULT_MAX_CONNECTIONS_PER_ROUTE);
    }
    
    /**
     * Creates a new configuration with custom settings.
     * 
     * @param connectTimeoutMs Connection timeout in milliseconds
     * @param readTimeoutMs Read timeout in milliseconds
     * @param maxConnections Maximum total connections in pool
     * @param maxConnectionsPerRoute Maximum connections per route
     */
    public Bsky4JClientConfiguration(int connectTimeoutMs, int readTimeoutMs, 
                                    int maxConnections, int maxConnectionsPerRoute) {
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.maxConnections = maxConnections;
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }

    @Override
    public String[] getRawContentTypes() {
        return new String[]{
                HttpMediaType.APPLICATION_JSON,
                HttpMediaType.IMAGE_JPEG,
                HttpMediaType.IMAGE_PNG,
                HttpMediaType.IMAGE_GIF,
        };
    }
    
    @Override
    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }
    
    @Override
    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }
    
    @Override
    public int getMaxConnections() {
        return maxConnections;
    }
    
    @Override
    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }
    
    /**
     * Creates a builder for customizing the client configuration.
     * 
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for creating customized client configurations.
     */
    public static class Builder {
        private int connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT_MS;
        private int readTimeoutMs = DEFAULT_READ_TIMEOUT_MS;
        private int maxConnections = DEFAULT_MAX_CONNECTIONS;
        private int maxConnectionsPerRoute = DEFAULT_MAX_CONNECTIONS_PER_ROUTE;
        
        /**
         * Sets the connection timeout.
         * 
         * @param timeoutMs Timeout in milliseconds
         * @return This builder instance
         */
        public Builder connectTimeout(int timeoutMs) {
            this.connectTimeoutMs = timeoutMs;
            return this;
        }
        
        /**
         * Sets the read timeout.
         * 
         * @param timeoutMs Timeout in milliseconds
         * @return This builder instance
         */
        public Builder readTimeout(int timeoutMs) {
            this.readTimeoutMs = timeoutMs;
            return this;
        }
        
        /**
         * Sets the maximum number of connections in the pool.
         * 
         * @param maxConnections Maximum connections
         * @return This builder instance
         */
        public Builder maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }
        
        /**
         * Sets the maximum number of connections per route.
         * 
         * @param maxConnectionsPerRoute Maximum connections per route
         * @return This builder instance
         */
        public Builder maxConnectionsPerRoute(int maxConnectionsPerRoute) {
            this.maxConnectionsPerRoute = maxConnectionsPerRoute;
            return this;
        }
        
        /**
         * Builds a new configuration with the current settings.
         * 
         * @return A new Bsky4JClientConfiguration instance
         */
        public Bsky4JClientConfiguration build() {
            return new Bsky4JClientConfiguration(
                connectTimeoutMs, readTimeoutMs, maxConnections, maxConnectionsPerRoute);
        }
    }
}
