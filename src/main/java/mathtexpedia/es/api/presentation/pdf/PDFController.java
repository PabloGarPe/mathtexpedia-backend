package mathtexpedia.es.api.presentation.pdf;

import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.pdf.PDFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pdf")
public class PDFController extends GenericController {

    @Autowired
    private PDFService pdfService;

    @GetMapping
    public List<PDF> getPDFs() {
        return pdfService.getPDF();
    }

}
