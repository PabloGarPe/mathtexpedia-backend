package mathtexpedia.es.api.persistence.degree;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Grado (o doble grado, PCEO...) del catálogo. Un año del grado no es una entidad, sino un entero entre {@code 1} y
 * {@code years}. La unicidad de (nombre, universidad) se comprueba en el servicio, porque con {@code university} nulo
 * una constraint de la base de datos no la haría cumplir.
 */
@Getter
@Setter
@Entity
@Table(name = "degree")
public class Degree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 150)
    private String university;

    @Column(nullable = false)
    private int years;
}
