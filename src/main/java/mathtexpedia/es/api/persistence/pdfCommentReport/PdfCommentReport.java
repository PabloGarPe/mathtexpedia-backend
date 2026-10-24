package mathtexpedia.es.api.persistence.pdfCommentReport;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mathtexpedia.es.api.domain.model.pdfComment.ReportReason;
import mathtexpedia.es.api.persistence.pdfComment.PdfComment;
import mathtexpedia.es.api.persistence.user.UserAccount;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "pdf_comment_report",
        uniqueConstraints = @UniqueConstraint(name = "uk_pdf_comment_report", columnNames = {"comment_id", "reporter_id"}),
        indexes = @Index(name = "idx_pdf_comment_report_reporter", columnList = "reporter_id"))
public class PdfCommentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "comment_id", nullable = false)
    private PdfComment comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "reporter_id", nullable = false)
    private UserAccount reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReportReason reason;

    @Column(nullable = false)
    private boolean resolved = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
