package mathtexpedia.es.api.service.quiz;

import jakarta.persistence.PersistenceException;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.option.OptionForAttemptDto;
import mathtexpedia.es.api.domain.model.question.QuestionDto;
import mathtexpedia.es.api.domain.model.question.QuestionForAttemptDto;
import mathtexpedia.es.api.domain.model.quiz.CreateQuizDto;
import mathtexpedia.es.api.domain.model.quiz.QuizDto;
import mathtexpedia.es.api.domain.model.quiz.QuizForAttemptDto;
import mathtexpedia.es.api.domain.model.quiz.UpdateQuizDto;
import mathtexpedia.es.api.domain.model.subject.SubjectDto;
import mathtexpedia.es.api.domain.model.subjectUnit.SubjectUnitDto;
import mathtexpedia.es.api.persistence.option.Option;
import mathtexpedia.es.api.persistence.option.OptionDataService;
import mathtexpedia.es.api.persistence.question.Question;
import mathtexpedia.es.api.persistence.question.QuestionDataService;
import mathtexpedia.es.api.persistence.quiz.Quiz;
import mathtexpedia.es.api.persistence.quiz.QuizDataService;
import mathtexpedia.es.api.persistence.subject.Subject;
import mathtexpedia.es.api.persistence.subject.SubjectDataService;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnit;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnitDataService;
import mathtexpedia.es.api.service.question.QuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class QuizServiceImpl implements QuizService {

    private final static Logger logger = LoggerFactory.getLogger(QuizServiceImpl.class);

    private final QuizDataService quizDataService;
    private final SubjectDataService subjectDataService;
    private final SubjectUnitDataService subjectUnitDataService;
    private final QuestionService questionService;
    private final QuestionDataService questionDataService;
    private final OptionDataService optionDataService;

    public QuizServiceImpl(
            QuizDataService quizDataService,
            SubjectDataService subjectDataService,
            SubjectUnitDataService subjectUnitDataService,
            QuestionService questionService,
            QuestionDataService questionDataService,
            OptionDataService optionDataService) {
        this.quizDataService = quizDataService;
        this.subjectDataService = subjectDataService;
        this.subjectUnitDataService = subjectUnitDataService;
        this.questionService = questionService;
        this.questionDataService = questionDataService;
        this.optionDataService = optionDataService;
    }

    @Override
    public List<QuizDto> getQuizzes() {
        logger.info("Fetching all quizzes");

        return quizDataService.getAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Optional<QuizDto> getQuiz(long id) {
        logger.info("Fetching quiz with id: {}", id);

        Optional<Quiz> quiz = quizDataService.getById(id);
        return quiz.map(this::toDto);
    }

    @Override
    public List<QuizDto> getQuizzesBySubjectUnit(long subjectUnitId) {
        logger.info("Fetching quizzes for subject unit with id: {}", subjectUnitId);

        return quizDataService.getAllBySubjectUnitId(subjectUnitId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<QuizDto> getQuizzesBySubject(long subjectId) {
        logger.info("Fetching quizzes for subject with id: {}", subjectId);

        return quizDataService.getAllBySubjectId(subjectId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public QuizDto create(CreateQuizDto dto) throws MathtexpediaInvalidException, MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Creating new quiz with name: {}", dto.getName());

        Quiz quiz = new Quiz();
        quiz.setName(dto.getName());
        quiz.setDescription(dto.getDescription());
        quiz.setLastTimeEdited(new Date());

        resolveSubjectAndUnit(quiz, dto.getSubjectId(), dto.getSubjectUnitId());

        try {
            Quiz created = quizDataService.create(quiz);
            return toDto(created);
        } catch (PersistenceException e) {
            throw new MathtexpediaConflictException("Error creating Quiz: " + e.getMessage(), e);
        }
    }

    @Override
    public QuizForAttemptDto getQuizForAttempt(long quizId) throws MathtexpediaNotFoundException {
        logger.info("Fetching quiz for attempt with id: {}", quizId);

        Quiz quiz = quizDataService.getById(quizId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Quiz not found with id: " + quizId));

        List<QuestionForAttemptDto> questionsForQuiz = new ArrayList<>();

        for (Question question : questionDataService.getAllQuestionsByQuizId(quizId)) {
            List<Option> options = optionDataService.getOptionsByQuestionId(question.getId());

            questionsForQuiz.add(buildQuestionForAttempt(question, options));
        }

        return new QuizForAttemptDto(
                quiz.getId(),
                quiz.getName(),
                quiz.getDescription(),
                quiz.getDifficulty(),
                questionsForQuiz
        );
    }

    @Override
    public QuizDto update(long quizId, UpdateQuizDto dto) throws MathtexpediaNotFoundException, MathtexpediaInvalidException, MathtexpediaConflictException {
        logger.info("Updating quiz with id: {}", quizId);

        Quiz toUpdate = quizDataService.getById(quizId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Quiz not found with id: " + quizId));

        toUpdate.setName(dto.getName());
        toUpdate.setDescription(dto.getDescription());
        toUpdate.setLastTimeEdited(new Date());

        resolveSubjectAndUnit(toUpdate, dto.getSubjectId(), dto.getSubjectUnitId());

        try {
            Quiz updated = quizDataService.update(toUpdate);
            return toDto(updated);
        } catch (PersistenceException e) {
            throw new MathtexpediaConflictException("Error updating Quiz: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void delete(long id) throws MathtexpediaNotFoundException {
        logger.info("Deleting quiz with id: {}", id);

        for (QuestionDto question : questionService.getQuestionsByQuiz(id)) {
            questionService.delete(question.getId());
        }

        Quiz toDelete = quizDataService.getById(id)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Quiz not found with id: " + id));

        quizDataService.delete(toDelete);

    }

    private void resolveSubjectAndUnit(Quiz target, Long subjectId, Long subjectUnitId)
            throws MathtexpediaNotFoundException, MathtexpediaInvalidException {
        Subject subject = subjectDataService.getById(subjectId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Subject not found with id: " + subjectId));
        target.setSubject(subject);

        if (subjectUnitId != null) {
            SubjectUnit subjectUnit = subjectUnitDataService.getById(subjectUnitId)
                    .orElseThrow(() -> new MathtexpediaNotFoundException("Subject unit not found with id: " + subjectUnitId));

            if (!Objects.equals(subjectUnit.getSubject().getId(), subjectId))
                throw new MathtexpediaInvalidException("Subject unit with id: " + subjectUnitId + " does not belong to subject with id: " + subjectId);


            target.setSubjectUnit(subjectUnit);
        } else {
            target.setSubjectUnit(null);
        }
    }

    private QuestionForAttemptDto buildQuestionForAttempt(Question question, List<Option> options) {
        List<OptionForAttemptDto> optionsForAttempt = new ArrayList<>();

        for (Option option : options) {
            OptionForAttemptDto optionForAttempt = new OptionForAttemptDto(
                    option.getId(),
                    option.getText(),
                    option.getPosition()
            );
            optionsForAttempt.add(optionForAttempt);
        }

        return new QuestionForAttemptDto(
                question.getId(),
                question.getText(),
                question.getType(),
                question.getPosition(),
                optionsForAttempt
        );
    }

    private QuizDto toDto(Quiz quiz) {
        return new QuizDto(
                quiz.getId(),
                quiz.getName(),
                quiz.getDescription(),
                quiz.getDifficulty(),
                quiz.getLastTimeEdited(),
                new SubjectDto(
                        quiz.getSubject().getId(),
                        quiz.getSubject().getName(),
                        quiz.getSubject().getDescription()
                ),
                quiz.getSubjectUnit() != null ? new SubjectUnitDto(
                        quiz.getSubjectUnit().getId(),
                        quiz.getSubjectUnit().getName(),
                        quiz.getSubjectUnit().getPosition()
                ) : null
        );
    }
}