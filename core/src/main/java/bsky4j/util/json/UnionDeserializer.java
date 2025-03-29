package bsky4j.util.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for union type deserializers with optimized type handling.
 * Provides thread-safe type mapping and efficient deserialization.
 */
public abstract class UnionDeserializer<T> implements JsonDeserializer<T> {
    private static final Logger LOGGER = Logger.getLogger(UnionDeserializer.class.getName());
    
    // Thread-safe type map
    private final Map<String, TypeToken<? extends T>> typeMap = new ConcurrentHashMap<>();
    
    /**
     * Initializes the type map with the supported types.
     * Subclasses should call this method from their static initializer.
     */
    protected final void initTypeMap(Map<String, TypeToken<? extends T>> types) {
        typeMap.putAll(types);
    }
    
    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        if (json == null || !json.isJsonObject()) {
            return null;
        }
        
        JsonObject obj = json.getAsJsonObject();
        JsonElement typeElement = obj.get("$type");
        
        if (typeElement == null) {
            LOGGER.log(Level.FINE, "No $type found in JSON object");
            return null;
        }
        
        String typeName = typeElement.getAsString();
        TypeToken<? extends T> typeToken = typeMap.get(typeName);
        
        if (typeToken == null) {
            LOGGER.log(Level.WARNING, "Unknown type: " + typeName);
            return null;
        }
        
        try {
            return context.deserialize(obj, typeToken.getType());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to deserialize type " + typeName, e);
            return null;
        }
    }
}
