package mathtexpedia.es.api.service.pdf;

import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.pdf.CreatePDFDto;
import mathtexpedia.es.api.domain.model.pdf.PDFContent;
import mathtexpedia.es.api.domain.model.pdf.PDFDto;
import mathtexpedia.es.api.domain.model.pdf.UpdatePDFDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface PDFService {

    List<PDFDto> getPDFs();

    PDFContent getPDFContent(long pdfId, UserProfile user) throws MathtexpediaNotFoundException;

    List<PDFDto> getRecentPDFs(Integer limit);

    /** Los PDFs más recientes de las asignaturas a las que está suscrito el usuario (vacío si no tiene suscripciones). */
    List<PDFDto> getRecentPDFsForSubscriber(UserProfile user, Integer limit) throws MathtexpediaUnauthorizedException;

    List<PDFDto> getTopViewedPDFs(Integer limit);

    List<PDFDto> getTopViewedPDFsForUser(UserProfile user, Integer limit) throws MathtexpediaUnauthorizedException;

    List<PDFDto> getPDFsBySubjectUnit(long subjectUnitId) throws MathtexpediaNotFoundException;

    List<PDFDto> getPDFsBySubject(long subjectId) throws MathtexpediaNotFoundException;

    PDFDto createPDF(CreatePDFDto dto, MultipartFile file)
            throws MathtexpediaConflictException, MathtexpediaNotFoundException, MathtexpediaInvalidException;

    PDFDto updatePDF(long pdfId, UpdatePDFDto dto, MultipartFile file)
            throws MathtexpediaNotFoundException, MathtexpediaConflictException, MathtexpediaInvalidException;

    void deletePDF(long pdfId) throws MathtexpediaNotFoundException;
}