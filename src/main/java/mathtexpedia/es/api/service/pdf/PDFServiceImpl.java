package mathtexpedia.es.api.service.pdf;

import jakarta.persistence.PersistenceException;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.event.PdfAuthorsAddedEvent;
import mathtexpedia.es.api.domain.event.PdfCreatedEvent;
import mathtexpedia.es.api.domain.event.PdfFileReplacedEvent;
import mathtexpedia.es.api.domain.event.ResourceDeletedEvent;
import mathtexpedia.es.api.domain.model.notification.ResourceType;
import mathtexpedia.es.api.domain.model.pdf.CreatePDFDto;
import mathtexpedia.es.api.domain.model.pdf.PDFContent;
import mathtexpedia.es.api.domain.model.pdf.PDFDto;
import mathtexpedia.es.api.domain.model.pdf.UpdatePDFDto;
import mathtexpedia.es.api.domain.model.userEvent.EventType;
import mathtexpedia.es.api.domain.port.pdf.PDFStoragePort;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.infrastructure.security.CurrentUser;
import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.persistence.pdf.PDFDataService;
import mathtexpedia.es.api.persistence.subject.Subject;
import mathtexpedia.es.api.persistence.subject.SubjectDataService;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnit;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnitDataService;
import mathtexpedia.es.api.persistence.user.UserAccount;
import mathtexpedia.es.api.persistence.user.UserAccountDataService;
import mathtexpedia.es.api.persistence.userEvent.UserEventDataService;
import mathtexpedia.es.api.service.userAccount.UserAccountService;
import mathtexpedia.es.api.service.userEvent.UserEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class PDFServiceImpl implements PDFService {

    private static final Logger logger = LoggerFactory.getLogger(PDFServiceImpl.class);

    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final byte[] PDF_MAGIC = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final String KEY_PREFIX = "pdfs/";
    private static final int DEFAULT_FEED_LIMIT = 10;
    private static final int MAX_FEED_LIMIT = 20;

    private final PDFDataService pdfDataService;
    private final SubjectUnitDataService subjectUnitDataService;
    private final SubjectDataService subjectDataService;
    private final PDFMapper pdfMapper;
    private final UserEventService userEventService;
    private final UserAccountService userAccountService;
    private final UserAccountDataService userAccountDataService;
    private final PDFStoragePort pdfStoragePort;
    private final UserEventDataService userEventDataService;
    private final ApplicationEventPublisher eventPublisher;
    private final CurrentUser currentUser;

    public PDFServiceImpl(
            PDFDataService pdfDataService,
            SubjectUnitDataService subjectUnitDataService,
            SubjectDataService subjectDataService,
            PDFMapper pdfMapper,
            UserEventService userEventService,
            UserAccountService userAccountService,
            UserAccountDataService userAccountDataService,
            PDFStoragePort pdfStoragePort,
            UserEventDataService userEventDataService,
            ApplicationEventPublisher eventPublisher,
            CurrentUser currentUser) {
        this.pdfDataService = pdfDataService;
        this.subjectUnitDataService = subjectUnitDataService;
        this.subjectDataService = subjectDataService;
        this.pdfMapper = pdfMapper;
        this.userEventService = userEventService;
        this.userAccountService = userAccountService;
        this.userAccountDataService = userAccountDataService;
        this.pdfStoragePort = pdfStoragePort;
        this.userEventDataService = userEventDataService;
        this.eventPublisher = eventPublisher;
        this.currentUser = currentUser;
    }


    @Override
    public List<PDFDto> getPDFs() {
        logger.info("Fetching all PDFs");

        return toDtos(pdfDataService.getAll());
    }

    @Override
    public List<PDFDto> getRecentPDFs(Integer limit) {
        logger.info("Fetching recent PDFs (limit: {})", limit);

        return toDtos(pdfDataService.getRecent(normalizeLimit(limit)));
    }

    @Override
    public List<PDFDto> getTopViewedPDFs(Integer limit) {
        logger.info("Fetching most viewed PDFs (limit: {})", limit);

        return toDtosInOrder(userEventDataService.getTopViewedPdfIds(normalizeLimit(limit)));
    }

    @Override
    public List<PDFDto> getTopViewedPDFsForUser(UserProfile user, Integer limit) throws MathtexpediaUnauthorizedException {
        logger.info("Fetching most viewed PDFs for user: {} (limit: {})", user.getId(), limit);

        UserAccount account = userAccountService.getOrProvision(user);
        return toDtosInOrder(userEventDataService.getTopViewedPdfIdsByUser(account.getId(), normalizeLimit(limit)));
    }

    @Override
    public PDFContent getPDFContent(long pdfId, UserProfile user) throws MathtexpediaNotFoundException {
        logger.info("Fetching content of PDF with id: {} for user: {}", pdfId, user.getId());

        PDF pdf = findByIdOrThrow(pdfId);

        PDFContent content = pdfStoragePort.download(pdf.getS3Key())
                .orElseThrow(() -> {
                    logger.error("PDF {} exists in DB but not in S3 (key {})", pdfId, pdf.getS3Key());
                    return new MathtexpediaNotFoundException("PDF content not available for id: " + pdfId);
                });

        recordViewEvent(pdf, user);
        return content;
    }

    @Override
    public List<PDFDto> getPDFsBySubjectUnit(long subjectUnitId) throws MathtexpediaNotFoundException {
        logger.info("Fetching PDFs for subject unit with id: {}", subjectUnitId);

        if (subjectUnitDataService.getById(subjectUnitId).isEmpty())
            throw new MathtexpediaNotFoundException("Subject unit not found with id: " + subjectUnitId);

        return toDtos(pdfDataService.getAllForSubjectUnit(subjectUnitId));
    }

    @Override
    public List<PDFDto> getPDFsBySubject(long subjectId) throws MathtexpediaNotFoundException {
        logger.info("Fetching PDFs for subject with id: {}", subjectId);

        if (subjectDataService.getById(subjectId).isEmpty())
            throw new MathtexpediaNotFoundException("Subject not found with id: " + subjectId);

        return toDtos(pdfDataService.getAllForSubject(subjectId));
    }


    @Override
    public PDFDto createPDF(CreatePDFDto dto, MultipartFile file)
            throws MathtexpediaConflictException, MathtexpediaNotFoundException, MathtexpediaInvalidException {
        logger.info("Creating new PDF with name: {}", dto.getName());

        validateFile(file);

        if (pdfDataService.findByNameAndSubjectId(dto.getName(), dto.getSubjectId()).isPresent())
            throw new MathtexpediaConflictException(
                    "PDF already exists with name: " + dto.getName() + " in subject with id: " + dto.getSubjectId());

        PDF pdf = pdfMapper.toEntity(dto);
        pdf.setLastTimeEdited(new Date());
        resolveSubjectAndUnit(pdf, dto.getSubjectId(), dto.getSubjectUnitId());
        resolveAuthors(pdf, dto.getAuthorEmails());

        String key = newKey();
        pdf.setS3Key(key);

        upload(key, file);

        try {
            PDF created = pdfDataService.createPDF(pdf);
            eventPublisher.publishEvent(new PdfCreatedEvent(
                    created.getId(), authorIds(created), currentUser.externalIdOrNull()));
            return pdfMapper.toDto(created, 0);
        } catch (RuntimeException e) {
            safeDelete(key);
            if (e instanceof PersistenceException)
                throw new MathtexpediaConflictException("Error creating PDF: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public PDFDto updatePDF(long pdfId, UpdatePDFDto dto, MultipartFile file)
            throws MathtexpediaNotFoundException, MathtexpediaConflictException, MathtexpediaInvalidException {
        logger.info("Updating PDF with id: {} (new file: {})", pdfId, file != null && !file.isEmpty());

        PDF toUpdate = findByIdOrThrow(pdfId);

        Optional<PDF> sameName = pdfDataService.findByNameAndSubjectId(dto.getName(), dto.getSubjectId());
        if (sameName.isPresent() && !Objects.equals(sameName.get().getId(), pdfId))
            throw new MathtexpediaConflictException(
                    "PDF already exists with name: " + dto.getName() + " in subject with id: " + dto.getSubjectId());

        boolean replaceFile = file != null && !file.isEmpty();
        if (replaceFile)
            validateFile(file);

        Set<Long> previousAuthorIds = new HashSet<>(authorIds(toUpdate));

        pdfMapper.updateEntity(toUpdate, dto);
        toUpdate.setLastTimeEdited(new Date());
        resolveSubjectAndUnit(toUpdate, dto.getSubjectId(), dto.getSubjectUnitId());
        resolveAuthors(toUpdate, dto.getAuthorEmails());

        String oldKey = toUpdate.getS3Key();
        String newKey = null;

        if (replaceFile) {
            newKey = newKey();
            upload(newKey, file);
            toUpdate.setS3Key(newKey);
        }

        PDF updated;
        try {
            updated = pdfDataService.updatePDF(toUpdate);
        } catch (RuntimeException e) {
            if (newKey != null)
                safeDelete(newKey);
            if (e instanceof PersistenceException)
                throw new MathtexpediaConflictException("Error updating PDF: " + e.getMessage(), e);
            throw e;
        }

        if (newKey != null)
            safeDelete(oldKey);

        publishUpdateEvents(updated, previousAuthorIds, newKey != null);

        return toDtos(List.of(updated)).get(0);
    }

    @Override
    public void deletePDF(long pdfId) throws MathtexpediaNotFoundException {
        logger.info("Deleting PDF with id: {}", pdfId);

        PDF toDelete = findByIdOrThrow(pdfId);
        String key = toDelete.getS3Key();

        pdfDataService.deletePDF(toDelete);
        safeDelete(key);

        eventPublisher.publishEvent(new ResourceDeletedEvent(ResourceType.PDF, pdfId));
    }

    private List<Long> authorIds(PDF pdf) {
        return pdf.getAuthors().stream().map(UserAccount::getId).toList();
    }

    private void publishUpdateEvents(PDF updated, Set<Long> previousAuthorIds, boolean fileReplaced) {
        String actor = currentUser.externalIdOrNull();

        List<Long> addedAuthorIds = authorIds(updated).stream()
                .filter(id -> !previousAuthorIds.contains(id))
                .toList();
        if (!addedAuthorIds.isEmpty())
            eventPublisher.publishEvent(new PdfAuthorsAddedEvent(updated.getId(), addedAuthorIds, actor));

        // Solo el reemplazo del fichero se notifica: cambiar nombre o descripción no es una novedad para el lector
        if (fileReplaced)
            eventPublisher.publishEvent(new PdfFileReplacedEvent(updated.getId(), actor));
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null)
            return DEFAULT_FEED_LIMIT;
        return Math.max(1, Math.min(limit, MAX_FEED_LIMIT));
    }

    private List<PDFDto> toDtos(List<PDF> pdfs) {
        Map<Long, Long> views = userEventDataService.countPdfViews(pdfs.stream().map(PDF::getId).toList());
        return pdfs.stream()
                .map(pdf -> pdfMapper.toDto(pdf, views.getOrDefault(pdf.getId(), 0L)))
                .toList();
    }

    private List<PDFDto> toDtosInOrder(List<Long> orderedIds) {
        Map<Long, PDF> byId = new HashMap<>();
        pdfDataService.getByIds(orderedIds).forEach(pdf -> byId.put(pdf.getId(), pdf));

        return toDtos(orderedIds.stream().map(byId::get).filter(Objects::nonNull).toList());
    }

    private PDF findByIdOrThrow(long pdfId) throws MathtexpediaNotFoundException {
        return pdfDataService.getPDFById(pdfId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("PDF not found with id: " + pdfId));
    }

    private void recordViewEvent(PDF pdf, UserProfile user) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("pdfId", pdf.getId());
            userEventService.record(userAccountService.getOrProvision(user), EventType.PDF_VIEWED, eventData);
        } catch (MathtexpediaUnauthorizedException e) {
            logger.warn("User {} is not authorized to view PDF {}", user.getId(), pdf.getId());
        }
    }

    private void resolveSubjectAndUnit(PDF target, Long subjectId, Long subjectUnitId)
            throws MathtexpediaNotFoundException, MathtexpediaInvalidException {

        Subject subject = subjectDataService.getById(subjectId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Subject not found with id: " + subjectId));
        target.setSubject(subject);

        if (subjectUnitId != null) {
            SubjectUnit subjectUnit = subjectUnitDataService.getById(subjectUnitId)
                    .orElseThrow(() -> new MathtexpediaNotFoundException("Subject unit not found with id: " + subjectUnitId));

            if (!Objects.equals(subjectUnit.getSubject().getId(), subjectId))
                throw new MathtexpediaInvalidException("Subject unit with id: " + subjectUnitId + " does not belong to subject with id: " + subjectId);

            target.setSubjectUnit(subjectUnit);
        } else {
            target.setSubjectUnit(null);
        }
    }

    private void resolveAuthors(PDF target, List<String> authorEmails)
            throws MathtexpediaNotFoundException, MathtexpediaInvalidException {

        if (authorEmails == null || authorEmails.isEmpty())
            throw new MathtexpediaInvalidException("PDF must have at least one author");

        List<String> emails = authorEmails.stream()
                .map(email -> email.trim().toLowerCase())
                .toList();

        if (new HashSet<>(emails).size() != emails.size())
            throw new MathtexpediaInvalidException("Author list contains duplicated users");

        Map<String, UserAccount> usersByEmail = new HashMap<>();
        for (UserAccount user : userAccountDataService.getByEmails(emails)) {
            if (usersByEmail.put(user.getEmail().toLowerCase(), user) != null)
                throw new MathtexpediaInvalidException("More than one user found with email: " + user.getEmail());
        }

        List<UserAccount> authors = new ArrayList<>(emails.size());
        for (String email : emails) {
            UserAccount author = usersByEmail.get(email);
            if (author == null)
                throw new MathtexpediaNotFoundException("User not found with email: " + email);
            authors.add(author);
        }

        target.getAuthors().clear();
        target.getAuthors().addAll(authors);
    }

    /** Clave opaca: renombrar o mover de tema no obliga a mover el objeto en S3. */
    private String newKey() {
        return KEY_PREFIX + UUID.randomUUID() + ".pdf";
    }

    private void validateFile(MultipartFile file) throws MathtexpediaInvalidException {
        if (file == null || file.isEmpty())
            throw new MathtexpediaInvalidException("PDF file is required");

        if (file.getContentType() != null && !PDF_CONTENT_TYPE.equalsIgnoreCase(file.getContentType()))
            throw new MathtexpediaInvalidException("File must be of type application/pdf");

        // El Content-Type lo pone el cliente; se comprueba también la cabecera real del fichero.
        try (InputStream in = file.getInputStream()) {
            if (!Arrays.equals(in.readNBytes(PDF_MAGIC.length), PDF_MAGIC))
                throw new MathtexpediaInvalidException("File is not a valid PDF");
        } catch (IOException e) {
            throw new MathtexpediaInvalidException("Uploaded file could not be read");
        }
    }

    private void upload(String key, MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            pdfStoragePort.upload(key, in, file.getSize());
        } catch (IOException e) {
            throw new UncheckedIOException("Error reading uploaded file", e);
        }
    }

    private void safeDelete(String key) {
        try {
            pdfStoragePort.delete(key);
        } catch (RuntimeException e) {
            logger.warn("Could not delete S3 object {} (orphan left in bucket)", key, e);
        }
    }
}