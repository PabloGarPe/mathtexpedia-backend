package mathtexpedia.es.api.service.pdf;

import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.persistence.pdf.PDFDataService;
import mathtexpedia.es.api.presentation.GenericController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PDFServiceImpl extends GenericController implements PDFService {

    @Autowired
    private PDFDataService pdfDataService;

    @Override
    public List<PDF>  getPDFWithoutLink() {
        return pdfDataService.getPDFWithNoLink();
    }

    @Override
    public List<PDF> getPDFs() {
        return pdfDataService.getPDFs();
    }

    @Override
    public PDF getPDF(String pdfName) throws MathtexpediaInvalidException {
        if(pdfName.isBlank())
            throw new MathtexpediaInvalidException("Pdf name cannot be empty or blank");

        return pdfDataService.getPDF(pdfName);
    }
}
