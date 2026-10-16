package mathtexpedia.es.api.persistence.degreeSubject;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mathtexpedia.es.api.persistence.degree.Degree;
import mathtexpedia.es.api.persistence.subject.Subject;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Colocación de una asignatura en un grado, con su año y su cuatrimestre. Una asignatura puede estar en varios grados (o en
 * ninguno), pero solo una vez en cada uno. Al borrar la asignatura se borran sus colocaciones en la propia base de datos
 * ({@code ON DELETE CASCADE}). Con el grado no hay cascada: el servicio devuelve 409 si aún tiene colocaciones.
 */
@Getter
@Setter
@Entity
@Table(name = "degree_subject",
        uniqueConstraints = @UniqueConstraint(name = "uk_degree_subject", columnNames = {"degree_id", "subject_id"}),
        indexes = {
                @Index(name = "idx_degree_subject_subject", columnList = "subject_id"),
                @Index(name = "idx_degree_subject_degree_year", columnList = "degree_id, course_year")
        })
public class DegreeSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "degree_id", nullable = false)
    private Degree degree;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    /** Año del grado en el que se cursa (de 1 a {@code degree.years}). "year" se evita como nombre de columna. */
    @Column(name = "course_year", nullable = false)
    private int year;

    /** 1, 2 o nulo (asignatura anual o sin especificar). */
    @Column
    private Integer semester;
}
