package mathtexpedia.es.api.domain.model.subjectSubscription;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import mathtexpedia.es.api.domain.model.degree.DegreeDto;

import java.time.Instant;

@Data
@AllArgsConstructor
public class CourseSubscriptionDto {

    @Schema(description = "Grado del curso")
    private DegreeDto degree;

    @Schema(description = "Año (curso) del grado, de 1 a sus años", example = "2")
    private int year;

    @Schema(description = "Cuatrimestre (1 o 2); nulo si está suscrito al año entero (cualquier cuatrimestre y las anuales)", nullable = true)
    private Integer semester;

    @Schema(description = "Cuándo se suscribió")
    private Instant subscribedAt;
}
