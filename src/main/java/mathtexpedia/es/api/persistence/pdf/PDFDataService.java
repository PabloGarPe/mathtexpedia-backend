package mathtexpedia.es.api.persistence.pdf;

import java.util.List;

public interface PDFDataService {

    List<PDF> getPDFWithNoLink();

    List<PDF> getPDFs();

    PDF getPDF(String pdfName);
}
