package bsky4j.api.entity.share;

import com.google.gson.annotations.SerializedName;

/**
 * @author uakihir0
 */
public class Response<T> {
    @SerializedName("json")
    private String json;
    
    @SerializedName("data")
    private T data;

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> Response<T> of(T data) {
        Response<T> response = new Response<>();
        response.setData(data);
        return response;
    }
}
