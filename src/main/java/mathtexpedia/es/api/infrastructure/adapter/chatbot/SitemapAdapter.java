package mathtexpedia.es.api.infrastructure.adapter.chatbot;

import mathtexpedia.es.api.domain.model.chatbot.SitemapEntry;
import mathtexpedia.es.api.domain.port.chatbot.SitemapPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Component
public class SitemapAdapter implements SitemapPort {

    private static final Logger logger = LoggerFactory.getLogger(SitemapAdapter.class);
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final RestClient restClient = RestClient.create();

    @Value("${mathtexpedia.sitemap.url}")
    private String sitemapUrl;

    private volatile List<SitemapEntry> cachedEntries = List.of();
    private volatile Instant cachedAt = Instant.EPOCH;

    @Override
    public List<SitemapEntry> searchUrls(String query, int limit) {
        String normalizedQuery = normalize(query);

        return getEntries().stream()
                .filter(entry -> matches(entry, normalizedQuery))
                .limit(limit)
                .toList();
    }

    private String normalize(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    private synchronized List<SitemapEntry> getEntries() {
        boolean isFresh = Duration.between(cachedAt, Instant.now()).compareTo(CACHE_TTL) < 0 && !cachedEntries.isEmpty();
        if (isFresh) return cachedEntries;

        try {
            cachedEntries = fetchAndParse();
            cachedAt = Instant.now();
            logger.info("Sitemap cache refrescada con {} entradas", cachedEntries.size());
        } catch (Exception e) {
            logger.error("No se pudo refrescar el sitemap, se usará la caché anterior", e);
        }

        return cachedEntries;
    }

    private List<SitemapEntry> fetchAndParse() throws Exception {
        String xml = restClient.get().uri(sitemapUrl).retrieve().body(String.class);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        NodeList urlNodes = document.getElementsByTagName("url");
        List<SitemapEntry> entries = new ArrayList<>();

        for (int i = 0; i < urlNodes.getLength(); i++) {
            Element urlElement = (Element) urlNodes.item(i);
            String loc = textOf(urlElement, "loc");
            if(loc == null || loc.isBlank()) continue;

            entries.add(new SitemapEntry(titleFromLoc(loc), loc, categoryFromLoc(loc)));
        }

        return entries;
    }

    private String textOf(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent() : null;
    }

    private String categoryFromLoc(String loc) {
        return loc.contains("/blog/") ? "blog" : "page";
    }

    private String titleFromLoc(String loc) {
        String path = loc.replaceAll("/+$", "");
        String slug = loc.substring(path.lastIndexOf('/') + 1);
        String humanReadable = slug.replace("-", " ");
        if (humanReadable.isEmpty()) return "Inicio";
        return humanReadable.substring(0, 1).toUpperCase(Locale.ROOT) + humanReadable.substring(1);
    }

    private boolean matches(SitemapEntry entry, String normalizedQuery) {
        String haystack = normalize(entry.getTitle()) + " " + normalize(entry.getLoc());
        if(haystack.contains(normalizedQuery)) return true;

        return Stream.of(normalizedQuery.split("\\s+"))
                .filter(word -> word.length() > 2)
                .anyMatch(haystack::contains);
    }
}
