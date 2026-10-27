package mathtexpedia.es.api.domain.model.pdfComment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record ModerationStatusDto(
        @Schema(description = "Si la moderación automática (IA) está respondiendo. Si no, los comentarios nuevos esperan en PENDING")
        boolean aiAvailable,
        @Schema(description = "Desde cuándo está en este estado (disponible o caída) en esta instancia del servidor")
        Instant since,
        @Schema(description = "Cuándo se volverá a comprobar. Nulo si está disponible", nullable = true)
        Instant nextCheckAt,
        @Schema(description = "Comentarios esperando a la IA")
        long pendingCount,
        @Schema(description = "Comentarios dudosos esperando a un administrador")
        long reviewCount) {
}
