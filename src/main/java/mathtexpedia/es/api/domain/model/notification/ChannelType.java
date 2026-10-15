package mathtexpedia.es.api.domain.model.notification;

/**
 * Canal por el que se entrega una notificación. Para añadir el correo: un valor {@code EMAIL} aquí, una implementación
 * de {@code NotificationChannel} y un job que procese las entregas pendientes (el compilador marcará los
 * {@code switch} que haya que completar, p. ej. {@link NotificationType#defaultEnabled}).
 */
public enum ChannelType {
    IN_APP
}
