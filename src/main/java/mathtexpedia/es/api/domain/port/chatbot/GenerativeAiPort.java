package mathtexpedia.es.api.domain.port.chatbot;

import mathtexpedia.es.api.domain.exception.PortActionNotPerformedException;

public interface GenerativeAiPort {
    String generate(String prompt) throws PortActionNotPerformedException;
}
