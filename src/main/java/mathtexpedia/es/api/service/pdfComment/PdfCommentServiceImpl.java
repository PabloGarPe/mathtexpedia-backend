package mathtexpedia.es.api.service.pdfComment;

import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaForbiddenException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaTooManyRequestsException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.pdfComment.CommentConfigDto;
import mathtexpedia.es.api.domain.model.pdfComment.CommentContextDto;
import mathtexpedia.es.api.domain.model.pdfComment.CommentDto;
import mathtexpedia.es.api.domain.model.pdfComment.CommentPageDto;
import mathtexpedia.es.api.domain.model.pdfComment.CommentStatus;
import mathtexpedia.es.api.domain.model.pdfComment.CreateCommentDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.domain.security.UserRole;
import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.persistence.pdf.PDFDataService;
import mathtexpedia.es.api.persistence.pdfComment.PdfComment;
import mathtexpedia.es.api.persistence.pdfComment.PdfCommentDataService;
import mathtexpedia.es.api.persistence.pdfCommentLike.PdfCommentLikeDataService;
import mathtexpedia.es.api.persistence.user.UserAccount;
import mathtexpedia.es.api.service.userAccount.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PdfCommentServiceImpl implements PdfCommentService {

    private static final Logger logger = LoggerFactory.getLogger(PdfCommentServiceImpl.class);

    private static final int MAX_PAGE_SIZE = 50;

    private final PdfCommentDataService pdfCommentDataService;
    private final PdfCommentLikeDataService likeDataService;
    private final PDFDataService pdfDataService;
    private final UserAccountService userAccountService;
    private final CommentContentValidator contentValidator;
    private final PdfCommentMapper mapper;
    private final int maxPendingPerUser;

    public PdfCommentServiceImpl(PdfCommentDataService pdfCommentDataService,
                                 PdfCommentLikeDataService likeDataService,
                                 PDFDataService pdfDataService,
                                 UserAccountService userAccountService,
                                 CommentContentValidator contentValidator,
                                 PdfCommentMapper mapper,
                                 @Value("${comments.max-pending-per-user}") int maxPendingPerUser) {
        this.pdfCommentDataService = pdfCommentDataService;
        this.likeDataService = likeDataService;
        this.pdfDataService = pdfDataService;
        this.userAccountService = userAccountService;
        this.contentValidator = contentValidator;
        this.mapper = mapper;
        this.maxPendingPerUser = maxPendingPerUser;
    }

    @Override
    public CommentDto create(long pdfId, CreateCommentDto dto, UserProfile user)
            throws MathtexpediaUnauthorizedException, MathtexpediaForbiddenException, MathtexpediaNotFoundException,
            MathtexpediaInvalidException, MathtexpediaConflictException, MathtexpediaTooManyRequestsException {
        logger.info("User {} commenting on PDF {}", user.getId(), pdfId);

        UserAccount account = userAccountService.getOrProvision(user);
        if (account.getUsername() == null)
            throw new MathtexpediaForbiddenException("User " + user.getId() + " has not completed the profile");

        PDF pdf = findPdfOrThrow(pdfId);
        String body = contentValidator.validate(dto.getBody());

        if (pdfCommentDataService.countByUserAndStatus(account.getId(), CommentStatus.PENDING) >= maxPendingPerUser)
            throw new MathtexpediaTooManyRequestsException("User " + user.getId() + " has too many pending comments");

        PdfComment replyTo = dto.getReplyToId() == null ? null : findReplyTarget(dto.getReplyToId(), pdfId);

        PdfComment comment = new PdfComment();
        comment.setPdf(pdf);
        comment.setUserAccount(account);
        comment.setBody(body);
        if (replyTo != null) {
            // Solo hay dos niveles: contestar a una respuesta cuelga de la misma raíz
            comment.setParent(replyTo.getParent() != null ? replyTo.getParent() : replyTo);
            comment.setReplyTo(replyTo);
        }

        return mapper.toOwnDto(pdfCommentDataService.create(comment), authorIds(pdf), 0L);
    }

    @Override
    public CommentPageDto getRoots(long pdfId, int page, int size) throws MathtexpediaNotFoundException {
        logger.info("Fetching comments of PDF {} (page {}, size {})", pdfId, page, size);

        PDF pdf = findPdfOrThrow(pdfId);
        int safePage = Math.max(0, page);
        int safeSize = clampSize(size);
        int offset = safePage * safeSize;

        List<PdfComment> roots = pdfCommentDataService.findVisibleRoots(pdfId, offset, safeSize);
        long total = pdfCommentDataService.countVisibleRoots(pdfId);
        Map<Long, Long> replyCounts = pdfCommentDataService.countPublishedReplies(roots.stream().map(PdfComment::getId).toList());
        Map<Long, Long> likeCounts = likeDataService.countByComment(roots.stream().map(PdfComment::getId).toList());
        Set<Long> pdfAuthorIds = authorIds(pdf);

        List<CommentDto> items = roots.stream()
                .map(root -> mapper.toPublicDto(root, pdfAuthorIds, replyCounts.getOrDefault(root.getId(), 0L),
                        likeCounts.getOrDefault(root.getId(), 0L)))
                .toList();

        return new CommentPageDto(items, safePage, safeSize, total, offset + items.size() < total);
    }

    @Override
    public CommentPageDto getReplies(long rootId, int page, int size) throws MathtexpediaNotFoundException {
        logger.info("Fetching replies of comment {} (page {}, size {})", rootId, page, size);

        PdfComment root = pdfCommentDataService.findById(rootId)
                .filter(c -> c.getParent() == null)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Root comment not found with id: " + rootId));

        int safePage = Math.max(0, page);
        int safeSize = clampSize(size);
        int offset = safePage * safeSize;

        List<PdfComment> replies = pdfCommentDataService.findPublishedReplies(rootId, offset, safeSize);
        long total = pdfCommentDataService.countPublishedReplies(List.of(rootId)).getOrDefault(rootId, 0L);
        Map<Long, Long> likeCounts = likeDataService.countByComment(replies.stream().map(PdfComment::getId).toList());
        Set<Long> pdfAuthorIds = authorIds(root.getPdf());

        List<CommentDto> items = replies.stream()
                .map(reply -> mapper.toPublicDto(reply, pdfAuthorIds, null, likeCounts.getOrDefault(reply.getId(), 0L)))
                .toList();

        return new CommentPageDto(items, safePage, safeSize, total, offset + items.size() < total);
    }

    @Override
    public CommentContextDto getContext(long commentId) throws MathtexpediaNotFoundException {
        logger.info("Fetching context of comment {}", commentId);

        PdfComment target = pdfCommentDataService.findById(commentId)
                .filter(c -> c.getStatus() == CommentStatus.PUBLISHED)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Published comment not found with id: " + commentId));

        // La raíz se vuelve a cargar entera: el padre de una respuesta viene sin sus relaciones
        boolean targetIsRoot = target.getParent() == null;
        PdfComment root = targetIsRoot ? target : pdfCommentDataService.findById(target.getParent().getId())
                .orElseThrow(() -> new MathtexpediaNotFoundException("Root comment not found for comment: " + commentId));

        Set<Long> pdfAuthorIds = authorIds(findPdfOrThrow(target.getPdf().getId()));
        long rootId = root.getId();
        long replyCount = pdfCommentDataService.countPublishedReplies(List.of(rootId)).getOrDefault(rootId, 0L);
        Map<Long, Long> likeCounts = likeDataService.countByComment(List.of(rootId, commentId));

        CommentDto rootDto = mapper.toPublicDto(root, pdfAuthorIds, replyCount, likeCounts.getOrDefault(rootId, 0L));
        CommentDto targetDto = targetIsRoot ? rootDto
                : mapper.toPublicDto(target, pdfAuthorIds, null, likeCounts.getOrDefault(commentId, 0L));

        return new CommentContextDto(rootDto, targetDto);
    }

    @Override
    public CommentConfigDto getConfig() {
        return new CommentConfigDto(contentValidator.getMaxLength(), maxPendingPerUser);
    }

    @Override
    public List<CommentDto> getMine(long pdfId, UserProfile user)
            throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException {
        logger.info("Fetching comments of user {} on PDF {}", user.getId(), pdfId);

        UserAccount account = userAccountService.getOrProvision(user);
        PDF pdf = findPdfOrThrow(pdfId);
        Set<Long> pdfAuthorIds = authorIds(pdf);

        List<PdfComment> comments = pdfCommentDataService.findByUserAndPdf(account.getId(), pdfId);
        Map<Long, Long> likeCounts = likeDataService.countByComment(comments.stream().map(PdfComment::getId).toList());

        return comments.stream()
                .map(comment -> mapper.toOwnDto(comment, pdfAuthorIds, likeCounts.getOrDefault(comment.getId(), 0L)))
                .toList();
    }

    @Override
    public void delete(long commentId, UserProfile user)
            throws MathtexpediaUnauthorizedException, MathtexpediaForbiddenException, MathtexpediaNotFoundException {
        logger.info("User {} deleting comment {}", user.getId(), commentId);

        UserAccount account = userAccountService.getOrProvision(user);
        PdfComment comment = pdfCommentDataService.findById(commentId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Comment not found with id: " + commentId));

        boolean isAuthor = Objects.equals(comment.getUserAccount().getId(), account.getId());
        if (!isAuthor && !UserRole.ADMIN.equals(user.getRole()))
            throw new MathtexpediaForbiddenException("User " + user.getId() + " cannot delete comment " + commentId);

        // Idempotente: si ya estaba borrado no hay nada que hacer
        pdfCommentDataService.markDeleted(commentId, Instant.now());
    }

    /** El comentario al que se responde debe existir, ser del mismo PDF y estar publicado. */
    private PdfComment findReplyTarget(long replyToId, long pdfId)
            throws MathtexpediaNotFoundException, MathtexpediaInvalidException, MathtexpediaConflictException {
        PdfComment target = pdfCommentDataService.findById(replyToId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Comment not found with id: " + replyToId));

        if (target.getPdf().getId() != pdfId)
            throw new MathtexpediaInvalidException("Comment " + replyToId + " belongs to another PDF");
        if (target.getStatus() != CommentStatus.PUBLISHED)
            throw new MathtexpediaConflictException("Comment " + replyToId + " is not published");
        return target;
    }

    private PDF findPdfOrThrow(long pdfId) throws MathtexpediaNotFoundException {
        return pdfDataService.getPDFById(pdfId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("PDF not found with id: " + pdfId));
    }

    private Set<Long> authorIds(PDF pdf) {
        return pdf.getAuthors().stream().map(UserAccount::getId).collect(Collectors.toSet());
    }

    private int clampSize(int size) {
        return Math.max(1, Math.min(size, MAX_PAGE_SIZE));
    }
}
