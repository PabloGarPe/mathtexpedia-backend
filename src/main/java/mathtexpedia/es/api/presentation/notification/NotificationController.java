package mathtexpedia.es.api.presentation.notification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.notification.CreateAnnouncementDto;
import mathtexpedia.es.api.domain.model.notification.MarkAllReadResultDto;
import mathtexpedia.es.api.domain.model.notification.NotificationDto;
import mathtexpedia.es.api.domain.model.notification.NotificationPreferenceDto;
import mathtexpedia.es.api.domain.model.notification.UnreadCountDto;
import mathtexpedia.es.api.domain.model.notification.UpdateNotificationPreferencesDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.notification.NotificationPreferenceService;
import mathtexpedia.es.api.service.notification.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notificaciones", description = "Notificaciones in-app del usuario autenticado (la campanita) y sus preferencias. "
        + "Requiere JWT; enviar avisos requiere rol ADMIN")
@RestController
@RequestMapping("/notifications")
public class NotificationController extends GenericController {

    private final NotificationService notificationService;
    private final NotificationPreferenceService preferenceService;

    public NotificationController(NotificationService notificationService, NotificationPreferenceService preferenceService) {
        this.notificationService = notificationService;
        this.preferenceService = preferenceService;
    }

    @Operation(summary = "Mis notificaciones",
            description = "Bandeja del usuario autenticado, de la más reciente a la más antigua. No modifica la cuenta del usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de notificaciones"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping
    public Page<NotificationDto> getMyNotifications(
            @Parameter(description = "Si es true, solo las no leídas")
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @Parameter(description = "Número de página, empezando en 0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto 20, máximo 50)")
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserProfile user) {
        logger.debug("Called getMyNotifications for user {}", user.getId());

        return notificationService.getMyNotifications(user, unreadOnly, page, size);
    }

    @Operation(summary = "Número de notificaciones sin leer",
            description = "Pensado para el polling de la campanita (recomendado cada 60 s con la pestaña visible). Es una consulta muy ligera y no modifica la cuenta del usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Número de notificaciones sin leer"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/unread-count")
    public UnreadCountDto getUnreadCount(@AuthenticationPrincipal UserProfile user) {
        logger.trace("Called getUnreadCount for user {}", user.getId());

        return notificationService.getUnreadCount(user);
    }

    @Operation(summary = "Marca una notificación como leída",
            description = "Idempotente: si ya estaba leída no es un error")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Marcada como leída"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "No existe o no es del usuario")
    })
    @PatchMapping("/{id:\\d+}/read")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "Id de la notificación", required = true) @PathVariable long id,
            @AuthenticationPrincipal UserProfile user) throws MathtexpediaNotFoundException {
        logger.debug("Called markAsRead {} for user {}", id, user.getId());

        notificationService.markAsRead(user, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Marca una notificación como no leída")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Marcada como no leída"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "No existe o no es del usuario")
    })
    @PatchMapping("/{id:\\d+}/unread")
    public ResponseEntity<Void> markAsUnread(
            @Parameter(description = "Id de la notificación", required = true) @PathVariable long id,
            @AuthenticationPrincipal UserProfile user) throws MathtexpediaNotFoundException {
        logger.debug("Called markAsUnread {} for user {}", id, user.getId());

        notificationService.markAsUnread(user, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Marca todas las notificaciones como leídas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Número de notificaciones marcadas"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PatchMapping("/read-all")
    public MarkAllReadResultDto markAllAsRead(@AuthenticationPrincipal UserProfile user) {
        logger.debug("Called markAllAsRead for user {}", user.getId());

        return notificationService.markAllAsRead(user);
    }

    @Operation(summary = "Borra una notificación", description = "Solo la quita de la bandeja del usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Borrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "No existe o no es del usuario")
    })
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id de la notificación", required = true) @PathVariable long id,
            @AuthenticationPrincipal UserProfile user) throws MathtexpediaNotFoundException {
        logger.debug("Called delete notification {} for user {}", id, user.getId());

        notificationService.delete(user, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Mis preferencias de notificación",
            description = "Matriz completa de tipos configurables por canal, con los valores por defecto donde el usuario no ha elegido. "
                    + "Los tipos no configurables (bienvenida, avisos) no aparecen porque se entregan siempre")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferencias del usuario"),
            @ApiResponse(responseCode = "401", description = "No autenticado o cuenta inactiva")
    })
    @GetMapping("/preferences")
    public List<NotificationPreferenceDto> getMyPreferences(@AuthenticationPrincipal UserProfile user)
            throws MathtexpediaUnauthorizedException {
        logger.debug("Called getMyPreferences for user {}", user.getId());

        return preferenceService.getMyPreferences(user);
    }

    @Operation(summary = "Actualiza mis preferencias de notificación",
            description = "Cambio parcial: solo se modifican las preferencias indicadas. Devuelve la matriz completa actualizada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matriz completa de preferencias"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, tipo no configurable o alcance no admitido por el tipo"),
            @ApiResponse(responseCode = "401", description = "No autenticado o cuenta inactiva")
    })
    @PutMapping("/preferences")
    public List<NotificationPreferenceDto> updateMyPreferences(
            @RequestBody @Valid UpdateNotificationPreferencesDto dto,
            @AuthenticationPrincipal UserProfile user) throws MathtexpediaUnauthorizedException, MathtexpediaInvalidException {
        logger.debug("Called updateMyPreferences for user {}", user.getId());

        return preferenceService.updateMyPreferences(user, dto);
    }

    @Operation(summary = "Envía un aviso a los usuarios",
            description = "A todos los usuarios activos o solo a los indicados por email. El reparto es asíncrono. "
                    + "El enlace opcional debe ser una ruta interna que empiece por '/'. Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Aviso aceptado; se reparte en segundo plano"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o enlace no interno"),
            @ApiResponse(responseCode = "401", description = "El usuario autenticado no tiene rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Algún email no corresponde a ningún usuario")
    })
    @PostMapping("/announcements")
    public ResponseEntity<Void> createAnnouncement(
            @RequestBody @Valid CreateAnnouncementDto dto,
            @AuthenticationPrincipal UserProfile user)
            throws MathtexpediaUnauthorizedException, MathtexpediaInvalidException, MathtexpediaNotFoundException {
        logger.debug("Called createAnnouncement by user {}", user.getId());

        checkIfAdmin(user);

        notificationService.createAnnouncement(user, dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
