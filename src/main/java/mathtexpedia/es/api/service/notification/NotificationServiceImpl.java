package mathtexpedia.es.api.service.notification;

import mathtexpedia.es.api.domain.event.AnnouncementCreatedEvent;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.notification.ChannelType;
import mathtexpedia.es.api.domain.model.notification.CreateAnnouncementDto;
import mathtexpedia.es.api.domain.model.notification.MarkAllReadResultDto;
import mathtexpedia.es.api.domain.model.notification.NotificationDto;
import mathtexpedia.es.api.domain.model.notification.UnreadCountDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.persistence.notification.NotificationDataService;
import mathtexpedia.es.api.persistence.user.UserAccount;
import mathtexpedia.es.api.persistence.user.UserAccountDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private static final String NOT_FOUND_MESSAGE = "Notification not found with id: ";
    private static final int MAX_PAGE_SIZE = 50;

    // El front solo tiene el canal in-app por ahora; con el correo aparecerán canales que no son "la bandeja"
    private static final ChannelType INBOX_CHANNEL = ChannelType.IN_APP;

    private final NotificationDataService notificationDataService;
    private final UserAccountDataService userAccountDataService;
    private final NotificationMapper notificationMapper;
    private final ApplicationEventPublisher eventPublisher;

    public NotificationServiceImpl(NotificationDataService notificationDataService,
                                   UserAccountDataService userAccountDataService,
                                   NotificationMapper notificationMapper,
                                   ApplicationEventPublisher eventPublisher) {
        this.notificationDataService = notificationDataService;
        this.userAccountDataService = userAccountDataService;
        this.notificationMapper = notificationMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Page<NotificationDto> getMyNotifications(UserProfile user, boolean unreadOnly, int page, int size) {
        logger.info("Fetching notifications for user {} (unreadOnly: {}, page: {}, size: {})", user.getId(), unreadOnly, page, size);

        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, MAX_PAGE_SIZE)));
        return notificationDataService.getInbox(user.getId(), INBOX_CHANNEL, unreadOnly, pageRequest)
                .map(notificationMapper::toDto);
    }

    @Override
    public UnreadCountDto getUnreadCount(UserProfile user) {
        logger.debug("Counting unread notifications for user {}", user.getId());

        return new UnreadCountDto(notificationDataService.countUnread(user.getId(), INBOX_CHANNEL));
    }

    @Override
    public void markAsRead(UserProfile user, long id) throws MathtexpediaNotFoundException {
        logger.info("Marking notification {} as read for user {}", id, user.getId());

        requireOwned(user, id);
        notificationDataService.markRead(id, user.getId());
    }

    @Override
    public void markAsUnread(UserProfile user, long id) throws MathtexpediaNotFoundException {
        logger.info("Marking notification {} as unread for user {}", id, user.getId());

        requireOwned(user, id);
        notificationDataService.markUnread(id, user.getId());
    }

    @Override
    public MarkAllReadResultDto markAllAsRead(UserProfile user) {
        logger.info("Marking all notifications as read for user {}", user.getId());

        return new MarkAllReadResultDto(notificationDataService.markAllRead(user.getId(), INBOX_CHANNEL));
    }

    @Override
    public void delete(UserProfile user, long id) throws MathtexpediaNotFoundException {
        logger.info("Deleting notification {} for user {}", id, user.getId());

        if (!notificationDataService.deleteDelivery(id, user.getId()))
            throw new MathtexpediaNotFoundException(NOT_FOUND_MESSAGE + id);
    }

    @Override
    public void createAnnouncement(UserProfile admin, CreateAnnouncementDto dto)
            throws MathtexpediaNotFoundException, MathtexpediaInvalidException {
        logger.info("Creating announcement from admin {} (recipients: {})", admin.getId(),
                dto.getRecipientEmails() == null || dto.getRecipientEmails().isEmpty() ? "all" : dto.getRecipientEmails().size());

        validateLink(dto.getLink());
        List<Long> recipientIds = resolveRecipients(dto.getRecipientEmails());

        eventPublisher.publishEvent(new AnnouncementCreatedEvent(
                dto.getTitle().trim(), dto.getMessage().trim(), blankToNull(dto.getLink()), recipientIds, admin.getId()));
    }

    private void requireOwned(UserProfile user, long id) throws MathtexpediaNotFoundException {
        if (!notificationDataService.existsDelivery(id, user.getId()))
            throw new MathtexpediaNotFoundException(NOT_FOUND_MESSAGE + id);
    }

    /** Solo rutas internas del front: evita que un aviso lleve a una web externa. */
    private void validateLink(String link) throws MathtexpediaInvalidException {
        if (link == null || link.isBlank())
            return;

        boolean internal = link.startsWith("/") && !link.startsWith("//") && !link.contains("\\")
                && link.chars().noneMatch(c -> Character.isISOControl(c) || Character.isWhitespace(c));
        if (!internal)
            throw new MathtexpediaInvalidException("Announcement link must be an internal path starting with a single '/'");
    }

    private List<Long> resolveRecipients(List<String> recipientEmails) throws MathtexpediaNotFoundException, MathtexpediaInvalidException {
        if (recipientEmails == null || recipientEmails.isEmpty())
            return null;

        List<String> emails = recipientEmails.stream().map(email -> email.trim().toLowerCase()).distinct().toList();

        Map<String, UserAccount> usersByEmail = new HashMap<>();
        for (UserAccount user : userAccountDataService.getByEmails(emails)) {
            if (usersByEmail.put(user.getEmail().toLowerCase(), user) != null)
                throw new MathtexpediaInvalidException("More than one user found with email: " + user.getEmail());
        }

        List<Long> ids = new ArrayList<>(emails.size());
        for (String email : emails) {
            UserAccount user = usersByEmail.get(email);
            if (user == null)
                throw new MathtexpediaNotFoundException("User not found with email: " + email);
            ids.add(user.getId());
        }
        return ids;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
