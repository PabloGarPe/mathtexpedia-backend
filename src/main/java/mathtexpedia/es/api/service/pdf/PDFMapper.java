package mathtexpedia.es.api.service.pdf;

import mathtexpedia.es.api.domain.model.pdf.CreatePDFDto;
import mathtexpedia.es.api.domain.model.pdf.PDFDto;
import mathtexpedia.es.api.domain.model.pdfRating.PdfRatingSummaryDto;
import mathtexpedia.es.api.domain.model.pdf.UpdatePDFDto;
import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.persistence.user.UserAccount;
import mathtexpedia.es.api.service.subject.SubjectMapper;
import mathtexpedia.es.api.service.subjectUnit.SubjectUnitMapper;
import mathtexpedia.es.api.service.userAccount.UserAccountMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PDFMapper {

    private final SubjectMapper subjectMapper;
    private final SubjectUnitMapper subjectUnitMapper;
    private final UserAccountMapper userAccountMapper;

    public PDFMapper(SubjectMapper subjectMapper, SubjectUnitMapper subjectUnitMapper, UserAccountMapper userAccountMapper) {
        this.subjectMapper = subjectMapper;
        this.subjectUnitMapper = subjectUnitMapper;
        this.userAccountMapper = userAccountMapper;
    }

    /** Asignatura, unidad, autores, clave S3 y fecha las rellena el servicio. */
    public PDF toEntity(CreatePDFDto dto) {
        PDF pdf = new PDF();
        pdf.setName(dto.getName());
        pdf.setDescription(dto.getDescription());
        return pdf;
    }

    /** Asignatura, unidad y autores los actualiza el servicio tras validarlos. */
    public void updateEntity(PDF target, UpdatePDFDto dto) {
        target.setName(dto.getName());
        target.setDescription(dto.getDescription());
    }

    public PDFDto toDto(PDF pdf, long viewCount, PdfRatingSummaryDto rating) {
        List<UserAccount> authors = pdf.getAuthors();
        List<String> authorNames = authors.stream().map(UserAccount::getUsername).toList();

        return new PDFDto(
                pdf.getId(),
                pdf.getName(),
                pdf.getLastTimeEdited(),
                pdf.getDescription(),
                subjectMapper.toDto(pdf.getSubject()),
                pdf.getSubjectUnit() != null ? subjectUnitMapper.toDto(pdf.getSubjectUnit()) : null,
                authorNames.isEmpty() ? null : authorNames.get(0),
                authorNames.size() > 1 ? authorNames.subList(1, authorNames.size()) : List.of(),
                authors.stream().map(userAccountMapper::toAuthorDto).toList(),
                viewCount,
                rating
        );
    }
}