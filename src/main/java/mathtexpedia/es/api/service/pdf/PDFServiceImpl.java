package mathtexpedia.es.api.service.pdf;

import jakarta.persistence.PersistenceException;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.pdf.CreatePDFDto;
import mathtexpedia.es.api.domain.model.pdf.PDFDto;
import mathtexpedia.es.api.domain.model.pdf.PDFNoLinkDto;
import mathtexpedia.es.api.domain.model.pdf.UpdatePDFDto;
import mathtexpedia.es.api.domain.model.subject.SubjectDto;
import mathtexpedia.es.api.domain.model.subjectUnit.SubjectUnitDto;
import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.persistence.pdf.PDFDataService;
import mathtexpedia.es.api.persistence.subject.Subject;
import mathtexpedia.es.api.persistence.subject.SubjectDataService;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnit;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@Service
public class PDFServiceImpl implements PDFService {

    private static final Logger logger = LoggerFactory.getLogger(PDFServiceImpl.class);

    private final PDFDataService pdfDataService;
    private final SubjectUnitDataService subjectUnitDataService;
    private final SubjectDataService subjectDataService;

    public PDFServiceImpl(
            PDFDataService pdfDataService,
            SubjectUnitDataService subjectUnitDataService,
            SubjectDataService subjectDataService
    ) {
        this.pdfDataService = pdfDataService;
        this.subjectUnitDataService = subjectUnitDataService;
        this.subjectDataService = subjectDataService;
    }

    @Override
    public List<PDFNoLinkDto> getPDFsWithoutLink() {
        logger.info("Fetching all PDFs without link");

        return pdfDataService.getAll()
                .stream()
                .map(this::toDtoWithoutLink)
                .toList();
    }

    @Override
    public List<PDFDto> getPDFs() {
        logger.info("Fetching all PDFs");

        return pdfDataService.getAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Optional<PDFDto> getPDF(String pdfName) {
        logger.info("Fetching PDF with name: {}", pdfName);

        Optional<PDF> pdf = pdfDataService.getPDF(pdfName);
        return pdf.map(this::toDto);
    }

    @Override
    public List<PDFDto> getPDFsBySubjectUnit(long subjectUnitId) throws MathtexpediaNotFoundException {
        logger.info("Fetching PDFs for subject unit with id: {}", subjectUnitId);

        if (subjectUnitDataService.getById(subjectUnitId).isEmpty())
            throw new MathtexpediaNotFoundException("Subject unit not found with id: " + subjectUnitId);

        return pdfDataService.getAllForSubjectUnit(subjectUnitId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<PDFDto> getPDFsBySubject(long subjectId) throws MathtexpediaNotFoundException {
        logger.info("Fetching PDFs for subject with id: {}", subjectId);

        if (subjectDataService.getById(subjectId).isEmpty())
            throw new MathtexpediaNotFoundException("Subject not found with id: " + subjectId);

        return pdfDataService.getAllForSubject(subjectId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public PDFDto createPDF(CreatePDFDto dto) throws MathtexpediaConflictException, MathtexpediaNotFoundException, MathtexpediaInvalidException {
        logger.info("Creating new PDF with name: {}", dto.getName());

        PDF pdf = new PDF();
        pdf.setName(dto.getName());
        pdf.setLink(dto.getLink());
        pdf.setDescription(dto.getDescription());
        pdf.setLastTimeEdited(new Date());

        resolveSubjectAndUnit(pdf, dto.getSubjectId(), dto.getSubjectUnitId());

        try {
            PDF created = pdfDataService.createPDF(pdf);
            return toDto(created);
        } catch (PersistenceException e) {
            throw new MathtexpediaConflictException("Error creating PDF: " + e.getMessage(), e);
        }
    }

    @Override
    public void deletePDF(String pdfName) throws MathtexpediaNotFoundException {
        logger.info("Deleting PDF with name: {}", pdfName);

        PDF toDelete = pdfDataService.getPDF(pdfName)
                .orElseThrow(() -> new MathtexpediaNotFoundException("PDF not found with name: " + pdfName));

        pdfDataService.deletePDF(toDelete);
    }

    @Override
    public PDFDto updatePDF(long pdfId, UpdatePDFDto pdf) throws MathtexpediaNotFoundException, MathtexpediaConflictException, MathtexpediaInvalidException {
        logger.info("Updating PDF with id: {}", pdfId);

        PDF toUpdate = pdfDataService.getPDFById(pdfId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("PDF not found with id: " + pdfId));

        toUpdate.setName(pdf.getName());
        toUpdate.setDescription(pdf.getDescription());
        toUpdate.setLink(pdf.getLink());
        toUpdate.setLastTimeEdited(new Date());

        resolveSubjectAndUnit(toUpdate, pdf.getSubjectId(), pdf.getSubjectUnitId());

        try {
            PDF updated = pdfDataService.updatePDF(toUpdate);
            return toDto(updated);
        } catch (PersistenceException e) {
            throw new MathtexpediaConflictException("Error updating PDF: " + e.getMessage(), e);
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


    private PDFDto toDto(PDF pdf) {
        return new PDFDto(
                pdf.getId(),
                pdf.getName(),
                pdf.getLink(),
                pdf.getLastTimeEdited(),
                pdf.getDescription(),
                new SubjectDto(
                        pdf.getSubject().getId(),
                        pdf.getSubject().getName(),
                        pdf.getSubject().getDescription()
                ),
                pdf.getSubjectUnit() != null ? new SubjectUnitDto(
                        pdf.getSubjectUnit().getId(),
                        pdf.getSubjectUnit().getName(),
                        pdf.getSubjectUnit().getPosition()
                ) : null
        );
    }

    private PDFNoLinkDto toDtoWithoutLink(PDF pdf) {
        return new PDFNoLinkDto(
                pdf.getId(),
                pdf.getName(),
                pdf.getLastTimeEdited(),
                pdf.getDescription(),
                new SubjectDto(
                        pdf.getSubject().getId(),
                        pdf.getSubject().getName(),
                        pdf.getSubject().getDescription()
                ),
                pdf.getSubjectUnit() != null ? new SubjectUnitDto(
                        pdf.getSubjectUnit().getId(),
                        pdf.getSubjectUnit().getName(),
                        pdf.getSubjectUnit().getPosition()
                ) : null
        );
    }
}
