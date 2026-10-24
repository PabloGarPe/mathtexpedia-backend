package mathtexpedia.es.api.persistence.pdfCommentLike;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mathtexpedia.es.api.persistence.pdfComment.PdfComment;
import mathtexpedia.es.api.persistence.user.UserAccount;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

/**
 * "Me gusta" de un usuario a un comentario: uno por usuario y comentario, garantizado por la restricción única. El número de
 * likes se calcula contando estas filas, no se guarda en el comentario.
 */
@Getter
@Setter
@Entity
@Table(name = "pdf_comment_like",
        uniqueConstraints = @UniqueConstraint(name = "uk_pdf_comment_like", columnNames = {"comment_id", "user_id"}),
        indexes = @Index(name = "idx_pdf_comment_like_user", columnList = "user_id"))
public class PdfCommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "comment_id", nullable = false)
    private PdfComment comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount userAccount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
