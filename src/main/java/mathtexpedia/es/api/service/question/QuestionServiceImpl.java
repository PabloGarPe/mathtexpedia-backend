package mathtexpedia.es.api.service.question;

import jakarta.persistence.PersistenceException;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.question.CreateQuestionDto;
import mathtexpedia.es.api.domain.model.question.QuestionDto;
import mathtexpedia.es.api.domain.model.question.UpdateQuestionDto;
import mathtexpedia.es.api.persistence.option.OptionDataService;
import mathtexpedia.es.api.persistence.question.Question;
import mathtexpedia.es.api.persistence.question.QuestionDataService;
import mathtexpedia.es.api.persistence.quiz.Quiz;
import mathtexpedia.es.api.persistence.quiz.QuizDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionServiceImpl implements QuestionService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionServiceImpl.class);

    private final QuestionDataService questionDataService;
    private final QuizDataService quizDataService;
    private final OptionDataService optionDataService;

    public QuestionServiceImpl(
            QuestionDataService questionDataService,
            QuizDataService quizDataService,
            OptionDataService optionDataService
    ) {
        this.questionDataService = questionDataService;
        this.quizDataService = quizDataService;
        this.optionDataService = optionDataService;
    }

    @Override
    public List<QuestionDto> getQuestionsByQuiz(long quizId) {
        logger.info("Fetching questions for quiz with id: {}", quizId);

        return questionDataService.getAllQuestionsByQuizId(quizId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Optional<QuestionDto> getQuestion(long id) {
        logger.info("Fetching question with id: {}", id);

        Optional<Question> question = questionDataService.getQuestionById(id);
        return question.map(this::toDto);
    }

    @Override
    public QuestionDto create(CreateQuestionDto dto) throws MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Creating new question with text: {}", dto.getText());

        Question question = new Question();
        question.setText(dto.getText());
        question.setExplanation(dto.getExplanation());
        question.setType(dto.getType());
        question.setPosition(dto.getPosition());

        resolveQuiz(question, dto.getQuizId());

        try {
            Question created = questionDataService.create(question);
            return toDto(created);
        } catch (PersistenceException e) {
            throw new MathtexpediaConflictException("Error creating question: " + e.getMessage(), e);
        }
    }

    @Override
    public QuestionDto update(long questionId, UpdateQuestionDto dto) throws MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Updating question with id: {}", questionId);

        Question toUpdate = questionDataService.getQuestionById(questionId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Question not found with id: " + questionId));

        toUpdate.setText(dto.getText());
        toUpdate.setExplanation(dto.getExplanation());
        toUpdate.setType(dto.getType());
        toUpdate.setPosition(dto.getPosition());

        resolveQuiz(toUpdate, dto.getQuizId());

        try {
            Question updated = questionDataService.update(toUpdate);
            return toDto(updated);
        } catch (PersistenceException e) {
            throw new MathtexpediaConflictException("Error updating question: " + e.getMessage(), e);
        }

    }

    @Override
    @Transactional
    public void delete(long id) throws MathtexpediaNotFoundException {
        logger.info("Deleting question  with id: {}", id);

        optionDataService.getOptionsByQuestionId(id).forEach(optionDataService::delete);

        Question toDelete = questionDataService.getQuestionById(id)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Question not found with id: " + id));

        questionDataService.delete(toDelete);
    }

    private void resolveQuiz(Question target, Long quizId) throws MathtexpediaNotFoundException {
        Optional<Quiz> quiz = quizDataService.getById(quizId);

        if (quiz.isEmpty())
            throw new MathtexpediaNotFoundException("Quiz not found with id: " + quizId);

        target.setQuiz(quiz.get());
    }

    private QuestionDto toDto(Question question) {
        return new QuestionDto(
                question.getId(),
                question.getText(),
                question.getType(),
                question.getExplanation(),
                question.getQuiz().getId()
        );
    }
}