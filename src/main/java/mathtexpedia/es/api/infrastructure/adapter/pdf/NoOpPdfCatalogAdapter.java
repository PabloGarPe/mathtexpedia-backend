package mathtexpedia.es.api.infrastructure.adapter.pdf;

import mathtexpedia.es.api.domain.model.pdf.PdfCatalogPortTemporal;
import mathtexpedia.es.api.domain.model.pdf.PdfSummaryTemporal;
import org.springframework.stereotype.Component;

import java.util.List;

//TODO: Eliminar esto cuando esté la implementación de verdad, es para poder hacer algo sobre le chat
@Component
public class NoOpPdfCatalogAdapter implements PdfCatalogPortTemporal {

    @Override
    public List<PdfSummaryTemporal> getAllPdfs() {
        return List.of();
    }

    @Override
    public List<PdfSummaryTemporal> getAllPdfsWithNoLink() {
        return List.of();
    }
}
