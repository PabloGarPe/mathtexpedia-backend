package mathtexpedia.es.api.domain.model.pdf;

import lombok.AllArgsConstructor;
import lombok.Data;
import mathtexpedia.es.api.domain.model.subjectUnit.SubjectUnitDto;

import java.util.Date;

@Data
@AllArgsConstructor
public class PDFNoLinkDto {
    private Long id;
    private String name;
    private Date lastTimeEdited;
    private String description;
    private SubjectUnitDto subjectUnit;
}
