package mathtexpedia.es.api.domain.model.pdfComment;

import lombok.Data;

import java.util.Date;

@Data
public class CommentDto {

    private Long id;
    private Long pdfId;
    private Long userAccountId;
    private Long parentId;
    private Long replyToId;
    private String body;
    private CommentStatus status;
    private ModerationCategory moderationCategory;

}
