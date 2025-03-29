package bsky4j.util.json;

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
 * Optimized base deserializer for union types with improved performance and error handling.
 */
public abstract class UnionDeserializer<T> implements JsonDeserializer<T> {
    private static final Logger LOGGER = Logger.getLogger(UnionDeserializer.class.getName());
    
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
        long startTime = System.nanoTime();
        
        try {
            JsonObject obj = json.getAsJsonObject();
            JsonElement typeElement = obj.get("$type");
            
            if (typeElement == null) {
                throw new JsonParseException("Missing $type field");
            }
            
            String typeName = typeElement.getAsString();
            TypeToken<? extends T> typeToken = typeMap.get(typeName);
            
            if (typeToken == null) {
                throw new JsonParseException("Unknown type: " + typeName);
            }
            
            T result = context.deserialize(obj, typeToken.getType());
            recordDeserializationSuccess(typeName, startTime);
            return result;
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
            LOGGER.log(Level.WARNING, "Deserialization error", e);
            throw new JsonParseException("Failed to deserialize", e);
        }
    }
    
    /**
     * Records a successful deserialization.
     * 
     * @param typeName The type name
     * @param startTime The start time in nanoseconds
     */
    protected void recordDeserializationSuccess(String typeName, long startTime) {
        deserializationStats.computeIfAbsent(typeName, k -> new AtomicInteger(0))
                          .incrementAndGet();
        
        deserializationLatencies.computeIfAbsent(typeName, k -> new AtomicLong(0))
                              .addAndGet(System.nanoTime() - startTime);
        
        totalDeserializationTime.addAndGet(System.nanoTime() - startTime);
        totalDeserializationCount.incrementAndGet();
    }
    
    /**
     * Gets statistics about deserialization operations.
     * 
     * @return A map containing deserialization counts per type
     */
    public Map<String, Object> getStatistics() {
        return Map.of(
            "totalDeserializations", totalDeserializationCount.get(),
            "errors", errorCount.get(),
            "averageTimeNs", totalDeserializationCount.get() > 0 ? 
                totalDeserializationTime.get() / totalDeserializationCount.get() : 0,
            "perTypeStats", typeMap.keySet().stream().collect(Collectors.toMap(
                type -> type,
                type -> Map.of(
                    "count", deserializationStats.getOrDefault(type, new AtomicInteger(0)).get(),
                    "averageTimeNs", deserializationStats.getOrDefault(type, new AtomicInteger(0)).get() > 0 ?
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
        deserializationStats.replaceAll((k, v) -> new AtomicInteger(0));
        deserializationLatencies.replaceAll((k, v) -> new AtomicLong(0));
        totalDeserializationTime.set(0);
        totalDeserializationCount.set(0);
        errorCount.set(0);
    }
}
