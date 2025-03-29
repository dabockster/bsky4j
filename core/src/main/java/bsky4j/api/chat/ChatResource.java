package bsky4j.api.chat;

import bsky4j.model.atproto.chat.ChatMessage;
import java.util.List;

public interface ChatResource {
    List<ChatMessage> getMessages(String conversationId);
    void sendMessage(String conversationId, ChatMessage message);
    void deleteMessage(String conversationId, String messageId);
}
