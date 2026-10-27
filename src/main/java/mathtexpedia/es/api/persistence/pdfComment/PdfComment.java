package mathtexpedia.es.api.persistence.pdfComment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mathtexpedia.es.api.domain.model.pdfComment.CommentStatus;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationCategory;
import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.persistence.user.UserAccount;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "pdf_comment", indexes = {
        @Index(name = "idx_pdf_comment_pdf_status_published", columnList = "pdf_id, status, published_at"),
        @Index(name = "idx_pdf_comment_parent_status_published", columnList = "parent_id, status, published_at"),
        @Index(name = "idx_pdf_comment_status_created", columnList = "status, created_at"),
        @Index(name = "idx_pdf_comment_user_status", columnList = "user_id, status")
})
public class PdfComment {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PdfComment parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private PdfComment replyTo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CommentStatus status = CommentStatus.PENDING;

    /** Solo con {@code REJECTED}, o como pista para el admin en {@code REVIEW}. Nulo en el resto. */
    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_category", length = 32)
    private ModerationCategory moderationCategory;

    /** Evaluaciones que fallaron por timeout o salida inválida del modelo; al llegar al máximo pasa a {@code REVIEW}. */
    @Column(name = "moderation_attempts", nullable = false)
    private int moderationAttempts = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Momento en que se aprobó; es el que ordena los listados. */
    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "moderated_at")
    private Instant moderatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null)
            createdAt = Instant.now();
    }
}
