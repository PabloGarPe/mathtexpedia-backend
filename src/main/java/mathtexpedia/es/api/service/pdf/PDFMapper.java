package mathtexpedia.es.api.service.pdf;

import mathtexpedia.es.api.domain.model.pdf.CreatePDFDto;
import mathtexpedia.es.api.domain.model.pdf.PDFDto;
import mathtexpedia.es.api.domain.model.pdf.UpdatePDFDto;
import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.service.subject.SubjectMapper;
import mathtexpedia.es.api.service.subjectUnit.SubjectUnitMapper;
import org.springframework.stereotype.Component;

@Component
public class PDFMapper {

    private final SubjectMapper subjectMapper;
    private final SubjectUnitMapper subjectUnitMapper;

    public PDFMapper(SubjectMapper subjectMapper, SubjectUnitMapper subjectUnitMapper) {
        this.subjectMapper = subjectMapper;
        this.subjectUnitMapper = subjectUnitMapper;
    }

    /** Asignatura, unidad, clave S3 y fecha las rellena el servicio. */
    public PDF toEntity(CreatePDFDto dto) {
        PDF pdf = new PDF();
        pdf.setName(dto.getName());
        pdf.setDescription(dto.getDescription());
        return pdf;
    }

    /** Asignatura y unidad las actualiza el servicio tras validarlas. */
    public void updateEntity(PDF target, UpdatePDFDto dto) {
        target.setName(dto.getName());
        target.setDescription(dto.getDescription());
    }

    public PDFDto toDto(PDF pdf) {
        return new PDFDto(
                pdf.getId(),
                pdf.getName(),
                pdf.getLastTimeEdited(),
                pdf.getDescription(),
                subjectMapper.toDto(pdf.getSubject()),
                pdf.getSubjectUnit() != null ? subjectUnitMapper.toDto(pdf.getSubjectUnit()) : null
        );
    }
}