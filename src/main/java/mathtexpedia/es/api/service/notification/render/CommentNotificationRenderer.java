package mathtexpedia.es.api.service.notification.render;

import mathtexpedia.es.api.domain.model.notification.NotificationType;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationCategory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class CommentNotificationRenderer implements NotificationRenderer {

    @Override
    public Set<NotificationType> supportedTypes() {
        return Set.of(NotificationType.COMMENT_REPLY, NotificationType.COMMENT_ON_PDF,
                NotificationType.COMMENT_REJECTED, NotificationType.COMMENT_REVIEW_PENDING);
    }

    @Override
    public RenderedNotification render(NotificationType type, Map<String, Object> payload) {
        String pdf = text(payload, "pdfName");
        String username = text(payload, "username");

        return switch (type) {
            case COMMENT_REPLY -> RenderedNotification.of("Nueva respuesta a tu comentario",
                    username + " ha respondido a tu comentario en «" + pdf + "».");
            case COMMENT_ON_PDF -> RenderedNotification.of("Nuevo comentario en tu apunte",
                    username + " ha comentado en «" + pdf + "».");
            case COMMENT_REJECTED -> RenderedNotification.of("Comentario no publicado",
                    "Tu comentario en «" + pdf + "» no se ha publicado porque " + reason(text(payload, "category")) + ".");
            case COMMENT_REVIEW_PENDING -> RenderedNotification.of("Comentario pendiente de revisión",
                    "Hay un comentario en «" + pdf + "» que espera la decisión de un administrador.");
            default -> throw new IllegalArgumentException("Unsupported type " + type);
        };
    }

    /** Sin {@code default}: si se añade una categoría nueva, esto deja de compilar hasta que tenga su texto. */
    private String reason(String categoryName) {
        ModerationCategory category;
        try {
            category = ModerationCategory.valueOf(categoryName);
        } catch (IllegalArgumentException e) {
            category = ModerationCategory.OTHER;
        }

        return switch (category) {
            case HATE_SPEECH -> "contiene lenguaje de odio o discriminatorio";
            case HARASSMENT -> "contiene insultos o acoso a otras personas";
            case SEXUAL_CONTENT -> "contiene contenido sexual";
            case VIOLENCE_OR_THREATS -> "contiene amenazas o incita a la violencia";
            case ILLEGAL_ACTIVITY -> "hace referencia a actividades ilegales";
            case SPAM -> "parece spam o publicidad";
            case PERSONAL_DATA -> "contiene datos personales de terceros";
            case OTHER -> "no cumple las normas de la comunidad";
        };
    }
}
