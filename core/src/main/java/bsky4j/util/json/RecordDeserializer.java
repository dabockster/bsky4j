package bsky4j.util.json;

import bsky4j.model.bsky.actor.ActorProfile;
import bsky4j.model.bsky.feed.FeedLike;
import bsky4j.model.bsky.feed.FeedPost;
import bsky4j.model.bsky.feed.FeedRepost;
import bsky4j.model.bsky.graph.GraphBlock;
import bsky4j.model.bsky.graph.GraphFollow;
import bsky4j.model.share.RecordUnion;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optimized deserializer for RecordUnion with improved performance and error handling.
 */
public class RecordDeserializer extends UnionDeserializer<RecordUnion> {
    private static final Logger LOGGER = Logger.getLogger(RecordDeserializer.class.getName());
    
    private static final Map<String, TypeToken<? extends RecordUnion>> TYPES = new ConcurrentHashMap<>();
    private static final String[] SUPPORTED_TYPES = {
        ActorProfile.TYPE,
        FeedPost.TYPE,
        FeedLike.TYPE,
        FeedRepost.TYPE,
        GraphFollow.TYPE,
        GraphBlock.TYPE
    };
    
    private static final Map<String, AtomicInteger> deserializationStats = new ConcurrentHashMap<>();
    private static final Map<String, AtomicLong> deserializationLatencies = new ConcurrentHashMap<>();
    private static final AtomicInteger totalDeserializationCount = new AtomicInteger(0);
    private static final AtomicInteger errorCount = new AtomicInteger(0);
    
    static {
        TYPES.put(ActorProfile.TYPE, new TypeToken<ActorProfile>() {});
        TYPES.put(FeedPost.TYPE, new TypeToken<FeedPost>() {});
        TYPES.put(FeedLike.TYPE, new TypeToken<FeedLike>() {});
        TYPES.put(FeedRepost.TYPE, new TypeToken<FeedRepost>() {});
        TYPES.put(GraphFollow.TYPE, new TypeToken<GraphFollow>() {});
        TYPES.put(GraphBlock.TYPE, new TypeToken<GraphBlock>() {});
    }
    
    /**
     * Initializes the type map with supported types.
     */
    public RecordDeserializer() {
        initTypeMap(TYPES);
    }
    
    /**
     * Gets the list of supported record types.
     * @return Array of supported type names
     */
    public static String[] getSupportedTypes() {
        return SUPPORTED_TYPES;
    }
    
    /**
     * Checks if a record type is supported.
     * @param typeName The type name to check
     * @return true if the type is supported
     */
    public static boolean isSupportedType(String typeName) {
        return TYPES.containsKey(typeName);
    }
    
    /**
     * Gets deserialization statistics.
     * @return Map containing deserialization statistics
     */
    public static Map<String, Object> getStatistics() {
        return Map.of(
            "totalDeserializations", totalDeserializationCount.get(),
            "totalErrors", errorCount.get(),
            "typeStats", deserializationStats.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> Map.of(
                        "deserializations", entry.getValue().get(),
                        "errors", getErrorCount(entry.getKey()),
                        "averageLatencyNs", getAverageLatency(entry.getKey())
                    )
                )),
            "overallAverageLatencyNs", getOverallAverageLatency()
        );
    }
    
    /**
     * Gets the most frequently deserialized record type.
     * @return The most frequently deserialized type name
     */
    public static String getMostFrequentType() {
        return deserializationStats.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    /**
     * Gets the type with the highest error rate.
     * @return The type with the highest error rate
     */
    public static String getHighestErrorRateType() {
        return deserializationStats.entrySet().stream()
            .map(entry -> {
                AtomicInteger deserializations = deserializationStats.get(entry.getKey());
                if (deserializations == null || deserializations.get() == 0) {
                    return Map.entry(entry.getKey(), 0.0);
                }
                return Map.entry(entry.getKey(), (double) getErrorCount(entry.getKey()) / deserializations.get());
            })
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    /**
     * Records a deserialization success.
     * @param typeName The type name
     * @param startTime The start time in nanoseconds
     */
    private void recordDeserializationSuccess(String typeName, long startTime) {
        deserializationStats.computeIfAbsent(typeName, k -> new AtomicInteger(0))
                          .incrementAndGet();
        
        deserializationLatencies.computeIfAbsent(typeName, k -> new AtomicLong(0))
                              .addAndGet(System.nanoTime() - startTime);
        
        totalDeserializationCount.incrementAndGet();
    }
    
    /**
     * Records a deserialization failure.
     * @param typeName The type name
     */
    private void recordDeserializationFailure(String typeName) {
        deserializationStats.computeIfAbsent(typeName, k -> new AtomicInteger(0))
                          .incrementAndGet();
        
        errorCount.incrementAndGet();
    }
    
    /**
     * Gets the error count for a specific type.
     * @param typeName The type name
     * @return The error count
     */
    private static int getErrorCount(String typeName) {
        AtomicInteger count = deserializationStats.get(typeName);
        return count != null ? count.get() : 0;
    }
    
    /**
     * Gets the average latency for a specific type.
     * @param typeName The type name
     * @return The average latency in nanoseconds
     */
    private static long getAverageLatency(String typeName) {
        AtomicInteger count = deserializationStats.get(typeName);
        AtomicLong latency = deserializationLatencies.get(typeName);
        
        if (count != null && latency != null && count.get() > 0) {
            return latency.get() / count.get();
        }
        return 0;
    }
    
    /**
     * Gets the overall average latency across all types.
     * @return The overall average latency in nanoseconds
     */
    private static long getOverallAverageLatency() {
        long totalLatency = deserializationLatencies.values().stream()
            .mapToLong(AtomicLong::get)
            .sum();
            
        int totalCount = totalDeserializationCount.get();
        if (totalCount > 0) {
            return totalLatency / totalCount;
        }
        return 0;
    }
    
    @Override
    protected RecordUnion deserialize(String json, String type) {
        long startTime = System.nanoTime();
        try {
            RecordUnion record = super.deserialize(json, type);
            recordDeserializationSuccess(type, startTime);
            return record;
        } catch (Exception e) {
            recordDeserializationFailure(type);
            LOGGER.log(Level.WARNING, "Failed to deserialize record of type " + type, e);
            throw e;
        }
    }
    
    /**
     * Clears the deserialization statistics.
     */
    public static void clearStats() {
        deserializationStats.values().forEach(AtomicInteger::set);
        deserializationLatencies.values().forEach(AtomicLong::set);
        totalDeserializationCount.set(0);
        errorCount.set(0);
    }
}
