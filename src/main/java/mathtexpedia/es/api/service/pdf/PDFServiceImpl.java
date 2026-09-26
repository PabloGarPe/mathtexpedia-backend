package mathtexpedia.es.api.service.pdf;

import jakarta.persistence.PersistenceException;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.pdf.CreatePDFDto;
import mathtexpedia.es.api.domain.model.pdf.PDFContent;
import mathtexpedia.es.api.domain.model.pdf.PDFDto;
import mathtexpedia.es.api.domain.model.pdf.UpdatePDFDto;
import mathtexpedia.es.api.domain.model.userEvent.EventType;
import mathtexpedia.es.api.domain.port.pdf.PDFStoragePort;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.persistence.pdf.PDFDataService;
import mathtexpedia.es.api.persistence.subject.Subject;
import mathtexpedia.es.api.persistence.subject.SubjectDataService;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnit;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnitDataService;
import mathtexpedia.es.api.service.userAccount.UserAccountService;
import mathtexpedia.es.api.service.userEvent.UserEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final PDFDataService pdfDataService;
    private final SubjectUnitDataService subjectUnitDataService;
    private final SubjectDataService subjectDataService;
    private final PDFMapper pdfMapper;
    private final UserEventService userEventService;
    private final UserAccountService userAccountService;
    private final PDFStoragePort pdfStoragePort;

    public PDFServiceImpl(
            PDFDataService pdfDataService,
            SubjectUnitDataService subjectUnitDataService,
            SubjectDataService subjectDataService,
            PDFMapper pdfMapper,
            UserEventService userEventService,
            UserAccountService userAccountService,
            PDFStoragePort pdfStoragePort) {
        this.pdfDataService = pdfDataService;
        this.subjectUnitDataService = subjectUnitDataService;
        this.subjectDataService = subjectDataService;
        this.pdfMapper = pdfMapper;
        this.userEventService = userEventService;
        this.userAccountService = userAccountService;
        this.pdfStoragePort = pdfStoragePort;
    }


    @Override
    public List<PDFDto> getPDFs() {
        logger.info("Fetching all PDFs");

        return pdfDataService.getAll()
                .stream()
                .map(pdfMapper::toDto)
                .toList();
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

        return pdfDataService.getAllForSubjectUnit(subjectUnitId)
                .stream()
                .map(pdfMapper::toDto)
                .toList();
    }

    @Override
    public List<PDFDto> getPDFsBySubject(long subjectId) throws MathtexpediaNotFoundException {
        logger.info("Fetching PDFs for subject with id: {}", subjectId);

        if (subjectDataService.getById(subjectId).isEmpty())
            throw new MathtexpediaNotFoundException("Subject not found with id: " + subjectId);

        return pdfDataService.getAllForSubject(subjectId)
                .stream()
                .map(pdfMapper::toDto)
                .toList();
    }


    @Override
    public PDFDto createPDF(CreatePDFDto dto, MultipartFile file)
            throws MathtexpediaConflictException, MathtexpediaNotFoundException, MathtexpediaInvalidException {
        logger.info("Creating new PDF with name: {}", dto.getName());

        validateFile(file);

        if (pdfDataService.getPDF(dto.getName()).isPresent())
            throw new MathtexpediaConflictException("PDF already exists with name: " + dto.getName());

        PDF pdf = pdfMapper.toEntity(dto);
        pdf.setLastTimeEdited(new Date());
        resolveSubjectAndUnit(pdf, dto.getSubjectId(), dto.getSubjectUnitId());

        String key = newKey();
        pdf.setS3Key(key);

        upload(key, file);

        try {
            PDF created = pdfDataService.createPDF(pdf);
            return pdfMapper.toDto(created);
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

        Optional<PDF> sameName = pdfDataService.getPDF(dto.getName());
        if (sameName.isPresent() && !Objects.equals(sameName.get().getId(), pdfId))
            throw new MathtexpediaConflictException("PDF already exists with name: " + dto.getName());

        boolean replaceFile = file != null && !file.isEmpty();
        if (replaceFile)
            validateFile(file);

        pdfMapper.updateEntity(toUpdate, dto);
        toUpdate.setLastTimeEdited(new Date());
        resolveSubjectAndUnit(toUpdate, dto.getSubjectId(), dto.getSubjectUnitId());

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

        return pdfMapper.toDto(updated);
    }

    @Override
    public void deletePDF(long pdfId) throws MathtexpediaNotFoundException {
        logger.info("Deleting PDF with id: {}", pdfId);

        PDF toDelete = findByIdOrThrow(pdfId);
        String key = toDelete.getS3Key();

        pdfDataService.deletePDF(toDelete);
        safeDelete(key);
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