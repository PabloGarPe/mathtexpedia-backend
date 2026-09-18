package mathtexpedia.es.api.domain.model.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import mathtexpedia.es.api.domain.model.subject.SubjectDto;
import mathtexpedia.es.api.domain.model.subjectUnit.SubjectUnitDto;

import java.util.Date;

@Data
@AllArgsConstructor
public class QuizDto {

    private Long id;
    private String name;
    private String description;
    private Difficulty difficulty;
    private Date lastTimeEdited;
    private SubjectDto subject;
    private SubjectUnitDto subjectUnit;
}
