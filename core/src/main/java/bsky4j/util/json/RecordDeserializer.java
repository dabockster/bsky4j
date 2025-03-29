package bsky4j.util.json;

import bsky4j.api.entity.record.ActorProfile;
import bsky4j.api.entity.record.FeedLike;
import bsky4j.api.entity.record.FeedPost;
import bsky4j.api.entity.record.FeedRepost;
import bsky4j.api.entity.record.GraphBlock;
import bsky4j.api.entity.record.GraphFollow;
import bsky4j.api.entity.record.RecordUnion;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optimized deserializer for RecordUnion with improved performance and error handling.
 * Implements ATProtocol's Lexicon specification for record types.
 */
public class RecordDeserializer implements JsonDeserializer<RecordUnion> {
    private static final Logger LOGGER = Logger.getLogger(RecordDeserializer.class.getName());
    private static final Gson GSON = new Gson();
    private static final String TYPE_FIELD = "$type";
    
    private static final Map<String, TypeToken<? extends RecordUnion>> TYPES = new ConcurrentHashMap<>();
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
     * Gets the list of supported record types.
     * 
     * @return Array of supported type names
     */
    public static String[] getSupportedTypes() {
        return TYPES.keySet().toArray(new String[0]);
    }
    
    /**
     * Checks if a record type is supported.
     * 
     * @param type The type name to check
     * @return true if the type is supported
     */
    public static boolean isTypeSupported(String type) {
        return TYPES.containsKey(type);
    }
    
    @Override
    public RecordUnion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        if (json == null || !json.isJsonObject()) {
            errorCount.incrementAndGet();
            throw new JsonParseException("Invalid JSON input for record");
        }
        
        long startTime = System.nanoTime();
        JsonObject obj = json.getAsJsonObject();
        JsonElement typeElement = obj.get(TYPE_FIELD);
        
        if (typeElement == null || !typeElement.isJsonPrimitive()) {
            errorCount.incrementAndGet();
            throw new JsonParseException("Missing or invalid $type field in JSON");
        }
        
        String type = typeElement.getAsString();
        TypeToken<? extends RecordUnion> typeToken = TYPES.get(type);
        
        if (typeToken == null) {
            errorCount.incrementAndGet();
            throw new JsonParseException("Unsupported record type: " + type);
        }
        
        try {
            RecordUnion result = context.deserialize(obj, typeToken.getType());
            
            // Update statistics
            deserializationStats.computeIfAbsent(type, k -> new AtomicInteger(0))
                              .incrementAndGet();
            long latency = System.nanoTime() - startTime;
            deserializationLatencies.computeIfAbsent(type, k -> new AtomicLong(0))
                                  .addAndGet(latency);
            totalDeserializationCount.incrementAndGet();
            
            return result;
        } catch (Exception e) {
            errorCount.incrementAndGet();
            LOGGER.log(Level.WARNING, "Failed to deserialize record type " + type + ": " + e.getMessage(), e);
            throw new JsonParseException("Failed to deserialize record type " + type, e);
        }
    }
    
    /**
     * Gets statistics about deserialization performance.
     * 
     * @return Map containing deserialization statistics
     */
    public Map<String, Object> getStatistics() {
        return Map.of(
            "totalDeserializations", totalDeserializationCount.get(),
            "totalErrors", errorCount.get(),
            "averageLatencyNs", totalDeserializationCount.get() > 0 ? 
                deserializationLatencies.values().stream()
                    .mapToLong(AtomicLong::get)
                    .sum() / totalDeserializationCount.get() : 0,
            "typeStats", TYPES.keySet().stream().collect(Collectors.toMap(
                type -> type,
                type -> Map.of(
                    "count", deserializationStats.getOrDefault(type, new AtomicInteger(0)).get(),
                    "averageLatencyNs", deserializationStats.getOrDefault(type, new AtomicInteger(0)).get() > 0 ?
                        deserializationLatencies.getOrDefault(type, new AtomicLong(0)).get() /
                        deserializationStats.get(type).get() : 0
                )
            ))
        );
    }
    
    /**
     * Clears all deserialization statistics.
     */
    public void clearStatistics() {
        totalDeserializationCount.set(0);
        errorCount.set(0);
        
        TYPES.keySet().forEach(type -> {
            deserializationStats.computeIfAbsent(type, k -> new AtomicInteger(0))
                              .set(0);
            deserializationLatencies.computeIfAbsent(type, k -> new AtomicLong(0))
                                  .set(0);
        });
    }
}
