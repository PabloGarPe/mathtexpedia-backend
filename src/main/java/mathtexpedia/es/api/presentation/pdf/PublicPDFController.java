package mathtexpedia.es.api.presentation.pdf;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import mathtexpedia.es.api.domain.model.pdf.PDFDto;
import mathtexpedia.es.api.domain.model.pdf.PdfSort;
import mathtexpedia.es.api.infrastructure.application.PublicEndpoint;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.pdf.PDFService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "PDFs públicos", description = "Consulta del catálogo de PDFs sin necesidad de autenticación")
@RestController
@RequestMapping("pdf")
public class PublicPDFController extends GenericController implements PublicEndpoint {

    private final PDFService pdfService;

    public PublicPDFController(PDFService pdfService) {
        this.pdfService = pdfService;
    }

    @Operation(summary = "Lista el catálogo de PDFs",
            description = "Solo metadatos (incluidos el nombre del autor, los de los coautores y el número de vistas); "
                    + "el contenido se obtiene autenticado en GET /pdf/{pdfId}/content. Cada PDF incluye el resumen público de "
                    + "valoraciones ('rating'). Sin 'sort' se devuelve en el orden por defecto",
            security = { @SecurityRequirement })
    @GetMapping("no-link")
    public List<PDFDto> getPDFs(
            @Parameter(description = "Orden del listado: RATING (mejor valorados primero), VIEWS (más vistos), "
                    + "RECENT (editados más recientemente) o NAME (alfabético sin distinguir mayúsculas ni acentos). "
                    + "Si se omite, orden por defecto")
            @RequestParam(required = false) PdfSort sort) {
        return pdfService.getPDFs(sort);
    }

    @Operation(summary = "PDFs nuevos o actualizados recientemente",
            description = "Del más reciente al más antiguo según la fecha de última edición. Pensado para la hilera 'Novedades' del feed",
            security = { @SecurityRequirement })
    @GetMapping("recent")
    public List<PDFDto> getRecentPDFs(
            @Parameter(description = "Máximo de PDFs a devolver (por defecto 10, máximo 20)")
            @RequestParam(required = false) Integer limit) {
        return pdfService.getRecentPDFs(limit);
    }

    @Operation(summary = "PDFs más vistos en general",
            description = "Ordenados de más a menos vistas contando todas las aperturas de todos los usuarios. "
                    + "Solo incluye PDFs con al menos una vista. Pensado para la hilera 'Lo más visto' del feed",
            security = { @SecurityRequirement })
    @GetMapping("top")
    public List<PDFDto> getTopViewedPDFs(
            @Parameter(description = "Máximo de PDFs a devolver (por defecto 10, máximo 20)")
            @RequestParam(required = false) Integer limit) {
        return pdfService.getTopViewedPDFs(limit);
    }

    @Operation(summary = "PDFs mejor valorados",
            description = "Solo PDFs con al menos 3 votos y más 👍 que 👎, ordenados por puntuación ('rating.score', límite inferior de Wilson) "
                    + "de mayor a menor. Pensado para la hilera 'Mejor valorados' del feed; lista vacía si ninguno cumple",
            security = { @SecurityRequirement })
    @GetMapping("top-rated")
    public List<PDFDto> getTopRatedPDFs(
            @Parameter(description = "Máximo de PDFs a devolver (por defecto 10, máximo 20)")
            @RequestParam(required = false) Integer limit) {
        return pdfService.getTopRatedPDFs(limit);
    }
}