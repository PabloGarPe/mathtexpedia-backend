package mathtexpedia.es.api.domain.model.pdfComment;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AdminCommentPageDto {

    private List<AdminCommentDto> items;
    private int page;
    private int size;
    private long total;
    private boolean hasNext;
}
