package mathtexpedia.es.api.service.pdf;

import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.model.pdf.CreatePDFDto;
import mathtexpedia.es.api.persistence.pdf.PDF;

import java.util.List;

public interface PDFService {

    List<PDF> getPDFWithoutLink();

    List<PDF> getPDFs();

    PDF getPDF(String pdfName) throws MathtexpediaInvalidException;

    PDF createPDF(CreatePDFDto dto) throws MathtexpediaInvalidException;

    void deletePDF(String pdfName) throws MathtexpediaInvalidException;

    PDF updatePDF(PDF pdf,  long pdfId) throws MathtexpediaInvalidException;
}
