package mathtexpedia.es.api.service.pdfComment;

import mathtexpedia.es.api.domain.model.pdfComment.CommentStatus;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

final class CommentTransitions {

    /** La IA solo resuelve comentarios que esperan revisión. */
    static final Set<CommentStatus> BY_AI = states(CommentStatus.PENDING);

    /** Un admin puede aprobar cualquier cosa que no esté publicada ni borrada (incluye restaurar falsos positivos). */
    static final Set<CommentStatus> APPROVABLE_BY_ADMIN =
            states(CommentStatus.PENDING, CommentStatus.REVIEW, CommentStatus.HIDDEN, CommentStatus.REJECTED);

    /** Un admin puede rechazar lo que está esperando o publicado. */
    static final Set<CommentStatus> REJECTABLE_BY_ADMIN =
            states(CommentStatus.PENDING, CommentStatus.REVIEW, CommentStatus.PUBLISHED, CommentStatus.HIDDEN);

    /** Estados que se pueden consultar en la cola de moderación. */
    static final Set<CommentStatus> ADMIN_QUEUE =
            states(CommentStatus.PENDING, CommentStatus.REVIEW, CommentStatus.HIDDEN, CommentStatus.REJECTED);

    private CommentTransitions() {
    }

    private static Set<CommentStatus> states(CommentStatus first, CommentStatus... rest) {
        return Collections.unmodifiableSet(EnumSet.of(first, rest));
    }
}
