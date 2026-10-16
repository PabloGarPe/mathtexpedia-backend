package mathtexpedia.es.api.service.pdf;

import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.pdf.CreatePDFDto;
import mathtexpedia.es.api.domain.model.pdf.PDFContent;
import mathtexpedia.es.api.domain.model.pdf.PDFDto;
import mathtexpedia.es.api.domain.model.pdf.PdfSort;
import mathtexpedia.es.api.domain.model.pdf.UpdatePDFDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface PDFService {

    /** Todo el catálogo. Con {@code sort} nulo, en el orden por defecto. */
    List<PDFDto> getPDFs(PdfSort sort);

    PDFContent getPDFContent(long pdfId, UserProfile user) throws MathtexpediaNotFoundException;

    List<PDFDto> getRecentPDFs(Integer limit);

    /** Los PDFs más recientes de las asignaturas a las que está suscrito el usuario (vacío si no tiene suscripciones). */
    List<PDFDto> getRecentPDFsForSubscriber(UserProfile user, Integer limit) throws MathtexpediaUnauthorizedException;

    List<PDFDto> getTopViewedPDFs(Integer limit);

    List<PDFDto> getTopViewedPDFsForUser(UserProfile user, Integer limit) throws MathtexpediaUnauthorizedException;

    /** Los mejor valorados: al menos 3 votos y más 👍 que 👎, por puntuación de mayor a menor. Vacío si ninguno cumple. */
    List<PDFDto> getTopRatedPDFs(Integer limit);

    List<PDFDto> getPDFsBySubjectUnit(long subjectUnitId) throws MathtexpediaNotFoundException;

    /** PDFs de la asignatura. Con {@code sort} nulo, en el orden por defecto. */
    List<PDFDto> getPDFsBySubject(long subjectId, PdfSort sort) throws MathtexpediaNotFoundException;

    PDFDto createPDF(CreatePDFDto dto, MultipartFile file)
            throws MathtexpediaConflictException, MathtexpediaNotFoundException, MathtexpediaInvalidException;

    PDFDto updatePDF(long pdfId, UpdatePDFDto dto, MultipartFile file)
            throws MathtexpediaNotFoundException, MathtexpediaConflictException, MathtexpediaInvalidException;

    void deletePDF(long pdfId) throws MathtexpediaNotFoundException;
}