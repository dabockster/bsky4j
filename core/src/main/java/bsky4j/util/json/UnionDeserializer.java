package bsky4j.util.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.Collections;

/**
 * Optimized base deserializer for union types with improved performance and error handling.
 * Implements ATProtocol's Lexicon specification for union types.
 */
public abstract class UnionDeserializer<T> implements JsonDeserializer<T> {
    private static final Logger LOGGER = Logger.getLogger(UnionDeserializer.class.getName());
    private static final String TYPE_FIELD = "$type";
    
    protected final ConcurrentHashMap<String, TypeToken<? extends T>> typeMap = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, AtomicInteger> deserializationStats = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, AtomicLong> deserializationLatencies = new ConcurrentHashMap<>();
    protected final AtomicLong totalDeserializationTime = new AtomicLong(0);
    protected final AtomicInteger totalDeserializationCount = new AtomicInteger(0);
    protected final AtomicInteger errorCount = new AtomicInteger(0);
    
    /**
     * Initializes the type map with the supported types.
     * Subclasses should call this method from their static initializer.
     * 
     * @param types Map of type names to their corresponding TypeToken
     */
    protected void initTypeMap(Map<String, TypeToken<? extends T>> types) {
        types.forEach((type, token) -> {
            typeMap.putIfAbsent(type, token);
            deserializationStats.putIfAbsent(type, new AtomicInteger(0));
            deserializationLatencies.putIfAbsent(type, new AtomicLong(0));
        });
    }
    
    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        if (json == null || !json.isJsonObject()) {
            errorCount.incrementAndGet();
            throw new JsonParseException("Invalid JSON input for union type");
        }
        
        long startTime = System.nanoTime();
        JsonObject obj = json.getAsJsonObject();
        JsonElement typeElement = obj.get(TYPE_FIELD);
        
        if (typeElement == null || !typeElement.isJsonPrimitive()) {
            errorCount.incrementAndGet();
            throw new JsonParseException("Missing or invalid $type field in JSON");
        }
        
        String type = typeElement.getAsString();
        TypeToken<? extends T> typeToken = typeMap.get(type);
        
        if (typeToken == null) {
            errorCount.incrementAndGet();
            throw new JsonParseException("Unsupported type: " + type);
        }
        
        try {
            T result = context.deserialize(obj, typeToken.getType());
            
            // Update statistics
            deserializationStats.get(type).incrementAndGet();
            long latency = System.nanoTime() - startTime;
            deserializationLatencies.get(type).addAndGet(latency);
            totalDeserializationTime.addAndGet(latency);
            totalDeserializationCount.incrementAndGet();
            
            return result;
        } catch (Exception e) {
            errorCount.incrementAndGet();
            LOGGER.log(Level.WARNING, "Failed to deserialize type " + type + ": " + e.getMessage(), e);
            throw new JsonParseException("Failed to deserialize type " + type, e);
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
                totalDeserializationTime.get() / totalDeserializationCount.get() : 0,
            "typeStats", typeMap.keySet().stream().collect(Collectors.toMap(
                type -> type,
                type -> Map.of(
                    "count", deserializationStats.get(type).get(),
                    "averageLatencyNs", deserializationStats.get(type).get() > 0 ? 
                        deserializationLatencies.get(type).get() / deserializationStats.get(type).get() : 0
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
        totalDeserializationTime.set(0);
        
        typeMap.keySet().forEach(type -> {
            deserializationStats.get(type).set(0);
            deserializationLatencies.get(type).set(0);
        });
    }
    
    /**
     * Gets the supported types for this union.
     * 
     * @return Set of supported type names
     */
    public Set<String> getSupportedTypes() {
        return Collections.unmodifiableSet(typeMap.keySet());
    }
    
    /**
     * Checks if a type is supported by this union.
     * 
     * @param type The type name to check
     * @return true if the type is supported
     */
    public boolean isTypeSupported(String type) {
        return typeMap.containsKey(type);
    }
}
