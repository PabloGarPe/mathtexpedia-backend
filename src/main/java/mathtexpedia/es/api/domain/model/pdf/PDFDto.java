package mathtexpedia.es.api.domain.model.pdf;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import mathtexpedia.es.api.domain.model.subject.SubjectDto;
import mathtexpedia.es.api.domain.model.subjectUnit.SubjectUnitDto;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
public class PDFDto {

    private Long id;
    private String name;
    private Date lastTimeEdited;
    private String description;
    private SubjectDto subject;
    private SubjectUnitDto subjectUnit;

    @Schema(description = "Nombre del autor principal. Nulo en PDFs antiguos que aún no tienen autores asignados",
            nullable = true, example = "Ada Lovelace")
    private String author;

    @Schema(description = "Nombres de los coautores, en el orden en que se indicaron. Vacío si no hay coautores",
            example = "[\"Alan Turing\"]")
    private List<String> coauthors;
}