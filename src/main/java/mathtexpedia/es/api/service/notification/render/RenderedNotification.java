package mathtexpedia.es.api.service.notification.render;

/** Texto listo para mostrar. {@code link} solo se rellena en los avisos manuales (ruta interna del front). */
public record RenderedNotification(String title, String message, String link) {

    public static RenderedNotification of(String title, String message) {
        return new RenderedNotification(title, message, null);
    }
}
