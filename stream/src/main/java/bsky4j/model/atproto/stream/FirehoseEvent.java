package bsky4j.model.atproto.stream;

public class FirehoseEvent {
    private String type;
    private String data;

    public FirehoseEvent(String type, String data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public static FirehoseEvent fromJson(String json) {
        // TODO: Implement JSON parsing
        return null;
    }
}
