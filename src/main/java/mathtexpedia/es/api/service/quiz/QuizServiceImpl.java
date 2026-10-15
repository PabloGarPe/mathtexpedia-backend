package mathtexpedia.es.api.service.quiz;

import jakarta.persistence.PersistenceException;
import mathtexpedia.es.api.domain.event.QuizPublishedEvent;
import mathtexpedia.es.api.domain.event.ResourceDeletedEvent;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.notification.ResourceType;
import mathtexpedia.es.api.domain.model.option.CreateOptionDto;
import mathtexpedia.es.api.domain.model.option.OptionExportableDto;
import mathtexpedia.es.api.domain.model.question.*;
import mathtexpedia.es.api.domain.model.quiz.*;
import mathtexpedia.es.api.infrastructure.security.CurrentUser;
import mathtexpedia.es.api.persistence.attemptAnswer.AttemptAnswerDataService;
import mathtexpedia.es.api.persistence.option.Option;
import mathtexpedia.es.api.persistence.option.OptionDataService;
import mathtexpedia.es.api.persistence.question.Question;
import mathtexpedia.es.api.persistence.question.QuestionDataService;
import mathtexpedia.es.api.persistence.quiz.Quiz;
import mathtexpedia.es.api.persistence.quiz.QuizDataService;
import mathtexpedia.es.api.persistence.quizAttempt.QuizAttemptDataService;
import mathtexpedia.es.api.persistence.subject.Subject;
import mathtexpedia.es.api.persistence.subject.SubjectDataService;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnit;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnitDataService;
import mathtexpedia.es.api.service.option.OptionService;
import mathtexpedia.es.api.service.question.QuestionMapper;
import mathtexpedia.es.api.service.question.QuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class QuizServiceImpl implements QuizService {

    private static final Logger logger = LoggerFactory.getLogger(QuizServiceImpl.class);
    private static final String QUIZ_NOT_FOUND_MESSAGE = "Quiz not found with id: ";
    private static final int DEFAULT_FEED_LIMIT = 10;
    private static final int MAX_FEED_LIMIT = 20;

    private final QuizDataService quizDataService;
    private final QuizAttemptDataService quizAttemptDataService;
    private final AttemptAnswerDataService attemptAnswerDataService;
    private final SubjectDataService subjectDataService;
    private final SubjectUnitDataService subjectUnitDataService;
    private final QuestionService questionService;
    private final OptionService optionService;
    private final QuestionDataService questionDataService;
    private final OptionDataService optionDataService;
    private final QuizMapper quizMapper;
    private final QuestionMapper questionMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final CurrentUser currentUser;

    public QuizServiceImpl(
            QuizDataService quizDataService,
            QuizAttemptDataService quizAttemptDataService,
            AttemptAnswerDataService attemptAnswerDataService,
            SubjectDataService subjectDataService,
            SubjectUnitDataService subjectUnitDataService,
            QuestionService questionService,
            OptionService optionService,
            QuestionDataService questionDataService,
            OptionDataService optionDataService,
            QuizMapper quizMapper,
            QuestionMapper questionMapper,
            ApplicationEventPublisher eventPublisher,
            CurrentUser currentUser) {
        this.quizDataService = quizDataService;
        this.quizAttemptDataService = quizAttemptDataService;
        this.attemptAnswerDataService = attemptAnswerDataService;
        this.subjectDataService = subjectDataService;
        this.subjectUnitDataService = subjectUnitDataService;
        this.questionService = questionService;
        this.optionService = optionService;
        this.questionDataService = questionDataService;
        this.optionDataService = optionDataService;
        this.quizMapper = quizMapper;
        this.questionMapper = questionMapper;
        this.eventPublisher = eventPublisher;
        this.currentUser = currentUser;
    }

    @Override
    public List<QuizDto> getQuizzes(boolean includeHidden) {
        logger.info("Fetching all quizzes (includeHidden: {})", includeHidden);

        return toDtos(quizDataService.getAll(!includeHidden));
    }

    @Override
    public List<QuizDto> getRecentQuizzes(Integer limit, boolean includeHidden) {
        logger.info("Fetching recent quizzes (limit: {}, includeHidden: {})", limit, includeHidden);

        return toDtos(quizDataService.getRecent(normalizeLimit(limit), !includeHidden));
    }

    @Override
    public List<QuizDto> getMostAttemptedQuizzes(Integer limit, boolean includeHidden) {
        logger.info("Fetching most attempted quizzes (limit: {}, includeHidden: {})", limit, includeHidden);

        List<Long> orderedIds = quizAttemptDataService.getMostAttemptedQuizIds(normalizeLimit(limit), !includeHidden);

        Map<Long, Quiz> byId = new HashMap<>();
        quizDataService.getByIds(orderedIds).forEach(quiz -> byId.put(quiz.getId(), quiz));

        return toDtos(orderedIds.stream().map(byId::get).filter(Objects::nonNull).toList());
    }

    @Override
    public Optional<QuizDto> getQuiz(long id, boolean includeHidden) {
        logger.info("Fetching quiz with id: {}", id);

        Optional<Quiz> quiz = quizDataService.getById(id).filter(found -> includeHidden || found.isVisible());
        return quiz.map(found -> toDtos(List.of(found)).get(0));
    }

    @Override
    public List<QuizDto> getQuizzesBySubjectUnit(long subjectUnitId, boolean includeHidden) throws MathtexpediaNotFoundException {
        logger.info("Fetching quizzes for subject unit with id: {}", subjectUnitId);

        if (subjectUnitDataService.getById(subjectUnitId).isEmpty())
            throw new MathtexpediaNotFoundException("Subject unit not found with id: " + subjectUnitId);

        return toDtos(quizDataService.getAllBySubjectUnitId(subjectUnitId, !includeHidden));
    }

    @Override
    public List<QuizDto> getQuizzesBySubject(long subjectId, boolean includeHidden) throws MathtexpediaNotFoundException {
        logger.info("Fetching quizzes for subject with id: {}", subjectId);

        if (subjectDataService.getById(subjectId).isEmpty())
            throw new MathtexpediaNotFoundException("Subject not found with id: " + subjectId);

        return toDtos(quizDataService.getAllBySubjectId(subjectId, !includeHidden));
    }

    @Override
    public QuizDto create(CreateQuizDto dto) throws MathtexpediaInvalidException, MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Creating new quiz with name: {}", dto.getName());

        Quiz quiz = quizMapper.toEntity(dto);
        quiz.setLastTimeEdited(Instant.now());
        // Los cuestionarios se crean vacíos y se rellenan pregunta a pregunta: nacen ocultos hasta que se publican
        quiz.setVisible(false);

        resolveSubjectAndUnit(quiz, dto.getSubjectId(), dto.getSubjectUnitId());

        try {
            Quiz created = quizDataService.create(quiz);
            return quizMapper.toDto(created, 0);
        } catch (PersistenceException e) {
            throw new MathtexpediaConflictException("Error creating Quiz: " + e.getMessage(), e);
        }
    }

    @Override
    public QuizForAttemptDto getQuizForAttempt(long quizId, boolean includeHidden) throws MathtexpediaNotFoundException {
        logger.info("Fetching quiz for attempt with id: {}", quizId);

        Quiz quiz = quizDataService.getById(quizId)
                .filter(found -> includeHidden || found.isVisible())
                .orElseThrow(() -> new MathtexpediaNotFoundException(QUIZ_NOT_FOUND_MESSAGE + quizId));

        List<QuestionForAttemptDto> questionsForQuiz = new ArrayList<>();

        for (Question question : questionDataService.getAllQuestionsByQuizId(quizId)) {
            List<Option> options = optionDataService.getOptionsByQuestionId(question.getId());

            questionsForQuiz.add(questionMapper.toAttemptDto(question, options));
        }

        return quizMapper.toAttemptDto(quiz, questionsForQuiz);
    }

    @Override
    public QuizForCorrectionDto getQuizForCorrection(long quizId) throws MathtexpediaNotFoundException {
        logger.info("Fetching quiz for correction with id: {}", quizId);

        Quiz quiz = quizDataService.getById(quizId)
                .orElseThrow(() -> new MathtexpediaNotFoundException(QUIZ_NOT_FOUND_MESSAGE + quizId));

        List<QuestionForCorrectionDto> questionsForQuiz = new ArrayList<>();

        for (Question question : questionDataService.getAllQuestionsByQuizId(quizId)) {
            List<Option> options = optionDataService.getOptionsByQuestionId(question.getId());

            questionsForQuiz.add(questionMapper.toCorrectionDto(question, options));
        }

        return quizMapper.toCorrectionDto(quiz, questionsForQuiz);
    }

    @Override
    public QuizDto update(long quizId, UpdateQuizDto dto) throws MathtexpediaNotFoundException, MathtexpediaInvalidException, MathtexpediaConflictException {
        logger.info("Updating quiz with id: {}", quizId);

        Quiz toUpdate = quizDataService.getById(quizId)
                .orElseThrow(() -> new MathtexpediaNotFoundException(QUIZ_NOT_FOUND_MESSAGE + quizId));

        quizMapper.updateEntity(toUpdate, dto);
        toUpdate.setLastTimeEdited(Instant.now());

        resolveSubjectAndUnit(toUpdate, dto.getSubjectId(), dto.getSubjectUnitId());

        try {
            Quiz updated = quizDataService.update(toUpdate);
            return toDtos(List.of(updated)).get(0);
        } catch (PersistenceException e) {
            throw new MathtexpediaConflictException("Error updating Quiz: " + e.getMessage(), e);
        }
    }

    @Override
    public QuizDto setVisibility(long quizId, boolean visible) throws MathtexpediaNotFoundException, MathtexpediaInvalidException {
        logger.info("Setting visibility of quiz {} to {}", quizId, visible);

        Quiz quiz = quizDataService.getById(quizId)
                .orElseThrow(() -> new MathtexpediaNotFoundException(QUIZ_NOT_FOUND_MESSAGE + quizId));

        if (quiz.isVisible() == visible)
            return toDtos(List.of(quiz)).get(0);

        if (visible) {
            if (questionDataService.getAllQuestionsByQuizId(quizId).isEmpty())
                throw new MathtexpediaInvalidException("Quiz with id: " + quizId + " has no questions and cannot be published");

            return toDtos(List.of(publish(quiz))).get(0);
        }

        quiz.setVisible(false);
        return toDtos(List.of(quizDataService.update(quiz))).get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(long id) throws MathtexpediaNotFoundException {
        logger.info("Deleting quiz with id: {}", id);

        Quiz toDelete = quizDataService.getById(id)
                .orElseThrow(() -> new MathtexpediaNotFoundException(QUIZ_NOT_FOUND_MESSAGE + id));

        // Los intentos referencian al cuestionario y sus respuestas a preguntas y opciones: van primero
        attemptAnswerDataService.deleteByQuizId(id);
        quizAttemptDataService.deleteByQuizId(id);

        for (QuestionDto question : questionService.getQuestionsByQuiz(id)) {
            questionService.delete(question.getId());
        }

        quizDataService.delete(toDelete);

        eventPublisher.publishEvent(new ResourceDeletedEvent(ResourceType.QUIZ, id));
    }

    @Override
    public QuizExportableDto exportQuiz(long quizId) throws MathtexpediaNotFoundException {
        logger.info("Exporting quiz with id: {}", quizId);

        Quiz quiz = quizDataService.getById(quizId)
                .orElseThrow(() -> new MathtexpediaNotFoundException(QUIZ_NOT_FOUND_MESSAGE + quizId));

        List<QuestionExportableDto> questionsForQuiz = new ArrayList<>();

        for (Question question : questionDataService.getAllQuestionsByQuizId(quizId)) {
            List<Option> options = optionDataService.getOptionsByQuestionId(question.getId());

            questionsForQuiz.add(questionMapper.toExportableDto(question, options));
        }

        return quizMapper.toExportableDto(quiz, questionsForQuiz);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuizDto importQuiz(QuizExportableDto dto, long subjectId, Long subjectUnitId)
            throws MathtexpediaInvalidException, MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Importing quiz with name: {}", dto.getName());

        CreateQuizDto createQuizDto = new CreateQuizDto();
        createQuizDto.setName(dto.getName());
        createQuizDto.setDescription(dto.getDescription());
        createQuizDto.setDifficulty(dto.getDifficulty());
        createQuizDto.setSubjectId(subjectId);
        createQuizDto.setSubjectUnitId(subjectUnitId);

        QuizDto createdQuiz = create(createQuizDto);

        for (QuestionExportableDto questionDto : dto.getQuestions()) {
            CreateQuestionDto createQuestionDto = new CreateQuestionDto();
            createQuestionDto.setText(questionDto.getText());
            createQuestionDto.setType(questionDto.getType());
            createQuestionDto.setPosition(questionDto.getPosition());
            createQuestionDto.setExplanation(questionDto.getExplanation());
            createQuestionDto.setQuizId(createdQuiz.getId());

            QuestionDto createdQuestion = questionService.create(createQuestionDto);

            for (OptionExportableDto optionDto : questionDto.getOptions()) {
                CreateOptionDto createOptionDto = new CreateOptionDto();
                createOptionDto.setText(optionDto.getText());
                createOptionDto.setCorrect(optionDto.isCorrect());
                createOptionDto.setPosition(optionDto.getPosition());
                createOptionDto.setQuestionId(createdQuestion.getId());

                optionService.create(createOptionDto);
            }
        }

        // Se importa completo, así que se publica al momento (si trae preguntas; uno vacío se queda oculto, como al crearlo)
        if (!dto.getQuestions().isEmpty()) {
            Quiz quiz = quizDataService.getById(createdQuiz.getId())
                    .orElseThrow(() -> new MathtexpediaNotFoundException(QUIZ_NOT_FOUND_MESSAGE + createdQuiz.getId()));
            return quizMapper.toDto(publish(quiz), 0);
        }

        return createdQuiz;
    }

    /**
     * Hace visible el cuestionario. Solo la primera publicación fija {@code publishedAt}, lo sube a "Novedades" y
     * notifica; ocultarlo y volver a publicarlo (p. ej. para corregir una errata) no avisa a nadie otra vez.
     */
    private Quiz publish(Quiz quiz) {
        boolean firstPublication = quiz.getPublishedAt() == null;

        quiz.setVisible(true);
        if (firstPublication) {
            Instant now = Instant.now();
            quiz.setPublishedAt(now);
            quiz.setLastTimeEdited(now);
        }

        Quiz updated = quizDataService.update(quiz);

        if (firstPublication)
            eventPublisher.publishEvent(new QuizPublishedEvent(updated.getId(), currentUser.externalIdOrNull()));

        return updated;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null)
            return DEFAULT_FEED_LIMIT;
        return Math.max(1, Math.min(limit, MAX_FEED_LIMIT));
    }

    /** Convierte a DTO conservando el orden recibido, con una única consulta para todos los contadores de intentos. */
    private List<QuizDto> toDtos(List<Quiz> quizzes) {
        Map<Long, Long> attempts = quizAttemptDataService.countAttemptsByQuizIds(quizzes.stream().map(Quiz::getId).toList());
        return quizzes.stream()
                .map(quiz -> quizMapper.toDto(quiz, attempts.getOrDefault(quiz.getId(), 0L)))
                .toList();
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
}
