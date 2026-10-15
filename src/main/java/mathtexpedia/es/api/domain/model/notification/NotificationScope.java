package mathtexpedia.es.api.domain.model.notification;

/** Alcance elegido por el usuario para un tipo de notificación. */
public enum NotificationScope {
    /** Cualquier recurso nuevo, sea de la asignatura que sea. */
    ALL,
    /** Solo recursos de asignaturas a las que el usuario está suscrito. */
    SUBSCRIBED
}
