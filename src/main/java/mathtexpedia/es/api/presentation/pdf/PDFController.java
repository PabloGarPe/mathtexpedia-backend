package mathtexpedia.es.api.presentation.pdf;

import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.pdf.CreatePDFDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.pdf.PDFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<PDF> createPDF(@RequestBody CreatePDFDto pdf,
                                         @AuthenticationPrincipal UserProfile user) throws MathtexpediaInvalidException, MathtexpediaUnauthorizedException {
        logger.debug("Called recieve to create PDF {}",pdf);

        checkIfAdmin(user);

        PDF created = pdfService.createPDF(pdf);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);

    }

    @DeleteMapping("/delete")
    public ResponseEntity<PDF> deletePDF(@RequestParam String pdfName,
                                         @AuthenticationPrincipal UserProfile user) throws MathtexpediaInvalidException, MathtexpediaUnauthorizedException {
        logger.debug("Called recieve to delete PDF {}",pdfName);

        checkIfAdmin(user);

        pdfService.deletePDF(pdfName);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

    @PatchMapping("/update")
    public ResponseEntity<PDF> updatePDF(@RequestBody PDF pdf, @RequestParam long pdfId,
                                         @AuthenticationPrincipal UserProfile user) throws MathtexpediaInvalidException, MathtexpediaUnauthorizedException {
        logger.debug("Called recieve to update PDF {}",pdf);

        checkIfAdmin(user);

        pdf = pdfService.updatePDF(pdf, pdfId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(pdf);
    }

}
