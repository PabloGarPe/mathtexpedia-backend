package mathtexpedia.es.api.service.pdfComment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
public class ModerationAvailability {

    private static final Logger logger = LoggerFactory.getLogger(ModerationAvailability.class);

    public record Status(boolean available, Instant since, Instant nextCheckAt) {}

    private final Duration initialDelay;
    private final Duration maxDelay;
    private final Clock clock;

    private boolean up = true;
    private Instant since;
    private int consecutiveFailures = 0;
    private Instant nextCheckAt;

    @Autowired
    public ModerationAvailability(@Value("${moderation.breaker.initial-delay-seconds}") long initialDelaySeconds,
                                  @Value("${moderation.breaker.max-delay-seconds}") long maxDelaySeconds) {
        this(Duration.ofSeconds(initialDelaySeconds), Duration.ofSeconds(maxDelaySeconds), Clock.systemUTC());
    }

    ModerationAvailability(Duration initialDelay, Duration maxDelay, Clock clock) {
        this.initialDelay = initialDelay;
        this.maxDelay = maxDelay;
        this.clock = clock;
        this.since = clock.instant();
    }

    public synchronized boolean canAttempt() {
        return up || !clock.instant().isBefore(nextCheckAt);
    }

    public synchronized boolean isDown() {
        return !up;
    }

    public synchronized void recordSuccess() {
        if (!up) {
            logger.info("AI moderation is available again (down since {})", since);
            up = true;
            since = clock.instant();
        }
        consecutiveFailures = 0;
        nextCheckAt = null;
    }

    public synchronized void recordFailure() {
        Instant now = clock.instant();
        if (up) {
            logger.warn("AI moderation is unavailable: comments will stay PENDING until it recovers");
            up = false;
            since = now;
        }
        consecutiveFailures++;
        nextCheckAt = now.plus(backoff(consecutiveFailures, initialDelay, maxDelay));
        logger.debug("Next AI moderation check at {} ({} consecutive failures)", nextCheckAt, consecutiveFailures);
    }

    public synchronized Status status() {
        return new Status(up, since, nextCheckAt);
    }

    static Duration backoff(int failures, Duration initial, Duration max) {
        int exponent = Math.clamp(failures - 1, 0, 20);
        Duration delay = initial.multipliedBy(1L << exponent);
        return delay.compareTo(max) > 0 ? max : delay;
    }

}
