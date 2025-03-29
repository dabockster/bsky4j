package bsky4j.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced HTTP client configuration for bsky4j with optimized connection handling.
 */
public class Bsky4JClientConfiguration {
    private static final Logger LOGGER = Logger.getLogger(Bsky4JClientConfiguration.class.getName());
    
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 15000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 30000;
    private static final int DEFAULT_MAX_CONNECTIONS = 100;
    private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 50;
    
    private final int connectTimeoutMs;
    private final int readTimeoutMs;
    private final int maxConnections;
    private final int maxConnectionsPerRoute;
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    
    private Bsky4JClientConfiguration(Builder builder) {
        this.connectTimeoutMs = builder.connectTimeoutMs;
        this.readTimeoutMs = builder.readTimeoutMs;
        this.maxConnections = builder.maxConnections;
        this.maxConnectionsPerRoute = builder.maxConnectionsPerRoute;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }
    
    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }
    
    public int getMaxConnections() {
        return maxConnections;
    }
    
    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }
    
    /**
     * Marks the configuration as initialized.
     */
    public void initialize() {
        if (isInitialized.compareAndSet(false, true)) {
            LOGGER.log(Level.FINE, "Configuration initialized: " + this);
        }
    }
    
    /**
     * Increments the active connection count.
     */
    public void incrementActiveConnections() {
        activeConnections.incrementAndGet();
    }
    
    /**
     * Decrements the active connection count.
     */
    public void decrementActiveConnections() {
        activeConnections.decrementAndGet();
    }
    
    /**
     * Gets the current active connection count.
     */
    public int getActiveConnections() {
        return activeConnections.get();
    }
    
    /**
     * Checks if the configuration is initialized.
     */
    public boolean isInitialized() {
        return isInitialized.get();
    }
    
    @Override
    public String toString() {
        return "Bsky4JClientConfiguration{" +
                "connectTimeoutMs=" + connectTimeoutMs +
                ", readTimeoutMs=" + readTimeoutMs +
                ", maxConnections=" + maxConnections +
                ", maxConnectionsPerRoute=" + maxConnectionsPerRoute +
                ", activeConnections=" + activeConnections.get() +
                ", initialized=" + isInitialized.get() +
                '}';
    }
    
    /**
     * Builder for Bsky4JClientConfiguration.
     */
    public static class Builder {
        private int connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT_MS;
        private int readTimeoutMs = DEFAULT_READ_TIMEOUT_MS;
        private int maxConnections = DEFAULT_MAX_CONNECTIONS;
        private int maxConnectionsPerRoute = DEFAULT_MAX_CONNECTIONS_PER_ROUTE;
        
        public Builder connectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
            return this;
        }
        
        public Builder readTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
            return this;
        }
        
        public Builder maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }
        
        public Builder maxConnectionsPerRoute(int maxConnectionsPerRoute) {
            this.maxConnectionsPerRoute = maxConnectionsPerRoute;
            return this;
        }
        
        public Bsky4JClientConfiguration build() {
            return new Bsky4JClientConfiguration(this);
        }
    }
}
