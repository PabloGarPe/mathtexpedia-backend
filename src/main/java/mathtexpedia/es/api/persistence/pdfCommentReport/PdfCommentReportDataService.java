package mathtexpedia.es.api.persistence.pdfCommentReport;

import mathtexpedia.es.api.domain.model.pdfComment.ReportReason;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

public interface PdfCommentReportDataService {

    boolean insertIfAbsent(long commentId, long reporterId, ReportReason reason, Instant now);

    long countOpenReporters(long commentId);

    int resolveAll(long commentId);

    Map<Long, Map<ReportReason, Long>> countOpenByReason(Collection<Long> commentIds);
}
