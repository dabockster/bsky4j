package bsky4j.internal.share;

import static java.util.TimeZone.getTimeZone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import net.socialhub.http.HttpException;
import net.socialhub.http.HttpResponse;
import net.socialhub.http.HttpResponseCode;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import bsky4j.ATProtocolException;
import bsky4j.api.entity.share.Response;
import bsky4j.model.bsky.actor.ActorDefsPreferencesUnion;
import bsky4j.model.bsky.embed.EmbedRecordViewUnion;
import bsky4j.model.bsky.embed.EmbedUnion;
import bsky4j.model.bsky.embed.EmbedViewUnion;
import bsky4j.model.bsky.feed.FeedDefsThreadUnion;
import bsky4j.model.bsky.richtext.RichtextFacetFeatureUnion;
import bsky4j.model.share.RecordUnion;
import bsky4j.util.json.ActorDefsPreferencesDeserializer;
import bsky4j.util.json.EmbedDeserializer;
import bsky4j.util.json.EmbedRecordViewDeserializer;
import bsky4j.util.json.EmbedSerializer;
import bsky4j.util.json.EmbedViewDeserializer;
import bsky4j.util.json.FeedDefsThreadDeserializer;
import bsky4j.util.json.RecordDeserializer;
import bsky4j.util.json.RichtextFacetFeatureDeserializer;
import bsky4j.util.json.RichtextFacetFeatureSerializer;

/**
 * Optimized internal utility class with improved error handling and performance.
 */
public class _InternalUtility {
    private static final Logger LOGGER = Logger.getLogger(_InternalUtility.class.getName());
    
    // Use lazy initialization with thread-safety for Gson
    private static final AtomicReference<Gson> gsonRef = new AtomicReference<>();
    private static final AtomicBoolean isInitialized = new AtomicBoolean(false);
    
    // Thread-safe date format with cache
    private static final ThreadLocal<SimpleDateFormat> dateFormatCache = ThreadLocal.withInitial(() -> {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(getTimeZone("UTC"));
        return df;
    });

    public static Gson getGson() {
        if (!isInitialized.getAndSet(true)) {
            synchronized (_InternalUtility.class) {
                if (!isInitialized.get()) {
                    createGson();
                }
            }
        }
        return gsonRef.get();
    }

    private static synchronized void createGson() {
        if (gsonRef.get() == null) {
            GsonBuilder builder = new GsonBuilder()
                    .registerTypeAdapter(EmbedUnion.class, new EmbedDeserializer())
                    .registerTypeAdapter(EmbedUnion.class, new EmbedSerializer())
                    .registerTypeAdapter(EmbedViewUnion.class, new EmbedViewDeserializer())
                    .registerTypeAdapter(RecordUnion.class, new RecordDeserializer())
                    .registerTypeAdapter(FeedDefsThreadUnion.class, new FeedDefsThreadDeserializer())
                    .registerTypeAdapter(RichtextFacetFeatureUnion.class, new RichtextFacetFeatureDeserializer())
                    .registerTypeAdapter(RichtextFacetFeatureUnion.class, new RichtextFacetFeatureSerializer())
                    .registerTypeAdapter(EmbedRecordViewUnion.class, new EmbedRecordViewDeserializer())
                    .registerTypeAdapter(ActorDefsPreferencesUnion.class, new ActorDefsPreferencesDeserializer());
            
            gsonRef.set(builder.create());
        }
    }

    public static SimpleDateFormat getDateFormat() {
        return dateFormatCache.get();
    }

    private _InternalUtility() {
    }

    public static Response<Void> proceed(RequestInterface function) {
        return proceed(null, function);
    }

    public static <T> Response<T> proceed(Class<T> clazz, RequestInterface function) {
        return proceed(clazz, function, null);
    }

    public static <T> Response<T> proceed(TypeToken<T> clazz, RequestInterface function) {
        return proceed(null, function, clazz);
    }

    private static <T> Response<T> proceed(Class<T> clazz, RequestInterface function, TypeToken<T> typeToken) {
        try {
            HttpResponse response = function.proceed();
            if (response.getStatusCode() == HttpResponseCode.OK) {
                Response<T> result = new Response<>();
                String json = response.asString();
                result.setJson(json);
                
                if (clazz != null) {
                    result.set(parseJson(json, clazz));
                } else if (typeToken != null) {
                    result.set(parseJson(json, typeToken.getType()));
                }
                
                return result;
            }
            throw new ATProtocolException("HTTP error: " + response.getStatusCode());
        } catch (HttpException e) {
            throw handleError(e);
        }
    }

    private static <T> T parseJson(String json, Type type) {
        try (JsonReader reader = new JsonReader(new StringReader(json))) {
            reader.setLenient(true);
            return getGson().fromJson(reader, type);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to parse JSON", e);
            throw new ATProtocolException("Failed to parse JSON response", e);
        }
    }

    public interface RequestInterface {
        HttpResponse proceed() throws HttpException;
    }

    public static String xrpc(String uri) {
        if (uri == null || uri.isEmpty()) {
            throw new IllegalArgumentException("URI cannot be null or empty");
        }
        
        StringBuilder builder = new StringBuilder(uri);
        if (!uri.endsWith("/")) {
            builder.append("/");
        }
        builder.append("xrpc/");
        return builder.toString();
    }

    static RuntimeException handleError(HttpException e) {
        try {
            String message = e.getResponse().asString();
            Map<String, Object> error = getGson().fromJson(message, new TypeToken<Map<String, Object>>() {}.getType());
            
            ATProtocolException exception = new ATProtocolException(e);
            exception.setErrorMessage(error.get("message") != null ? error.get("message").toString() : "Unknown error");
            exception.setError(error.get("error") != null ? error.get("error").toString() : "Unknown error");
            
            // Log detailed error information
            LOGGER.log(Level.SEVERE, "HTTP error: " + e.getStatusCode() + " - " + exception.getErrorMessage(), e);
            return exception;
        } catch (Exception t) {
            LOGGER.log(Level.SEVERE, "Failed to parse error response: " + e.getMessage(), t);
            return new ATProtocolException("Failed to parse error response: " + e.getMessage(), e);
        }
    }
}
