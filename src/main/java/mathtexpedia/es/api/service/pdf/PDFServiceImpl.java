package mathtexpedia.es.api.service.pdf;

import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.model.pdf.CreatePDFDto;
import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.persistence.pdf.PDFDataService;
import mathtexpedia.es.api.presentation.GenericController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
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

    @Override
    public PDF createPDF(CreatePDFDto dto) throws MathtexpediaInvalidException {
        PDF pdf = new PDF();
        if(dto.getDescription() != null && !dto.getDescription().isEmpty())
            pdf.setDescription(dto.getDescription());
        pdf.setName(dto.getName());
        pdf.setLink(dto.getLink());
        pdf.setTag(dto.getPdfTag());
        pdf.setLastTimeEdited(new Date());
        return pdfDataService.createPDF(pdf);

    }

    @Override
    public void deletePDF(String pdfName) throws MathtexpediaInvalidException {
        try {
            pdfDataService.deletePDF(pdfName);
        } catch (Exception e) {
            throw new MathtexpediaInvalidException("An error occurred while deleting PDF");
        }
    }
}
