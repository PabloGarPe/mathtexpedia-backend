package mathtexpedia.es.api.domain.port.chatbot;

import mathtexpedia.es.api.domain.model.chatbot.SitemapEntry;

import java.util.List;

public interface SitemapPort {
    List<SitemapEntry> searchUrls(String query, int limit);
}
