package mathtexpedia.es.api.domain.model.mail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Mail {
    @Nullable
    String from;

    @NotBlank
    String subject;

    @NotBlank
    String body;

    @JsonIgnore
    String cc;

}
