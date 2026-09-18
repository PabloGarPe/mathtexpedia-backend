package mathtexpedia.es.api.service.pdf;

import mathtexpedia.es.api.domain.model.pdf.CreatePDFDto;
import mathtexpedia.es.api.domain.model.pdf.PDFDto;
import mathtexpedia.es.api.domain.model.pdf.PDFNoLinkDto;
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

    public PDF toEntity(CreatePDFDto dto) {
        PDF pdf = new PDF();
        pdf.setName(dto.getName());
        pdf.setLink(dto.getLink());
        pdf.setDescription(dto.getDescription());
        return pdf;
    }

    public void updateEntity(PDF target, UpdatePDFDto dto) {
        target.setName(dto.getName());
        target.setLink(dto.getLink());
        target.setDescription(dto.getDescription());
    }

    public PDFDto toDto(PDF pdf) {
        return new PDFDto(
                pdf.getId(),
                pdf.getName(),
                pdf.getLink(),
                pdf.getLastTimeEdited(),
                pdf.getDescription(),
                subjectMapper.toDto(pdf.getSubject()),
                pdf.getSubjectUnit() != null ? subjectUnitMapper.toDto(pdf.getSubjectUnit()) : null
        );
    }

    public PDFNoLinkDto toDtoWithoutLink(PDF pdf) {
        return new PDFNoLinkDto(
                pdf.getId(),
                pdf.getName(),
                pdf.getLastTimeEdited(),
                pdf.getDescription(),
                subjectMapper.toDto(pdf.getSubject()),
                pdf.getSubjectUnit() != null ? subjectUnitMapper.toDto(pdf.getSubjectUnit()) : null
        );
    }
}
