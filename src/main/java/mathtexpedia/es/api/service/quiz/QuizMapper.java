package mathtexpedia.es.api.service.quiz;

import mathtexpedia.es.api.domain.model.question.QuestionExportableDto;
import mathtexpedia.es.api.domain.model.question.QuestionForAttemptDto;
import mathtexpedia.es.api.domain.model.question.QuestionForCorrectionDto;
import mathtexpedia.es.api.domain.model.quiz.*;
import mathtexpedia.es.api.persistence.quiz.Quiz;
import mathtexpedia.es.api.service.subject.SubjectMapper;
import mathtexpedia.es.api.service.subjectUnit.SubjectUnitMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuizMapper {

    private final SubjectMapper subjectMapper;
    private final SubjectUnitMapper subjectUnitMapper;

    public QuizMapper(SubjectMapper subjectMapper, SubjectUnitMapper subjectUnitMapper) {
        this.subjectMapper = subjectMapper;
        this.subjectUnitMapper = subjectUnitMapper;
    }

    public Quiz toEntity(CreateQuizDto dto) {
        Quiz quiz = new Quiz();
        quiz.setName(dto.getName());
        quiz.setDescription(dto.getDescription());
        quiz.setDifficulty(dto.getDifficulty());
        return quiz;
    }

    public void updateEntity(Quiz target, UpdateQuizDto dto) {
        target.setName(dto.getName());
        target.setDescription(dto.getDescription());
        target.setDifficulty(dto.getDifficulty());
    }

    public QuizDto toDto(Quiz quiz) {
        return new QuizDto(
                quiz.getId(),
                quiz.getName(),
                quiz.getDescription(),
                quiz.getDifficulty(),
                quiz.getLastTimeEdited(),
                subjectMapper.toDto(quiz.getSubject()),
                quiz.getSubjectUnit() != null ? subjectUnitMapper.toDto(quiz.getSubjectUnit()) : null
        );
    }

    public QuizForAttemptDto toAttemptDto(Quiz quiz, List<QuestionForAttemptDto> questions) {
        return new QuizForAttemptDto(
                quiz.getId(),
                quiz.getName(),
                quiz.getDescription(),
                quiz.getDifficulty(),
                questions
        );
    }

    public QuizExportableDto toExportableDto(Quiz quiz, List<QuestionExportableDto> questions) {
        return new QuizExportableDto(
                quiz.getName(),
                quiz.getDescription(),
                quiz.getDifficulty(),
                questions
        );
    }

    public QuizForCorrectionDto toCorrectionDto(Quiz quiz, List<QuestionForCorrectionDto> questions) {
        return new QuizForCorrectionDto(
                quiz.getId(),
                quiz.getName(),
                quiz.getDescription(),
                quiz.getDifficulty(),
                questions
        );
    }
}
