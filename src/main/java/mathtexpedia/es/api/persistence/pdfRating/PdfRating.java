package mathtexpedia.es.api.persistence.pdfRating;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mathtexpedia.es.api.domain.model.pdfRating.PdfVote;
import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.persistence.user.UserAccount;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

/**
 * Voto (👍/👎) de un usuario a un PDF: uno por usuario y PDF. Al borrar el PDF se borran sus votos en la propia base de datos
 * ({@code ON DELETE CASCADE}), así que no bloquean su borrado.
 */
@Getter
@Setter
@Entity
@Table(name = "pdf_rating",
        uniqueConstraints = @UniqueConstraint(name = "uk_pdf_rating", columnNames = {"pdf_id", "user_id"}),
        indexes = @Index(name = "idx_pdf_rating_user", columnList = "user_id"))
public class PdfRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "pdf_id", nullable = false)
    private PDF pdf;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount userAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PdfVote vote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
