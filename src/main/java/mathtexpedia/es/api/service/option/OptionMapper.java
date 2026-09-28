package mathtexpedia.es.api.service.option;

import mathtexpedia.es.api.domain.model.option.CreateOptionDto;
import mathtexpedia.es.api.domain.model.option.OptionDto;
import mathtexpedia.es.api.domain.model.option.OptionExportableDto;
import mathtexpedia.es.api.domain.model.option.OptionForAttemptDto;
import mathtexpedia.es.api.domain.model.option.UpdateOptionDto;
import mathtexpedia.es.api.persistence.option.Option;
import org.springframework.stereotype.Component;

@Component
public class OptionMapper {

    public Option toEntity(CreateOptionDto dto) {
        Option option = new Option();
        option.setText(dto.getText());
        option.setCorrect(dto.isCorrect());
        option.setPosition(dto.getPosition());
        return option;
    }

    public void updateEntity(Option target, UpdateOptionDto dto) {
        target.setText(dto.getText());
        target.setCorrect(dto.isCorrect());
        target.setPosition(dto.getPosition());
    }

    public OptionDto toDto(Option option) {
        return new OptionDto(
                option.getId(),
                option.getText(),
                option.isCorrect(),
                option.getPosition(),
                option.getQuestion().getId()
        );
    }

    public OptionForAttemptDto toAttemptDto(Option option) {
        return new OptionForAttemptDto(
                option.getId(),
                option.getText(),
                option.getPosition()
        );
    }

    public OptionExportableDto toExportableDto(Option option) {
        return new OptionExportableDto(
                option.getText(),
                option.isCorrect(),
                option.getPosition()
        );
    }
}
