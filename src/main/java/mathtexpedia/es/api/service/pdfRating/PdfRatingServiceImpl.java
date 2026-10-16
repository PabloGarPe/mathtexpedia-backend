package mathtexpedia.es.api.service.pdfRating;

import mathtexpedia.es.api.domain.exception.MathtexpediaForbiddenException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.pdfRating.CannotVoteReason;
import mathtexpedia.es.api.domain.model.pdfRating.MyPdfVoteDto;
import mathtexpedia.es.api.domain.model.pdfRating.PdfRatingDto;
import mathtexpedia.es.api.domain.model.pdfRating.PdfRatingSummaryDto;
import mathtexpedia.es.api.domain.model.pdfRating.PdfVote;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.persistence.pdf.PDFDataService;
import mathtexpedia.es.api.persistence.pdfRating.PdfRatingDataService;
import mathtexpedia.es.api.persistence.pdfRating.PdfVoteCounts;
import mathtexpedia.es.api.persistence.user.UserAccount;
import mathtexpedia.es.api.persistence.userEvent.UserEventDataService;
import mathtexpedia.es.api.service.userAccount.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PdfRatingServiceImpl implements PdfRatingService {

    private static final Logger logger = LoggerFactory.getLogger(PdfRatingServiceImpl.class);

    private static final int MAX_BATCH = 100;

    private final PdfRatingDataService pdfRatingDataService;
    private final PDFDataService pdfDataService;
    private final UserEventDataService userEventDataService;
    private final UserAccountService userAccountService;

    public PdfRatingServiceImpl(PdfRatingDataService pdfRatingDataService,
                                PDFDataService pdfDataService,
                                UserEventDataService userEventDataService,
                                UserAccountService userAccountService) {
        this.pdfRatingDataService = pdfRatingDataService;
        this.pdfDataService = pdfDataService;
        this.userEventDataService = userEventDataService;
        this.userAccountService = userAccountService;
    }

    @Override
    public Map<Long, PdfRatingSummaryDto> getSummaries(Collection<Long> pdfIds) {
        Map<Long, PdfVoteCounts> counts = pdfRatingDataService.countVotes(pdfIds);

        Map<Long, PdfRatingSummaryDto> summaries = new HashMap<>();
        for (Long pdfId : pdfIds) {
            PdfVoteCounts c = counts.get(pdfId);
            summaries.put(pdfId, c == null
                    ? PdfRatingSummaryDto.empty()
                    : new PdfRatingSummaryDto(c.likes(), c.dislikes(), PdfRatingScore.wilson(c.likes(), c.dislikes())));
        }
        return summaries;
    }

    @Override
    public PdfRatingDto getRating(long pdfId, UserProfile user)
            throws MathtexpediaNotFoundException, MathtexpediaUnauthorizedException {
        logger.info("Fetching rating of PDF {} for user: {}", pdfId, user.getId());

        UserAccount account = userAccountService.getOrProvision(user);
        PDF pdf = findPdfOrThrow(pdfId);
        return buildRating(pdf, account);
    }

    @Override
    public PdfRatingDto rate(long pdfId, PdfVote vote, UserProfile user)
            throws MathtexpediaNotFoundException, MathtexpediaUnauthorizedException, MathtexpediaForbiddenException {
        logger.info("User {} rating PDF {} with {}", user.getId(), pdfId, vote);

        UserAccount account = userAccountService.getOrProvision(user);
        PDF pdf = findPdfOrThrow(pdfId);

        if (isAuthor(pdf, account))
            throw new MathtexpediaForbiddenException("User is an author of PDF " + pdfId);
        if (!userEventDataService.hasViewedPdf(account.getId(), pdfId))
            throw new MathtexpediaForbiddenException("User has not opened PDF " + pdfId);

        pdfRatingDataService.upsert(pdfId, account.getId(), vote);
        return buildRating(pdf, account);
    }

    @Override
    public void removeRating(long pdfId, UserProfile user)
            throws MathtexpediaNotFoundException, MathtexpediaUnauthorizedException {
        logger.info("User {} removing rating of PDF {}", user.getId(), pdfId);

        UserAccount account = userAccountService.getOrProvision(user);
        findPdfOrThrow(pdfId);
        pdfRatingDataService.delete(pdfId, account.getId());
    }

    @Override
    public List<MyPdfVoteDto> getMyVotes(List<Long> pdfIds, UserProfile user)
            throws MathtexpediaInvalidException, MathtexpediaUnauthorizedException {
        logger.info("Fetching ratings of user {} for {} PDFs", user.getId(), pdfIds == null ? 0 : pdfIds.size());

        UserAccount account = userAccountService.getOrProvision(user);

        LinkedHashSet<Long> ids = pdfIds == null ? new LinkedHashSet<>() : new LinkedHashSet<>(pdfIds);
        ids.remove(null);
        if (ids.size() > MAX_BATCH)
            throw new MathtexpediaInvalidException("Too many PDF ids: maximum is " + MAX_BATCH);
        if (ids.isEmpty())
            return List.of();

        Map<Long, PdfVote> votes = pdfRatingDataService.getVotes(account.getId(), ids);

        List<MyPdfVoteDto> result = new ArrayList<>();
        for (Long id : ids) {
            PdfVote vote = votes.get(id);
            if (vote != null)
                result.add(new MyPdfVoteDto(id, vote));
        }
        return result;
    }

    private PdfRatingDto buildRating(PDF pdf, UserAccount account) {
        long pdfId = pdf.getId();
        PdfRatingSummaryDto summary = getSummaries(List.of(pdfId)).get(pdfId);
        PdfVote myVote = pdfRatingDataService.getVote(pdfId, account.getId()).orElse(null);

        // Ser autor tiene prioridad sobre no haber abierto el PDF
        CannotVoteReason reason = null;
        if (isAuthor(pdf, account))
            reason = CannotVoteReason.AUTHOR;
        else if (!userEventDataService.hasViewedPdf(account.getId(), pdfId))
            reason = CannotVoteReason.NOT_VIEWED;

        return new PdfRatingDto(pdfId, summary, myVote, reason == null, reason);
    }

    private boolean isAuthor(PDF pdf, UserAccount account) {
        return pdf.getAuthors().stream().anyMatch(author -> Objects.equals(author.getId(), account.getId()));
    }

    private PDF findPdfOrThrow(long pdfId) throws MathtexpediaNotFoundException {
        return pdfDataService.getPDFById(pdfId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("PDF not found with id: " + pdfId));
    }
}
