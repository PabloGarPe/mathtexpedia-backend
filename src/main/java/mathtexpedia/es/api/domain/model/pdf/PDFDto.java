package mathtexpedia.es.api.domain.model.pdf;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import mathtexpedia.es.api.domain.model.pdfRating.PdfRatingSummaryDto;
import mathtexpedia.es.api.domain.model.subject.SubjectDto;
import mathtexpedia.es.api.domain.model.subjectUnit.SubjectUnitDto;
import mathtexpedia.es.api.domain.model.userAccount.AuthorDto;

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

    @Schema(description = "Nombre público (username) del autor principal. Nulo en PDFs antiguos que aún no tienen autores asignados "
            + "o si el autor no ha completado su perfil", nullable = true, example = "ada_lovelace")
    private String author;

    @ArraySchema(
            arraySchema = @Schema(description = "Nombres públicos de los coautores, en el orden en que se indicaron. Vacío si no hay coautores. "
                    + "Un coautor sin perfil completo aparece como nulo", example = "[\"alan_turing\"]"),
            schema = @Schema(nullable = true))
    private List<String> coauthors;

    @Schema(description = "Autores con su identificador y marca de foto de perfil: el primero es el principal, el resto coautores, "
            + "en el mismo orden que 'author' y 'coauthors'. Vacío en PDFs antiguos sin autores")
    private List<AuthorDto> authors;

    @Schema(description = "Número total de veces que se ha abierto el PDF (todos los usuarios; cada apertura cuenta, aunque sea del mismo usuario). 0 si nunca se ha visto",
            example = "42")
    private long viewCount;

    @Schema(description = "Resumen público de valoraciones (👍/👎). Nunca dice quién votó")
    private PdfRatingSummaryDto rating;
}
