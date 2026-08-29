package mathtexpedia.es.api.presentation.pdf;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import mathtexpedia.es.api.infrastructure.application.PublicEndpoint;
import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.service.pdf.PDFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "PDFs públicos", description = "Consulta de PDFs sin necesidad de autenticación")
@RestController
@RequestMapping("pdf")
public class PublicPDFController implements PublicEndpoint {

    @Autowired
    private PDFService pdfService;

    @Operation(summary = "Lista los PDFs sin enlace de descarga",
            description = "Pensado para mostrar el catálogo público sin exponer el link real del archivo",
            security = { @SecurityRequirement })
    @GetMapping("no-link")
    public List<PDF> getPDFWithoutLink() {
        return pdfService.getPDFWithoutLink();
    }
}
