package bsky4j.internal.share;

import static java.util.TimeZone.getTimeZone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.net.http.HttpResponse;
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

    private static final Gson getGson() {
        if (isInitialized.get()) {
            return gsonRef.get();
        }
        
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(EmbedUnion.class, new EmbedSerializer())
            .registerTypeAdapter(EmbedUnion.class, new EmbedDeserializer())
            .registerTypeAdapter(EmbedRecordViewUnion.class, new EmbedRecordViewDeserializer())
            .registerTypeAdapter(EmbedViewUnion.class, new EmbedViewDeserializer())
            .registerTypeAdapter(RecordUnion.class, new RecordDeserializer())
            .registerTypeAdapter(ActorDefsPreferencesUnion.class, new ActorDefsPreferencesDeserializer())
            .registerTypeAdapter(FeedDefsThreadUnion.class, new FeedDefsThreadDeserializer())
            .registerTypeAdapter(RichtextFacetFeatureUnion.class, new RichtextFacetFeatureSerializer())
            .registerTypeAdapter(RichtextFacetFeatureUnion.class, new RichtextFacetFeatureDeserializer())
            .create();
            
        if (gsonRef.compareAndSet(null, gson)) {
            isInitialized.set(true);
        }
        
        return gsonRef.get();
    }

    public static SimpleDateFormat getDateFormat() {
        return dateFormatCache.get();
    }

    private _InternalUtility() {
    }

    public static <T> Response<T> proceed(Class<T> responseType, RunnableWithResult<HttpResponse> action) {
        try {
            HttpResponse response = action.run();
            
            if (response.statusCode() >= 400) {
                throw new ATProtocolException("HTTP error: " + response.statusCode());
            }
            
            String body = response.body().toString();
            return Response.of(getGson().fromJson(new JsonReader(new StringReader(body)), responseType));
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error during HTTP request", e);
            throw new ATProtocolException("HTTP request failed", e);
        }
    }

    public static Response<Void> proceed(RunnableWithResult<HttpResponse> action) {
        return proceed(null, action);
    }

    public static <T> Response<T> proceed(TypeToken<T> responseType, RunnableWithResult<HttpResponse> action) {
        return proceed(responseType.getType(), action);
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

    public interface RunnableWithResult<T> {
        T run() throws IOException;
    }
}
