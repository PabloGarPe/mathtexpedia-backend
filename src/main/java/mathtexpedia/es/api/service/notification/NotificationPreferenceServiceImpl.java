package mathtexpedia.es.api.service.notification;

import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.notification.ChannelType;
import mathtexpedia.es.api.domain.model.notification.NotificationPreferenceDto;
import mathtexpedia.es.api.domain.model.notification.NotificationScope;
import mathtexpedia.es.api.domain.model.notification.NotificationType;
import mathtexpedia.es.api.domain.model.notification.UpdateNotificationPreferencesDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.persistence.notification.NotificationPreference;
import mathtexpedia.es.api.persistence.notification.NotificationPreferenceDataService;
import mathtexpedia.es.api.persistence.user.UserAccount;
import mathtexpedia.es.api.service.userAccount.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationPreferenceServiceImpl.class);

    private final NotificationPreferenceDataService preferenceDataService;
    private final UserAccountService userAccountService;

    public NotificationPreferenceServiceImpl(NotificationPreferenceDataService preferenceDataService,
                                             UserAccountService userAccountService) {
        this.preferenceDataService = preferenceDataService;
        this.userAccountService = userAccountService;
    }

    @Override
    public List<NotificationPreferenceDto> getMyPreferences(UserProfile user) throws MathtexpediaUnauthorizedException {
        logger.info("Fetching notification preferences for user {}", user.getId());

        // Aquí sí se usa getOrProvision: es una acción puntual del usuario, no el polling de la campanita
        UserAccount account = userAccountService.getOrProvision(user);
        return buildMatrix(stored(account.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<NotificationPreferenceDto> updateMyPreferences(UserProfile user, UpdateNotificationPreferencesDto dto)
            throws MathtexpediaUnauthorizedException, MathtexpediaInvalidException {
        logger.info("Updating notification preferences for user {}", user.getId());

        UserAccount account = userAccountService.getOrProvision(user);
        Map<Key, NotificationPreference> current = stored(account.getId());

        // Primero se valida todo, para que un elemento inválido no deje la actualización a medias
        for (UpdateNotificationPreferencesDto.Item item : dto.getPreferences()) {
            if (!item.getType().configurable())
                throw new MathtexpediaInvalidException("Notification type " + item.getType() + " is not configurable");
            if (item.getScope() != null && !item.getType().supportedScopes().contains(item.getScope()))
                throw new MathtexpediaInvalidException(
                        "Scope " + item.getScope() + " is not supported by notification type " + item.getType());
        }

        for (UpdateNotificationPreferencesDto.Item item : dto.getPreferences()) {
            NotificationPreference existing = current.get(new Key(item.getType(), item.getChannel()));
            NotificationScope scope = item.getScope() != null ? item.getScope()
                    : existing != null ? existing.getScope() : item.getType().defaultScope();

            preferenceDataService.upsert(account.getId(), item.getType(), item.getChannel(), item.getEnabled(), scope);
        }

        return buildMatrix(stored(account.getId()));
    }

    private Map<Key, NotificationPreference> stored(long userAccountId) {
        return preferenceDataService.getByUser(userAccountId).stream()
                .collect(Collectors.toMap(p -> new Key(p.getType(), p.getChannel()), p -> p, (a, b) -> b));
    }

    private List<NotificationPreferenceDto> buildMatrix(Map<Key, NotificationPreference> stored) {
        List<NotificationPreferenceDto> matrix = new ArrayList<>();

        for (NotificationType type : NotificationType.values()) {
            if (!type.configurable())
                continue;

            for (ChannelType channel : ChannelType.values()) {
                NotificationPreference preference = stored.get(new Key(type, channel));
                boolean defaultEnabled = type.defaultEnabled(channel);

                matrix.add(new NotificationPreferenceDto(
                        type,
                        type.category(),
                        type.label(),
                        type.description(),
                        channel,
                        preference != null ? preference.isEnabled() : defaultEnabled,
                        defaultEnabled,
                        preference != null && preference.getScope() != null ? preference.getScope() : type.defaultScope(),
                        type.supportedScopes()
                ));
            }
        }
        return matrix;
    }

    private record Key(NotificationType type, ChannelType channel) {
    }
}
