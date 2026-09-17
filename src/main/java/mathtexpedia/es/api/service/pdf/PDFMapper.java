package mathtexpedia.es.api.service.pdf;

import mathtexpedia.es.api.domain.model.pdf.CreatePDFDto;
import mathtexpedia.es.api.persistence.pdf.PDF;
import org.springframework.stereotype.Component;

@Component
public class PDFMapper {

    public PDF toEntity(CreatePDFDto dto) {
        PDF pdf = new PDF();

        if (dto.getDescription() != null && !dto.getDescription().isEmpty()) {
            pdf.setDescription(dto.getDescription());
        }

        pdf.setName(dto.getName());
        pdf.setLink(dto.getLink());
        pdf.setTag(dto.getPdfTag());

        return pdf;
    }
}