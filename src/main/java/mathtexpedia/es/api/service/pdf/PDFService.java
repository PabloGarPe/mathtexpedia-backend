package mathtexpedia.es.api.service.pdf;

import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.pdf.CreatePDFDto;
import mathtexpedia.es.api.domain.model.pdf.PDFDto;
import mathtexpedia.es.api.domain.model.pdf.PDFNoLinkDto;
import mathtexpedia.es.api.domain.model.pdf.UpdatePDFDto;

import java.util.List;
import java.util.Optional;

public interface PDFService {

    List<PDFDto> getPDFs();

    List<PDFNoLinkDto> getPDFsWithoutLink();

    Optional<PDFDto> getPDF(String pdfName);

    List<PDFDto> getPDFsBySubjectUnit(long subjectUnitId);

    PDFDto createPDF(CreatePDFDto dto) throws MathtexpediaConflictException, MathtexpediaNotFoundException;

    void deletePDF(String pdfName) throws MathtexpediaNotFoundException;

    PDFDto updatePDF(long pdfId, UpdatePDFDto dto) throws MathtexpediaNotFoundException, MathtexpediaConflictException;
}
