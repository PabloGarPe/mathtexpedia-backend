package mathtexpedia.es.api.presentation.pdf;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import mathtexpedia.es.api.domain.model.pdf.PDFDto;
import mathtexpedia.es.api.infrastructure.application.PublicEndpoint;
import mathtexpedia.es.api.service.pdf.PDFService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "PDFs públicos", description = "Consulta del catálogo de PDFs sin necesidad de autenticación")
@RestController
@RequestMapping("pdf")
public class PublicPDFController implements PublicEndpoint {

    private final PDFService pdfService;

    public PublicPDFController(PDFService pdfService) {
        this.pdfService = pdfService;
    }

    @Operation(summary = "Lista el catálogo de PDFs",
            description = "Solo metadatos; el contenido se obtiene autenticado en GET /pdf/{pdfId}/content",
            security = { @SecurityRequirement })
    @GetMapping("no-link")
    public List<PDFDto> getPDFs() {
        return pdfService.getPDFs();
    }
}