package mathtexpedia.es.api.domain.port.chatbot;

import mathtexpedia.es.api.domain.exception.PortActionNotPerformedException;
import mathtexpedia.es.api.domain.model.chatbot.GenerationResult;

public interface GenerativeAiPort {
    GenerationResult generate(String prompt) throws PortActionNotPerformedException;
}
