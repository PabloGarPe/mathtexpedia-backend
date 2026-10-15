package mathtexpedia.es.api.domain.model.notification;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Catálogo de notificaciones. Añadir un tipo nuevo es una línea aquí + un método en un producer + su renderer
 * (la aplicación no arranca si falta el renderer). No requiere migraciones: se guarda como texto sin CHECK.
 * <p>
 * Los tipos con {@code supportedScopes} vacío no admiten alcance (son personales o del sistema).
 */
public enum NotificationType {

    PDF_CREATED(NotificationCategory.CONTENT, ResourceType.PDF, true, true, scopes(NotificationScope.ALL, NotificationScope.SUBSCRIBED),
            "Nuevos apuntes", "Cuando se sube un apunte nuevo"),
    PDF_FILE_UPDATED(NotificationCategory.CONTENT, ResourceType.PDF, true, true, scopes(NotificationScope.ALL, NotificationScope.SUBSCRIBED),
            "Apuntes actualizados", "Cuando se reemplaza el contenido de un apunte"),
    QUIZ_PUBLISHED(NotificationCategory.CONTENT, ResourceType.QUIZ, true, true, scopes(NotificationScope.ALL, NotificationScope.SUBSCRIBED),
            "Nuevos cuestionarios", "Cuando se publica un cuestionario"),
    SUBJECT_CREATED(NotificationCategory.CONTENT, ResourceType.SUBJECT, true, true, scopes(NotificationScope.ALL),
            "Nuevas asignaturas", "Cuando se añade una asignatura"),
    // Solo llega a los suscritos a la asignatura (lo decide la audiencia, no el alcance). Apunta a la asignatura
    SUBJECT_UNIT_CREATED(NotificationCategory.CONTENT, ResourceType.SUBJECT, true, true, Set.of(),
            "Nuevos temas", "Cuando se añade un tema a una asignatura a la que estás suscrito"),
    PDF_AUTHOR_ADDED(NotificationCategory.PERSONAL, ResourceType.PDF, true, true, Set.of(),
            "Autoría de apuntes", "Cuando te añaden como autor de un apunte"),
    WELCOME(NotificationCategory.SYSTEM, ResourceType.NONE, false, true, Set.of(),
            "Bienvenida", "Mensaje de bienvenida"),
    ANNOUNCEMENT(NotificationCategory.SYSTEM, ResourceType.NONE, false, true, Set.of(),
            "Avisos", "Avisos de los administradores");

    private final NotificationCategory category;
    private final ResourceType resourceType;
    private final boolean configurable;
    private final boolean defaultInApp;
    private final Set<NotificationScope> supportedScopes;
    private final String label;
    private final String description;

    NotificationType(NotificationCategory category, ResourceType resourceType, boolean configurable, boolean defaultInApp,
                     Set<NotificationScope> supportedScopes, String label, String description) {
        this.category = category;
        this.resourceType = resourceType;
        this.configurable = configurable;
        this.defaultInApp = defaultInApp;
        this.supportedScopes = supportedScopes;
        this.label = label;
        this.description = description;
    }

    public NotificationCategory category() {
        return category;
    }

    public ResourceType resourceType() {
        return resourceType;
    }

    /** Si el usuario puede activarlo o desactivarlo. Los no configurables se entregan siempre. */
    public boolean configurable() {
        return configurable;
    }

    public Set<NotificationScope> supportedScopes() {
        return supportedScopes;
    }

    /** Nombre corto para la pantalla de preferencias. */
    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    private static Set<NotificationScope> scopes(NotificationScope first, NotificationScope... rest) {
        return Collections.unmodifiableSet(EnumSet.of(first, rest));
    }

    /** Valor por defecto cuando el usuario no ha guardado ninguna preferencia para el canal. */
    public boolean defaultEnabled(ChannelType channel) {
        return switch (channel) {
            case IN_APP -> defaultInApp;
        };
    }

    /** Alcance por defecto: {@code ALL} si el tipo admite alcances, {@code null} si no. */
    public NotificationScope defaultScope() {
        return supportedScopes.isEmpty() ? null : NotificationScope.ALL;
    }
}
