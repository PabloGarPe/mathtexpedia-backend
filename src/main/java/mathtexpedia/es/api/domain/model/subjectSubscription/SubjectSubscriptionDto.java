package mathtexpedia.es.api.domain.model.subjectSubscription;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import mathtexpedia.es.api.domain.model.subject.SubjectDto;

import java.time.Instant;

@Data
@AllArgsConstructor
public class SubjectSubscriptionDto {

    @Schema(description = "Asignatura a la que está suscrito el usuario")
    private SubjectDto subject;

    @Schema(description = "Cuándo se suscribió")
    private Instant subscribedAt;
}
