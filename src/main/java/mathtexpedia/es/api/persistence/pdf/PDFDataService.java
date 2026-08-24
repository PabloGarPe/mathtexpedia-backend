package mathtexpedia.es.api.persistence.pdf;

import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.model.pdf.CreatePDFDto;

import java.util.List;

public interface PDFDataService {

    List<PDF> getPDFWithNoLink();

    List<PDF> getPDFs();

    PDF getPDF(String pdfName);

    PDF createPDF(PDF pdf) throws MathtexpediaInvalidException;
}
