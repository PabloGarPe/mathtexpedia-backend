package mathtexpedia.es.api.domain.model.subjectUnit;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubjectUnitDto {

    private Long id;
    private String name;
    private int position;
}
