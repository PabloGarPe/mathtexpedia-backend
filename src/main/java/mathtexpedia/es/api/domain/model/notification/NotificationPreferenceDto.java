package mathtexpedia.es.api.domain.model.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class NotificationPreferenceDto {

    @Schema(description = "Tipo de notificación", example = "PDF_CREATED")
    private NotificationType type;

    @Schema(description = "Categoría, para agrupar en la pantalla de preferencias", example = "CONTENT")
    private NotificationCategory category;

    @Schema(description = "Nombre corto para mostrar", example = "Nuevos apuntes")
    private String label;

    @Schema(description = "Descripción para mostrar", example = "Cuando se sube un apunte nuevo")
    private String description;

    @Schema(description = "Canal de entrega", example = "IN_APP")
    private ChannelType channel;

    @Schema(description = "Si el usuario recibe este tipo por este canal")
    private boolean enabled;

    @Schema(description = "Valor por defecto, para poder mostrar 'restablecer'")
    private boolean defaultEnabled;

    @Schema(description = "Alcance elegido. Nulo si el tipo no admite alcances", nullable = true, example = "ALL")
    private NotificationScope scope;

    @Schema(description = "Alcances que admite el tipo. Si solo hay uno no hace falta mostrar selector")
    private Set<NotificationScope> availableScopes;
}
