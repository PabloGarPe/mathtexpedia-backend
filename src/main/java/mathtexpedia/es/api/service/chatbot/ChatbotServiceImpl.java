package mathtexpedia.es.api.service.chatbot;

import mathtexpedia.es.api.domain.model.chatbot.*;
import mathtexpedia.es.api.domain.model.pdf.PDFDto;
import mathtexpedia.es.api.domain.model.pdf.PDFNoLinkDto;
import mathtexpedia.es.api.domain.model.pdf.PDFSummary;
import mathtexpedia.es.api.domain.port.chatbot.GenerativeAiPort;
import mathtexpedia.es.api.domain.port.chatbot.SitemapPort;
import mathtexpedia.es.api.persistence.chatbot.ChatUsage;
import mathtexpedia.es.api.persistence.chatbot.ChatUsageDataService;
import mathtexpedia.es.api.service.pdf.PDFService;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    private final static Logger logger = LoggerFactory.getLogger(ChatbotServiceImpl.class);

    private final static String SYSTEM_CONTEXT = """
            Eres el asistente virtual de MathTexPedia, un sitio educativo de matemáticas en español.
            
            Tu función principal es ayudar a los usuarios a:
            - Encontrar PDFs específicos por asignatura o tema
            - Navegar por las diferentes secciones del sitio
            - Encontrar artículos específicos del blog
            - Responder preguntas sobre cómo usar el sitio
            
            Directrices importantes:
            - Siempre responde en español
            - Sé conciso, claro y amigable (máximo 3-4 líneas)
            - Proporciona enlaces específicos cuando encuentres recursos
            - Si encuentras múltiples resultados, menciona los 3-4 más relevantes
            - NUNCA intentes resolver ejercicios matemáticos, solo indica que visitar para resolver dudas
            - Para navegación, da instrucciones claras y enlaces directos
            """;

    private static final Set<String> NAV_KEYWORDS = Set.of(
            "como", "donde", "pagina", "seccion", "navegar", "ir a",
            "acceder", "contacto", "contactar", "politica", "privacidad",
            "terminos", "cookies", "faq", "preguntas frecuentes", "sobre nosotros",
            "acerca de", "inicio", "home", "registrar", "login", "perfil"
    );

    private static final Set<String> STOP_WORDS = Set.of(
            "el", "la", "de", "que", "y", "a", "en", "un", "ser", "se", "no", "haber",
            "por", "con", "su", "para", "como", "estar", "tener", "le", "lo", "todo",
            "pero", "mas", "hacer", "o", "poder", "decir", "este", "ir", "otro", "ese",
            "si", "me", "ya", "ver", "porque", "dar", "cuando", "muy", "sin",
            "vez", "mucho", "saber", "sobre", "mi", "alguno", "mismo", "yo",
            "tambien", "hasta", "ano", "dos", "querer", "entre", "asi", "primero",
            "desde", "grande", "eso", "ni", "nos", "llegar", "pasar", "tiempo", "ella",
            "una", "pueden", "tengo", "tienen", "donde", "hay", "encuentro",
            "necesito", "busco", "quiero", "gustaria", "pdfs", "pdf", "ejercicios",
            "recursos", "materiales", "contenido", "algo", "articulo", "articulos",
            "blog", "post", "posts", "leer", "explicacion", "teoria"
    );

    private final PDFService pdfService;
    private final SitemapPort sitemapPort;
    private final GenerativeAiPort generativeAiPort;
    private final ChatUsageDataService chatUsageDataService;

    @Value("${chatbot.anonymous-daily-request-limit:1}")
    private int anonymousDailyRequestLimit;

    @Value("${chatbot.daily-token-limit:3000}")
    private int dailyTokenLimit;

    public ChatbotServiceImpl(PDFService pdfService, SitemapPort sitemapPort, GenerativeAiPort generativeAiPort, ChatUsageDataService chatUsageDataService) {
        this.pdfService = pdfService;
        this.sitemapPort = sitemapPort;
        this.generativeAiPort = generativeAiPort;
        this.chatUsageDataService = chatUsageDataService;
    }

    @Override
    public ChatResponse chat(ChatRequest request, boolean isAuthenticated, String userIdentifier) {
        try {
            logger.info("Processing chat request: {}", request.getMessage());

            LocalDate today = LocalDate.now();
            Optional<ChatResponse> limitResponse = checkUsageLimit(isAuthenticated, userIdentifier, today);
            if (limitResponse.isPresent()) {
                logger.warn("Usage limit reached for user: {} on date: {}", userIdentifier, today);
                return limitResponse.get();
            }

            boolean isNavigation = isNavigationQuery(request.getMessage());

            List<PDFSummary> pdfs = List.of();
            List<SitemapEntry> blogPosts = List.of();
            List<SitemapEntry> navigationPages = List.of();

            if (isNavigation) {
                navigationPages = sitemapPort.searchUrls(request.getMessage(), 5);
            } else {
                pdfs = searchRelevantPdfs(request.getMessage(), 6, isAuthenticated);
                blogPosts = searchRelevantBlogPosts(request.getMessage(), 6);
            }

            String educationalContext = formatEducationalResourcesForPrompt(pdfs, blogPosts, isAuthenticated);
            String navigationContext = formatNavigationForPrompt(navigationPages);

            String conversationContext = "";
            if (request.getConversationHistory() != null && !request.getConversationHistory().isEmpty()) {
                conversationContext = request.getConversationHistory().stream()
                        .map(msg -> ("user".equals(msg.getRole()) ? "Usuario" : "Asistente") + ": " + msg.getContent())
                        .collect(Collectors.joining("\n"));
            }

            String contextToUse = isNavigation ? navigationContext : educationalContext;
            String instructions = getInstructions(isAuthenticated, pdfs, isNavigation);

            String prompt = buildPrompt(contextToUse, conversationContext, request.getMessage(), instructions);

            GenerationResult result = generativeAiPort.generate(prompt);
            chatUsageDataService.incrementUsage(userIdentifier, today, result.totalTokens());
            return ChatResponse.success(result.text(), buildResources(pdfs, blogPosts, navigationPages));

        } catch (Exception e) {
            logger.error("Error processing chat request", e);
            return ChatResponse.error("Lo siento, estoy teniendo problemas técnicos. Por favor, intenta de nuevo en unos momentos.");
        }
    }

    private Optional<ChatResponse> checkUsageLimit(boolean isAuthenticated, String userIdentifier, LocalDate today) {
        Optional<ChatUsage> usage = chatUsageDataService.get(userIdentifier, today);

        if (!isAuthenticated) {
            int requests = usage.map(ChatUsage::getRequestCount).orElse(0);
            if (requests >= anonymousDailyRequestLimit) {
                return Optional.of(ChatResponse.error("Has usado tu mensaje gratuito hoy. Regístrate para poder seguir chateando con el asistente y acceder a todos los recursos."));
            }
        } else {
            int tokens = usage.map(ChatUsage::getTokensUsed).orElse(0);
            if (tokens >= dailyTokenLimit) {
                return Optional.of(ChatResponse.error("Has alcanzado tu límite diario de uso del chat. Vuelve mañana."));
            }
        }

        return Optional.empty();
    }

    private String buildPrompt(String contextToUse, String conversationContext, String message, String instructions) {
        return """
                %s
                
                %s
                
                %s
                
                CONSULTA DEL USUARIO: %s
                
                INSTRUCCIONES:
                %s
                """.formatted(
                SYSTEM_CONTEXT,
                contextToUse,
                conversationContext.isEmpty() ? "" : "HISTORIAL DE CONVERSACIÓN:\n" + conversationContext,
                message,
                instructions
        );
    }

    private static @NonNull String getInstructions(boolean isAuthenticated, List<PDFSummary> pdfs, boolean isNavigation) {
        String authInstructions = (!isAuthenticated && !pdfs.isEmpty())
                ? "\n- IMPORTANTE: Si mencionas PDFs, recuerda al usuario que debe REGISTRARSE para acceder a los enlaces de descarga"
                : "";

        return isNavigation
                ? "- Da el enlace directo a la página solicitada\n- Sé breve y directo"
                : ("- Menciona los 3-4 recursos más relevantes\n"
                   + (isAuthenticated
                      ? "- Proporciona los enlaces de visualización directos para PDFs\n"
                      : "- Para PDFs, menciona que están disponibles pero requieren registro para acceder\n")
                   + "- Incluye los enlaces completos disponibles\n"
                   + "- Máximo 8-10 líneas" + authInstructions);
    }

    private boolean isNavigationQuery(String query) {
        String normalized = normalize(query);
        return NAV_KEYWORDS.stream().anyMatch(normalized::contains);
    }

    private String normalize(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    @SuppressWarnings("SameParameterValue")
    private List<PDFSummary> searchRelevantPdfs(String query, int limit, boolean includeLinks) {
        try {
            List<String> keywords = extractKeywords(query);
            List<PDFSummary> allPdfs = includeLinks
                    ? pdfService.getPDFs().stream().map(this::toSummary).toList()
                    : pdfService.getPDFsWithoutLink().stream().map(this::toSummary).toList();

            return allPdfs.stream()
                    .map(pdf -> Map.entry(pdf, score(pdf, keywords)))
                    .filter(entry -> entry.getValue() > 0)
                    .sorted((a, b) -> b.getValue() - a.getValue())
                    .limit(limit)
                    .map(Map.Entry::getKey)
                    .toList();
        } catch (Exception e) {
            logger.error("Error searching PDFs", e);
            return List.of();
        }
    }

    private List<String> extractKeywords(String query) {
        String normalized = normalize(query).replaceAll("[^a-z0-9\\s]", " ");
        return Arrays.stream(normalized.split("\\s+"))
                .filter(word -> word.length() > 2 && !STOP_WORDS.contains(word))
                .toList();
    }

    private int score(PDFSummary pdf, List<String> keywords) {
        String name = normalize(pdf.getName());
        String tag = normalize(pdf.getTag());
        int score = 0;
        for (String keyword : keywords) {
            if (name.contains(keyword)) score += 3;
            if (tag.contains(keyword)) score += 1;
        }
        return score;
    }

    @SuppressWarnings("SameParameterValue")
    private List<SitemapEntry> searchRelevantBlogPosts(String query, int limit) {
        try {
            return sitemapPort.searchUrls(query, limit * 2).stream()
                    .filter(entry -> "blog".equals(entry.getCategory()))
                    .limit(limit)
                    .toList();
        } catch (Exception e) {
            logger.error("Error searching blog posts", e);
            return List.of();
        }
    }

    private String formatEducationalResourcesForPrompt(List<PDFSummary> pdfs, List<SitemapEntry> blogPosts, boolean isAuthenticated) {
        if (pdfs.isEmpty() && blogPosts.isEmpty()) {
            return "No se encontraron recursos educativos para esta consulta.";
        }

        StringBuilder formatted = new StringBuilder("RECURSOS EDUCATIVOS DISPONIBLES:\n\n");

        if (!pdfs.isEmpty()) {
            formatted.append(isAuthenticated
                    ? "**PDFs para visualizar (usuario autenticado):**\n"
                    : "**PDFs disponibles (requieren registro):**\n");

            Map<String, List<PDFSummary>> grouped = pdfs.stream()
                    .collect(Collectors.groupingBy(
                            pdf -> pdf.getTag() == null || pdf.getTag().isBlank() ? "General" : pdf.getTag(),
                            LinkedHashMap::new,
                            Collectors.toList()));

            grouped.forEach((subject, subjectPdfs) -> {
                formatted.append("\n").append(subject).append(":\n");
                subjectPdfs.forEach(pdf -> {
                    if (isAuthenticated && pdf.getLink() != null) {
                        formatted.append("- ").append(pdf.getName()).append(" → ").append(pdf.getLink()).append("\n");
                    } else {
                        formatted.append("- ").append(pdf.getName()).append(" (enlace disponible tras registro)\n");
                    }
                });
            });
        }

        if (!blogPosts.isEmpty()) {
            formatted.append("\n**Artículos del blog (para leer online):**\n");
            blogPosts.forEach(post -> formatted.append("- ").append(post.getTitle()).append(" → ").append(post.getLoc()).append("\n"));
        }

        return formatted.toString();
    }

    private String formatNavigationForPrompt(List<SitemapEntry> pages) {
        if (pages.isEmpty()) {
            return "No se encontraron páginas específicas.";
        }

        StringBuilder formatted = new StringBuilder("PÁGINAS DEL SITIO:\n\n");
        pages.forEach(page -> formatted.append("- ").append(page.getTitle()).append(" → ").append(page.getLoc()).append("\n"));
        return formatted.toString();
    }

    private List<ChatResource> buildResources(List<PDFSummary> pdfs, List<SitemapEntry> blogPosts, List<SitemapEntry> navigationPages) {
        List<ChatResource> resources = new ArrayList<>();
        pdfs.forEach(pdf -> resources.add(new ChatResource(ChatResource.ChatContentType.PDF, pdf.getName(), pdf.getLink())));
        blogPosts.forEach(post -> resources.add(new ChatResource(ChatResource.ChatContentType.BLOG_POST, post.getTitle(), post.getLoc())));
        navigationPages.forEach(page -> resources.add(new ChatResource(ChatResource.ChatContentType.PAGE, page.getTitle(), page.getLoc())));
        return resources;
    }

    private PDFSummary toSummary(PDFDto dto) {
        return new PDFSummary(
                dto.getName(),
                dto.getSubjectUnit() != null ? dto.getSubjectUnit().getName() : null,
                dto.getLink()
        );
    }

    private PDFSummary toSummary(PDFNoLinkDto dto) {
        return new PDFSummary(
                dto.getName(),
                dto.getSubjectUnit() != null ? dto.getSubjectUnit().getName() : null,
                null
        );
    }
}
