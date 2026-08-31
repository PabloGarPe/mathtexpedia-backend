package mathtexpedia.es.api.service.pdf;

import jakarta.persistence.PersistenceException;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.pdf.CreatePDFDto;
import mathtexpedia.es.api.domain.model.pdf.PDFDto;
import mathtexpedia.es.api.domain.model.pdf.PDFNoLinkDto;
import mathtexpedia.es.api.domain.model.pdf.UpdatePDFDto;
import mathtexpedia.es.api.domain.model.subjectUnit.SubjectUnitDto;
import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.persistence.pdf.PDFDataService;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnit;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PDFServiceImpl implements PDFService {

    private static final Logger logger = LoggerFactory.getLogger(PDFServiceImpl.class);

    private final PDFDataService pdfDataService;
    private final SubjectUnitDataService subjectUnitDataService;

    public PDFServiceImpl(PDFDataService pdfDataService, SubjectUnitDataService subjectUnitDataService) {
        this.pdfDataService = pdfDataService;
        this.subjectUnitDataService = subjectUnitDataService;
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
    public List<PDFDto> getPDFsBySubjectUnit(long subjectUnitId) {
        logger.info("Fetching PDFs for subject unit with id: {}", subjectUnitId);

        return pdfDataService.getAllForSubjectUnit(subjectUnitId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public PDFDto createPDF(CreatePDFDto dto) throws MathtexpediaConflictException, MathtexpediaNotFoundException {
        logger.info("Creating new PDF with name: {}", dto.getName());

        PDF pdf = new PDF();
        pdf.setName(dto.getName());
        pdf.setLink(dto.getLink());
        pdf.setDescription(dto.getDescription());
        pdf.setLastTimeEdited(new Date());

        Optional<SubjectUnit> subjectUnit = subjectUnitDataService.getById(dto.getSubjectUnitId());
        if (subjectUnit.isEmpty()) {
            throw new MathtexpediaNotFoundException("Subject unit not found with id: " + dto.getSubjectUnitId());
        }

        pdf.setSubjectUnit(subjectUnit.get());
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
    public PDFDto updatePDF(long pdfId, UpdatePDFDto pdf) throws MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Updating PDF with id: {}", pdfId);

        PDF toUpdate = pdfDataService.getPDFById(pdfId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("PDF not found with id: " + pdfId));
        toUpdate.setName(pdf.getName());
        toUpdate.setDescription(pdf.getDescription());
        toUpdate.setLink(pdf.getLink());
        toUpdate.setLastTimeEdited(new Date());

        Optional<SubjectUnit> subjectUnit = subjectUnitDataService.getById(pdf.getSubjectUnitId());
        if (subjectUnit.isPresent()) {
            toUpdate.setSubjectUnit(subjectUnit.get());
        } else {
            throw new MathtexpediaNotFoundException("Subject unit not found with id: " + pdf.getSubjectUnitId());
        }

        try {
            PDF updated = pdfDataService.updatePDF(toUpdate);
            return toDto(updated);
        } catch (PersistenceException e) {
            throw new MathtexpediaConflictException("Error updating PDF: " + e.getMessage(), e);
        }
    }

    private PDFDto toDto(PDF pdf) {
        return new PDFDto(
                pdf.getId(),
                pdf.getName(),
                pdf.getLink(),
                pdf.getLastTimeEdited(),
                pdf.getDescription(),
                new SubjectUnitDto(
                        pdf.getSubjectUnit().getId(),
                        pdf.getSubjectUnit().getName(),
                        pdf.getSubjectUnit().getPosition()
                ));
    }

    private PDFNoLinkDto toDtoWithoutLink(PDF pdf) {
        return new PDFNoLinkDto(
                pdf.getId(),
                pdf.getName(),
                pdf.getLastTimeEdited(),
                pdf.getDescription(),
                new SubjectUnitDto(
                        pdf.getSubjectUnit().getId(),
                        pdf.getSubjectUnit().getName(),
                        pdf.getSubjectUnit().getPosition()
                ));
    }
}
