package mathtexpedia.es.api.service.question;

import mathtexpedia.es.api.domain.model.option.OptionDto;
import mathtexpedia.es.api.domain.model.option.OptionExportableDto;
import mathtexpedia.es.api.domain.model.option.OptionForAttemptDto;
import mathtexpedia.es.api.domain.model.question.*;
import mathtexpedia.es.api.persistence.option.Option;
import mathtexpedia.es.api.persistence.question.Question;
import mathtexpedia.es.api.service.option.OptionMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuestionMapper {

    private final OptionMapper optionMapper;

    public QuestionMapper(OptionMapper optionMapper) {
        this.optionMapper = optionMapper;
    }

    public Question toEntity(CreateQuestionDto dto) {
        Question question = new Question();
        question.setText(dto.getText());
        question.setExplanation(dto.getExplanation());
        question.setType(dto.getType());
        question.setPosition(dto.getPosition());
        return question;
    }

    public void updateEntity(Question target, UpdateQuestionDto dto) {
        target.setText(dto.getText());
        target.setExplanation(dto.getExplanation());
        target.setType(dto.getType());
        target.setPosition(dto.getPosition());
    }

    public QuestionDto toDto(Question question) {
        return new QuestionDto(
                question.getId(),
                question.getText(),
                question.getType(),
                question.getExplanation(),
                question.getPosition(),
                question.getQuiz().getId()
        );
    }

    public QuestionForAttemptDto toAttemptDto(Question question, List<Option> options) {
        List<OptionForAttemptDto> optionsForAttempt = options.stream()
                .map(optionMapper::toAttemptDto)
                .toList();

        return new QuestionForAttemptDto(
                question.getId(),
                question.getText(),
                question.getType(),
                question.getPosition(),
                optionsForAttempt
        );
    }

    public QuestionExportableDto toExportableDto(Question question, List<Option> options) {
        List<OptionExportableDto> optionsForExport = options.stream()
                .map(optionMapper::toExportableDto)
                .toList();

        return new QuestionExportableDto(
                question.getText(),
                question.getPosition(),
                question.getType(),
                question.getExplanation(),
                optionsForExport
        );
    }

    public QuestionForCorrectionDto toCorrectionDto(Question question, List<Option> options) {
        List<OptionDto> optionsForCorrection = options.stream()
                .map(optionMapper::toDto)
                .toList();

        return new QuestionForCorrectionDto(
                question.getId(),
                question.getText(),
                question.getType(),
                question.getExplanation(),
                question.getPosition(),
                optionsForCorrection
        );
    }
}
