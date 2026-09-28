package mathtexpedia.es.api.domain.model.pdf;

import java.io.InputStream;


public record PDFContent(InputStream stream, long length) {
}