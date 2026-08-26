package mathtexpedia.es.api.infrastructure.adapter.pdf;

import mathtexpedia.es.api.domain.model.pdf.PDFSummary;
import mathtexpedia.es.api.domain.port.pdf.PDFCatalogPort;
import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.persistence.pdf.PDFDataService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PDFCatalogAdapter implements PDFCatalogPort {

    private final PDFDataService pdfDataService;

    public PDFCatalogAdapter(PDFDataService pdfDataService) {
        this.pdfDataService = pdfDataService;
    }

    @Override
    public List<PDFSummary> getAllPdfs() {
        return pdfDataService.getPDFs().stream().map(this::toSummary).toList();
    }

    @Override
    public List<PDFSummary> getAllPdfsWithNoLink() {
        return pdfDataService.getPDFWithNoLink().stream().map(this::toSummary).toList();
    }

    private PDFSummary toSummary(PDF pdf) {
        return new PDFSummary(pdf.getName(), pdf.getTag() != null ? pdf.getTag().getName() : null, pdf.getLink());
    }
}
