package mathtexpedia.es.api.service.notification;

import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.notification.NotificationPreferenceDto;
import mathtexpedia.es.api.domain.model.notification.UpdateNotificationPreferencesDto;
import mathtexpedia.es.api.domain.security.UserProfile;

import java.util.List;

public interface NotificationPreferenceService {

    /** La matriz completa tipo × canal de los tipos configurables, con los valores por defecto donde el usuario no ha elegido. */
    List<NotificationPreferenceDto> getMyPreferences(UserProfile user) throws MathtexpediaUnauthorizedException;

    /** Cambio parcial. Devuelve la matriz completa ya actualizada. */
    List<NotificationPreferenceDto> updateMyPreferences(UserProfile user, UpdateNotificationPreferencesDto dto)
            throws MathtexpediaUnauthorizedException, MathtexpediaInvalidException;
}
