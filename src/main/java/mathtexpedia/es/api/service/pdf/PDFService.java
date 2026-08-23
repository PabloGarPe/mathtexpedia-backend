package mathtexpedia.es.api.service.pdf;

import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.persistence.pdf.PDF;

import java.util.List;

public interface PDFService {

    List<PDF> getPDFWithoutLink();

    List<PDF> getPDFs();

    PDF getPDF(String pdfName) throws MathtexpediaInvalidException;
}
