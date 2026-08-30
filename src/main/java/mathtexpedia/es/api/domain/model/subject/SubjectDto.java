package mathtexpedia.es.api.domain.model.subject;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubjectDto {
    private Long id;
    private String name;
    private String description;
}
