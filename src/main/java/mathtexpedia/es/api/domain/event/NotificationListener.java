package mathtexpedia.es.api.domain.event;

import mathtexpedia.es.api.infrastructure.application.AsyncConfig;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Listener de un evento de dominio que genera notificaciones.
 * <ul>
 *     <li>Si el evento se publica dentro de una transacción, espera al commit: nunca se notifica algo que luego hace rollback.</li>
 *     <li>Si no hay transacción (la mayoría de los servicios, porque el DAO ya confirma al volver), se ejecuta directamente.</li>
 *     <li>Siempre en el executor de notificaciones, para no bloquear la petición.</li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Async(AsyncConfig.NOTIFICATION_EXECUTOR)
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
public @interface NotificationListener {
}
