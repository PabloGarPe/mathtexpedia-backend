package mathtexpedia.es.api.service.notification.render;

import mathtexpedia.es.api.domain.model.notification.NotificationType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class PdfNotificationRenderer implements NotificationRenderer {

    @Override
    public Set<NotificationType> supportedTypes() {
        return Set.of(NotificationType.PDF_CREATED, NotificationType.PDF_FILE_UPDATED, NotificationType.PDF_AUTHOR_ADDED);
    }

    @Override
    public RenderedNotification render(NotificationType type, Map<String, Object> payload) {
        String pdf = text(payload, "pdfName");
        String subject = text(payload, "subjectName");
        String unit = text(payload, "subjectUnitName");

        return switch (type) {
            case PDF_CREATED -> RenderedNotification.of("Nuevo apunte en " + subject,
                    "Se ha añadido «" + pdf + "»" + (unit.isEmpty() ? "" : " al tema " + unit) + ".");
            case PDF_FILE_UPDATED -> RenderedNotification.of("Apunte actualizado",
                    "Se ha actualizado «" + pdf + "» de " + subject + ".");
            case PDF_AUTHOR_ADDED -> RenderedNotification.of("Eres autor de un apunte",
                    "Te han añadido como autor de «" + pdf + "» (" + subject + ").");
            default -> throw new IllegalArgumentException("Unsupported type " + type);
        };
    }
}
