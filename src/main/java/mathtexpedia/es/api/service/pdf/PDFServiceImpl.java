package mathtexpedia.es.api.service.pdf;

import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.persistence.pdf.PDFDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PDFServiceImpl implements PDFService {

    @Autowired
    private PDFDataService pdfDataService;

    @Override
    public List<PDF>  getPDFWithoutLink() {
        return pdfDataService.getPDFWithNoLink();
    }
}
