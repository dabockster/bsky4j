package bsky4j.util.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optimized base class for union type deserializers with improved performance and error handling.
 * Provides thread-safe type mapping, efficient deserialization, and detailed metrics.
 */
public abstract class UnionDeserializer<T> implements JsonDeserializer<T> {
    private static final Logger LOGGER = Logger.getLogger(UnionDeserializer.class.getName());
    
    // Thread-safe type map with atomic statistics
    private final Map<String, TypeToken<? extends T>> typeMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> deserializationStats = new ConcurrentHashMap<>();
    
    // Cache for frequently used type tokens
    private final Map<String, TypeToken<? extends T>> typeTokenCache = new ConcurrentHashMap<>();
    
    /**
     * Initializes the type map with the supported types.
     * Subclasses should call this method from their static initializer.
     */
    protected final void initTypeMap(Map<String, TypeToken<? extends T>> types) {
        types.forEach((key, value) -> {
            typeMap.put(key, value);
            deserializationStats.put(key, new AtomicInteger(0));
            typeTokenCache.put(key, value);
        });
    }
    
    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        if (json == null || !json.isJsonObject()) {
            recordDeserializationFailure("null or not JsonObject");
            return null;
        }
        
        JsonObject obj = json.getAsJsonObject();
        JsonElement typeElement = obj.get("$type");
        
        if (typeElement == null) {
            recordDeserializationFailure("missing $type");
            return null;
        }
        
        String typeName = typeElement.getAsString();
        TypeToken<? extends T> typeToken = typeTokenCache.get(typeName);
        
        if (typeToken == null) {
            recordDeserializationFailure("unknown type: " + typeName);
            return null;
        }
        
        try {
            T result = context.deserialize(obj, typeToken.getType());
            recordDeserializationSuccess(typeName);
            return result;
        } catch (Exception e) {
            String errorMessage = "Failed to deserialize type " + typeName;
            LOGGER.log(Level.SEVERE, errorMessage, e);
            recordDeserializationFailure(errorMessage);
            return null;
        }
    }
    
    private void recordDeserializationSuccess(String typeName) {
        deserializationStats.computeIfPresent(typeName, (key, value) -> value.incrementAndGet());
    }
    
    private void recordDeserializationFailure(String reason) {
        deserializationStats.computeIfPresent("failures", (key, value) -> value.incrementAndGet());
        LOGGER.log(Level.FINE, "Deserialization failure: " + reason);
    }
    
    /**
     * Gets statistics about deserialization operations.
     * @return A map containing deserialization counts per type
     */
    public Map<String, Integer> getDeserializationStats() {
        return deserializationStats.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().get()));
    }
    
    /**
     * Clears the deserialization statistics.
     */
    public void clearStats() {
        deserializationStats.forEach((key, value) -> value.set(0));
    }
}
