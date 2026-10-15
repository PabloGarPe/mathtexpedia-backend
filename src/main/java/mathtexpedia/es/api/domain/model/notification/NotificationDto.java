package mathtexpedia.es.api.domain.model.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class NotificationDto {

    @Schema(description = "Identificador de la notificación del usuario (es el que se usa para marcarla como leída o borrarla)",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Tipo de notificación", example = "PDF_CREATED")
    private NotificationType type;

    @Schema(description = "Categoría de la notificación, útil para elegir el icono", example = "CONTENT")
    private NotificationCategory category;

    @Schema(description = "Título corto, ya redactado", example = "Nuevo apunte en Álgebra")
    private String title;

    @Schema(description = "Mensaje corto, ya redactado", example = "Se ha añadido «Matrices» al tema Matrices y determinantes.")
    private String message;

    @Schema(description = "Ruta interna del front a la que llevar al usuario. Solo la traen los avisos manuales; el resto se navega con resourceType y resourceId",
            nullable = true, example = "/dashboard")
    private String link;

    @Schema(description = "Tipo del recurso al que apunta (NONE si no apunta a ninguno)", example = "PDF")
    private ResourceType resourceType;

    @Schema(description = "Id del recurso al que apunta", nullable = true, example = "12")
    private Long resourceId;

    @Schema(description = "Id de la asignatura relacionada", nullable = true, example = "3")
    private Long subjectId;

    @Schema(description = "Si el usuario ya la ha leído")
    private boolean read;

    @Schema(description = "Cuándo la leyó", nullable = true)
    private Instant readAt;

    @Schema(description = "Cuándo se generó")
    private Instant createdAt;
}
