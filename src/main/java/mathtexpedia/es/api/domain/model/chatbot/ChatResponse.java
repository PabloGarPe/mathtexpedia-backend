package mathtexpedia.es.api.domain.model.chatbot;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class ChatResponse {

    public enum ChatResponseStatus {
        SUCCESS,
        ERROR
    }

    private String response;
    private List<ChatResource> relevantResources;
    private ChatResponseStatus status;

    public static ChatResponse success(String response, List<ChatResource> relevantResources) {
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setResponse(response);
        chatResponse.setRelevantResources(relevantResources);
        chatResponse.setStatus(ChatResponseStatus.SUCCESS);
        return chatResponse;
    }

    public static ChatResponse error(String response) {
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setResponse(response);
        chatResponse.setRelevantResources(Collections.emptyList());
        chatResponse.setStatus(ChatResponseStatus.ERROR);
        return chatResponse;
    }
}
