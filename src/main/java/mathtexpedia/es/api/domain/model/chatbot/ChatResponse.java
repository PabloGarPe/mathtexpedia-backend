package mathtexpedia.es.api.domain.model.chatbot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class ChatResponse {

    public enum ChatResponseStatus {
        SUCCESS,
        ERROR
    }

    @Schema(description = "Respuesta generada por el chatbot")
    private String response;

    @Schema(description = "Recursos de Mathtexpedia relevantes para la respuesta")
    private List<ChatResource> relevantResources;

    @Schema(description = "Resultado de la operación")
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
