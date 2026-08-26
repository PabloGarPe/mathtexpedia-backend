package mathtexpedia.es.api.domain.port.pdf;

import mathtexpedia.es.api.domain.model.pdf.PDFSummary;

import java.util.List;

public interface PDFCatalogPort {
    List<PDFSummary> getAllPdfs();
    List<PDFSummary> getAllPdfsWithNoLink();
}
