package mathtexpedia.es.api.domain.model.pdf;

import java.util.List;

//TODO: Eliminar esto cuando esté la implementación de verdad, es para poder hacer algo sobre le chat
public interface PdfCatalogPortTemporal {
    List<PdfSummaryTemporal> getAllPdfs();
    List<PdfSummaryTemporal> getAllPdfsWithNoLink();
}
