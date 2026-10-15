package mathtexpedia.es.api.domain.model.notification;

/** Estado de una entrega. IN_APP nace DELIVERED; el resto lo usará el canal de correo (outbox: PENDING → SENT / FAILED). */
public enum DeliveryStatus {
    DELIVERED, PENDING, SENT, FAILED
}
