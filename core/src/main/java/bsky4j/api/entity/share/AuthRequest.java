package bsky4j.api.entity.share;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optimized authentication request with improved performance and security.
 * Implements ATProtocol's OAuth specification for token handling.
 */
public class AuthRequest {
    private static final Logger LOGGER = Logger.getLogger(AuthRequest.class.getName());
    private static final Gson GSON = new Gson();
    
    private final String accessJwt;
    private final String refreshJwt;
    private final AtomicReference<String> cachedDid = new AtomicReference<>();
    private final AtomicReference<Long> cacheTimestamp = new AtomicReference<>(0L);
    private final AtomicBoolean isCacheValid = new AtomicBoolean(false);
    private static final long CACHE_DURATION_MS = 1000 * 60 * 5; // 5 minutes
    
    /**
     * Creates a new AuthRequest with both access and refresh JWTs.
     * 
     * @param accessJwt The access JWT token (type: at+jwt)
     * @param refreshJwt The refresh JWT token (type: refresh+jwt)
     */
    public AuthRequest(String accessJwt, String refreshJwt) {
        this.accessJwt = accessJwt;
        this.refreshJwt = refreshJwt;
        // Pre-cache DID on creation
        getDid();
    }
    
    /**
     * Gets the access JWT token (type: at+jwt).
     * 
     * @return The access JWT token
     */
    public String getAccessJwt() {
        return accessJwt;
    }
    
    /**
     * Gets the refresh JWT token (type: refresh+jwt).
     * 
     * @return The refresh JWT token
     */
    public String getRefreshJwt() {
        return refreshJwt;
    }
    
    /**
     * Gets the bearer token for authentication.
     * 
     * @return The bearer token
     */
    public String getBearerToken() {
        return "Bearer " + getAccessJwt();
    }
    
    /**
     * Gets the DID from the JWT token.
     * 
     * @return The DID, or null if not available
     */
    public String getDid() {
        // Check cache
        String cached = cachedDid.get();
        long timestamp = cacheTimestamp.get();
        boolean isValid = isCacheValid.get();
        
        // Use cached value if valid
        if (isValid && cached != null && System.currentTimeMillis() - timestamp < CACHE_DURATION_MS) {
            return cached;
        }
        
        // Decode and cache new value
        try {
            String encodedJson = getAccessJwt().split("\\.")[1];
            String decodedJson = new String(Base64.getDecoder().decode(encodedJson));
            Map<String, String> jsonMap = GSON.fromJson(decodedJson,
                    new TypeToken<Map<String, String>>() {}.getType());
            
            String did = jsonMap.get("sub");
            if (did != null) {
                cachedDid.set(did);
                cacheTimestamp.set(System.currentTimeMillis());
                isCacheValid.set(true);
                return did;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to decode JWT", e);
            isCacheValid.set(false);
        }
        
        return null;
    }
    
    /**
     * Clears the cached DID value.
     */
    public void clearCache() {
        cachedDid.set(null);
        cacheTimestamp.set(0L);
        isCacheValid.set(false);
    }
    
    /**
     * Checks if the JWT token is expired.
     * 
     * @return true if the token is expired
     */
    public boolean isExpired() {
        try {
            String encodedJson = getAccessJwt().split("\\.")[1];
            String decodedJson = new String(Base64.getDecoder().decode(encodedJson));
            Map<String, Object> jsonMap = GSON.fromJson(decodedJson,
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
    
    /**
     * Gets the remaining time until the token expires.
     * 
     * @return The remaining time in milliseconds, or -1 if not available
     */
    public long getRemainingTime() {
        try {
            String encodedJson = getAccessJwt().split("\\.")[1];
            String decodedJson = new String(Base64.getDecoder().decode(encodedJson));
            Map<String, Object> jsonMap = GSON.fromJson(decodedJson,
                    new TypeToken<Map<String, Object>>() {}.getType());
            
            Long exp = (Long) jsonMap.get("exp");
            if (exp != null) {
                return (exp * 1000) - System.currentTimeMillis();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get remaining time", e);
        }
        return -1;
    }
    
    /**
     * Gets statistics about the token.
     * 
     * @return Map containing token statistics
     */
    public Map<String, Object> getStatistics() {
        return Map.of(
            "isExpired", isExpired(),
            "remainingTimeMs", getRemainingTime(),
            "cacheValid", isCacheValid.get(),
            "cacheAgeMs", System.currentTimeMillis() - cacheTimestamp.get(),
            "tokenType", getTokenType()
        );
    }
    
    /**
     * Gets the token type (at+jwt or refresh+jwt).
     * 
     * @return The token type
     */
    public String getTokenType() {
        try {
            String encodedJson = getAccessJwt().split("\\.")[1];
            String decodedJson = new String(Base64.getDecoder().decode(encodedJson));
            Map<String, String> jsonMap = GSON.fromJson(decodedJson,
                    new TypeToken<Map<String, String>>() {}.getType());
            
            return jsonMap.get("typ");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get token type", e);
            return null;
        }
    }
}
