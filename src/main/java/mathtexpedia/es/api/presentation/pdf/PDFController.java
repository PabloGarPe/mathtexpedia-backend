package mathtexpedia.es.api.presentation.pdf;

import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.model.pdf.CreatePDFDto;
import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.pdf.PDFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pdf")
public class PDFController extends GenericController {

    @Autowired
    private PDFService pdfService;

    @GetMapping
    public List<PDF> getPDFs() {
        return pdfService.getPDFs();
    }

    @GetMapping("/pdf")
    public PDF getPDF(@RequestParam String pdfName) throws MathtexpediaInvalidException {
        return pdfService.getPDF(pdfName);
    }

    @PostMapping("/create")
    public ResponseEntity<PDF> createPDF(@RequestBody CreatePDFDto pdf) throws MathtexpediaInvalidException {
        logger.debug("Called recieve to create PDF {}",pdf);

        PDF created = pdfService.createPDF(pdf);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);

    }

}
