package mathtexpedia.es.api.service.pdfComment;

import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaForbiddenException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.pdfComment.CommentLikeDto;
import mathtexpedia.es.api.domain.model.pdfComment.CommentStatus;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.persistence.pdfComment.PdfComment;
import mathtexpedia.es.api.persistence.pdfComment.PdfCommentDataService;
import mathtexpedia.es.api.persistence.pdfCommentLike.PdfCommentLikeDataService;
import mathtexpedia.es.api.persistence.user.UserAccount;
import mathtexpedia.es.api.service.userAccount.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class PdfCommentLikeServiceImpl implements PdfCommentLikeService {

    private static final Logger logger = LoggerFactory.getLogger(PdfCommentLikeServiceImpl.class);

    private static final int MAX_BATCH = 100;

    private final PdfCommentDataService pdfCommentDataService;
    private final PdfCommentLikeDataService likeDataService;
    private final UserAccountService userAccountService;

    public PdfCommentLikeServiceImpl(PdfCommentDataService pdfCommentDataService,
                                     PdfCommentLikeDataService likeDataService,
                                     UserAccountService userAccountService) {
        this.pdfCommentDataService = pdfCommentDataService;
        this.likeDataService = likeDataService;
        this.userAccountService = userAccountService;
    }

    @Override
    public CommentLikeDto like(long commentId, UserProfile user)
            throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaForbiddenException,
            MathtexpediaConflictException {
        logger.info("User {} liking comment {}", user.getId(), commentId);

        UserAccount account = userAccountService.getOrProvision(user);
        PdfComment comment = findOrThrow(commentId);

        if (Objects.equals(comment.getUserAccount().getId(), account.getId()))
            throw new MathtexpediaForbiddenException("User " + user.getId() + " cannot like their own comment");
        if (comment.getStatus() != CommentStatus.PUBLISHED)
            throw new MathtexpediaConflictException("Comment " + commentId + " is not published");

        likeDataService.insertIfAbsent(commentId, account.getId(), Instant.now());
        return build(commentId, true);
    }

    @Override
    public CommentLikeDto unlike(long commentId, UserProfile user)
            throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException {
        logger.info("User {} removing like of comment {}", user.getId(), commentId);

        UserAccount account = userAccountService.getOrProvision(user);
        findOrThrow(commentId);

        likeDataService.delete(commentId, account.getId());
        return build(commentId, false);
    }

    @Override
    public List<Long> getMyLikes(List<Long> commentIds, UserProfile user)
            throws MathtexpediaUnauthorizedException, MathtexpediaInvalidException {
        logger.info("Fetching likes of user {} for {} comments", user.getId(), commentIds == null ? 0 : commentIds.size());

        UserAccount account = userAccountService.getOrProvision(user);

        LinkedHashSet<Long> ids = commentIds == null ? new LinkedHashSet<>() : new LinkedHashSet<>(commentIds);
        ids.remove(null);
        if (ids.size() > MAX_BATCH)
            throw new MathtexpediaInvalidException("Too many comment ids: maximum is " + MAX_BATCH);
        if (ids.isEmpty())
            return List.of();

        Set<Long> liked = likeDataService.findLikedIds(account.getId(), ids);
        return ids.stream().filter(liked::contains).toList();
    }

    private CommentLikeDto build(long commentId, boolean liked) {
        long count = likeDataService.countByComment(List.of(commentId)).getOrDefault(commentId, 0L);
        return new CommentLikeDto(commentId, count, liked);
    }

    private PdfComment findOrThrow(long commentId) throws MathtexpediaNotFoundException {
        return pdfCommentDataService.findById(commentId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Comment not found with id: " + commentId));
    }
}
