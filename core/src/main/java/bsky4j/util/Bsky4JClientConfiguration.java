package bsky4j.util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced HTTP client configuration for bsky4j with optimized connection handling.
 * Implements ATProtocol's HTTP specification for connection management.
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
    
    public AtomicInteger getActiveConnections() {
        return activeConnections;
    }
    
    public boolean isInitialized() {
        return isInitialized.get();
    }
    
    public void initialize() {
        isInitialized.set(true);
    }
    
    public void incrementActiveConnections() {
        activeConnections.incrementAndGet();
    }
    
    public void decrementActiveConnections() {
        activeConnections.decrementAndGet();
    }
    
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
            if (maxConnections <= 0) {
                throw new IllegalArgumentException("Max connections must be positive");
            }
            this.maxConnections = maxConnections;
            return this;
        }
        
        public Builder maxConnectionsPerRoute(int maxConnectionsPerRoute) {
            if (maxConnectionsPerRoute <= 0) {
                throw new IllegalArgumentException("Max connections per route must be positive");
            }
            this.maxConnectionsPerRoute = maxConnectionsPerRoute;
            return this;
        }
        
        public Bsky4JClientConfiguration build() {
            if (maxConnectionsPerRoute > maxConnections) {
                LOGGER.log(Level.WARNING, 
                    "maxConnectionsPerRoute ({0}) is greater than maxConnections ({1}) - this may cause connection issues", 
                    new Object[]{maxConnectionsPerRoute, maxConnections});
            }
            return new Bsky4JClientConfiguration(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("Bsky4JClientConfiguration{" +
            "connectTimeoutMs=%d," +
            "readTimeoutMs=%d," +
            "maxConnections=%d," +
            "maxConnectionsPerRoute=%d," +
            "activeConnections=%d" +
            '}',
            connectTimeoutMs, readTimeoutMs, maxConnections, maxConnectionsPerRoute, activeConnections.get());
    }
}
