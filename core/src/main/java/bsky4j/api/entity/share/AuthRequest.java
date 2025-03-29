package bsky4j.api.entity.share;

import bsky4j.internal.share._InternalUtility;
import com.google.gson.reflect.TypeToken;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optimized authentication request with improved performance and security.
 */
public class AuthRequest {
    private static final Logger LOGGER = Logger.getLogger(AuthRequest.class.getName());
    
    private final String accessJwt;
    private final AtomicReference<String> cachedDid = new AtomicReference<>();
    private final AtomicReference<Long> cacheTimestamp = new AtomicReference<>(0L);
    private static final long CACHE_DURATION_MS = 1000 * 60 * 5; // 5 minutes
    
    protected AuthRequest(String accessJwt) {
        this.accessJwt = accessJwt;
        // Pre-cache DID on creation
        getDid();
    }
    
    public String getAccessJwt() {
        return accessJwt;
    }
    
    public String getBearerToken() {
        return "Bearer " + getAccessJwt();
    }
    
    public String getDid() {
        // Check cache
        String cached = cachedDid.get();
        long timestamp = cacheTimestamp.get();
        
        // Use cached value if valid
        if (cached != null && System.currentTimeMillis() - timestamp < CACHE_DURATION_MS) {
            return cached;
        }
        
        // Decode and cache new value
        try {
            String encodedJson = getAccessJwt().split("\\.")[1];
            String decodedJson = new String(Base64.getDecoder().decode(encodedJson));
            Map<String, String> jsonMap = _InternalUtility.gson.fromJson(decodedJson,
                    new TypeToken<Map<String, String>>() {}.getType());
            
            String did = jsonMap.get("sub");
            if (did != null) {
                cachedDid.set(did);
                cacheTimestamp.set(System.currentTimeMillis());
                return did;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to decode JWT", e);
        }
        
        return null;
    }
    
    /**
     * Clears the cached DID value.
     */
    public void clearCache() {
        cachedDid.set(null);
        cacheTimestamp.set(0L);
    }
    
    /**
     * Checks if the JWT token is expired.
     * @return true if the token is expired
     */
    public boolean isExpired() {
        try {
            String encodedJson = getAccessJwt().split("\\.")[1];
            String decodedJson = new String(Base64.getDecoder().decode(encodedJson));
            Map<String, Object> jsonMap = _InternalUtility.gson.fromJson(decodedJson,
                    new TypeToken<Map<String, Object>>() {}.getType());
            
            Long exp = (Long) jsonMap.get("exp");
            if (exp != null) {
                return System.currentTimeMillis() / 1000 > exp;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to check token expiration", e);
        }
        return false;
    }
}
