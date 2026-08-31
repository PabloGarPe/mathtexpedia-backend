package mathtexpedia.es.api.persistence.pdf;

import java.util.List;
import java.util.Optional;

public interface PDFDataService {

    List<PDF> getAll();

    Optional<PDF> getPDF(String pdfName);

    List<PDF> getAllForSubjectUnit(long subjectUnitId);

    PDF createPDF(PDF pdf);

    void deletePDF(PDF pdf);

    PDF updatePDF(PDF pdf);

    Optional<PDF> getPDFById(long pdfId);
}
