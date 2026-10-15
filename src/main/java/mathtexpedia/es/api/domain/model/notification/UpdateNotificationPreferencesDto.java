package mathtexpedia.es.api.domain.model.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateNotificationPreferencesDto {

    @Schema(description = "Preferencias a cambiar. Es parcial: solo se tocan las indicadas")
    @NotEmpty
    @Valid
    private List<Item> preferences;

    @Data
    public static class Item {

        @Schema(description = "Tipo de notificación (debe ser configurable)", example = "PDF_CREATED")
        @NotNull
        private NotificationType type;

        @Schema(description = "Canal", example = "IN_APP")
        @NotNull
        private ChannelType channel;

        @Schema(description = "Si se quiere recibir")
        @NotNull
        private Boolean enabled;

        @Schema(description = "Alcance (opcional; debe estar entre los admitidos por el tipo). Si no se indica se mantiene el actual",
                nullable = true, example = "ALL")
        private NotificationScope scope;
    }
}
