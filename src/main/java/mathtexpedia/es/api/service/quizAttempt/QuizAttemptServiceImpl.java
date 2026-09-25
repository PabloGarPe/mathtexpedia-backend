package mathtexpedia.es.api.service.quizAttempt;

import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.attemptAnswer.AttemptAnswerResultDto;
import mathtexpedia.es.api.domain.model.attemptAnswer.AttemptAnswerSubmissionDto;
import mathtexpedia.es.api.domain.model.option.OptionDto;
import mathtexpedia.es.api.domain.model.question.QuestionForCorrectionDto;
import mathtexpedia.es.api.domain.model.quiz.QuizForCorrectionDto;
import mathtexpedia.es.api.domain.model.quizAttempt.QuizAttemptDto;
import mathtexpedia.es.api.domain.model.quizAttempt.QuizAttemptResultDto;
import mathtexpedia.es.api.domain.model.quizAttempt.SubmitQuizAttemptDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.persistence.attemptAnswer.AttemptAnswer;
import mathtexpedia.es.api.persistence.attemptAnswer.AttemptAnswerDataService;
import mathtexpedia.es.api.persistence.option.Option;
import mathtexpedia.es.api.persistence.option.OptionDataService;
import mathtexpedia.es.api.persistence.question.Question;
import mathtexpedia.es.api.persistence.question.QuestionDataService;
import mathtexpedia.es.api.persistence.quiz.Quiz;
import mathtexpedia.es.api.persistence.quiz.QuizDataService;
import mathtexpedia.es.api.persistence.quizAttempt.QuizAttempt;
import mathtexpedia.es.api.persistence.quizAttempt.QuizAttemptDataService;
import mathtexpedia.es.api.persistence.user.UserAccount;
import mathtexpedia.es.api.service.quiz.QuizService;
import mathtexpedia.es.api.service.userAccount.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class QuizAttemptServiceImpl implements QuizAttemptService {

    private final Logger logger = LoggerFactory.getLogger(QuizAttemptServiceImpl.class);
    private final QuizService quizService;
    private final UserAccountService userAccountService;
    private final QuizDataService quizDataService;
    private final QuizAttemptDataService quizAttemptDataService;
    private final QuestionDataService questionDataService;
    private final OptionDataService optionDataService;
    private final AttemptAnswerDataService attemptAnswerDataService;
    private final QuizAttemptMapper quizAttemptMapper;

    public QuizAttemptServiceImpl(QuizService quizService, UserAccountService userAccountService, QuizDataService quizDataService, QuizAttemptDataService quizAttemptDataService, QuestionDataService questionDataService, OptionDataService optionDataService, AttemptAnswerDataService attemptAnswerDataService, QuizAttemptMapper quizAttemptMapper) {
        this.quizService = quizService;
        this.userAccountService = userAccountService;
        this.quizDataService = quizDataService;
        this.quizAttemptDataService = quizAttemptDataService;
        this.questionDataService = questionDataService;
        this.optionDataService = optionDataService;
        this.attemptAnswerDataService = attemptAnswerDataService;
        this.quizAttemptMapper = quizAttemptMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuizAttemptResultDto submitAttempt(long quizId, SubmitQuizAttemptDto dto, UserProfile user) throws MathtexpediaNotFoundException, MathtexpediaUnauthorizedException {
        logger.info("Submitting quiz attempt for quizId: {}, userId: {}", quizId, user.getId());

        QuizForCorrectionDto quizForCorrection = quizService.getQuizForCorrection(quizId);
        QuizAttemptResultDto result = correctAnswers(quizForCorrection, dto);

        UserAccount account = userAccountService.getOrProvision(user);
        Quiz quiz = quizDataService.getById(quizId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Quiz not found with id: " + quizId));

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(account);
        attempt.setQuiz(quiz);
        attempt.setScore(result.getScore());
        attempt.setTotalQuestions(result.getTotalQuestions());
        attempt.setSubmittedAt(Instant.now());

        QuizAttempt savedAttempt = quizAttemptDataService.createQuizAttempt(attempt);
        result.setId(savedAttempt.getId());

        for (AttemptAnswerResultDto answerResult : result.getAnswers()) {
            AttemptAnswer answerEntity = new AttemptAnswer();
            answerEntity.setAttempt(savedAttempt);
            answerEntity.setCorrect(answerResult.isCorrect());

            Question question = questionDataService.getQuestionById(answerResult.getQuestionId())
                    .orElseThrow(() -> new MathtexpediaNotFoundException("Question not found with id: " + answerResult.getQuestionId()));
            answerEntity.setQuestion(question);

            if (answerResult.getSelectedOptionId() != null) {
                Option selectedOption = optionDataService.getOptionById(answerResult.getSelectedOptionId())
                        .orElseThrow(() -> new MathtexpediaNotFoundException("Option not found with id: " + answerResult.getSelectedOptionId()));
                answerEntity.setSelectedOption(selectedOption);
            }

            attemptAnswerDataService.saveAttempt(answerEntity);
        }

        return result;
    }

    @Override
    public Page<QuizAttemptDto> getMyAttempts(UserProfile user, Pageable pageable) throws MathtexpediaUnauthorizedException {
        logger.info("Fetching all quizzes attempts for userId: {}", user.getId());

        UserAccount account = userAccountService.getOrProvision(user);

        return quizAttemptDataService.getByUserId(account.getId(), pageable)
                .map(quizAttemptMapper::toDto);
    }

    @Override
    public List<QuizAttemptDto> getAttemptsForQuiz(long quizId, UserProfile user) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException {
        logger.info("Fetching all attempts for quizId: {}, userId: {}", quizId, user.getId());

        UserAccount account = userAccountService.getOrProvision(user);
        quizDataService.getById(quizId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Quiz not found with id: " + quizId));

        return quizAttemptDataService.getByQuizIdAndUserId(quizId, account.getId())
                .stream()
                .map(quizAttemptMapper::toDto)
                .toList();
    }

    private QuizAttemptResultDto correctAnswers(QuizForCorrectionDto quiz, SubmitQuizAttemptDto dto) {
        QuizAttemptResultDto result = new QuizAttemptResultDto();
        result.setTotalQuestions(quiz.getQuestions().size());

        double rawScore = 0;
        List<AttemptAnswerResultDto> answers = new ArrayList<>();

        for (QuestionForCorrectionDto question : quiz.getQuestions()) {
            Long correctOptionId = question.getOptions().stream()
                    .filter(OptionDto::isCorrect)
                    .findFirst()
                    .map(OptionDto::getId)
                    .orElse(null);

            Long selectedOptionId = dto.getAnswers().stream()
                    .filter(a -> Objects.equals(a.getQuestionId(), question.getId()))
                    .findFirst()
                    .map(AttemptAnswerSubmissionDto::getSelectedOptionId)
                    .orElse(null);

            boolean answered = selectedOptionId != null;
            boolean isCorrect = answered && Objects.equals(selectedOptionId, correctOptionId);

            if (!answered) {
                result.setUnansweredQuestions(result.getUnansweredQuestions() + 1);
            } else if (isCorrect) {
                result.setCorrectAnswers(result.getCorrectAnswers() + 1);
                rawScore += 1;
            } else {
                result.setIncorrectAnswers(result.getIncorrectAnswers() + 1);
                int optionCount = question.getOptions().size();
                if (optionCount > 1) {
                    rawScore -= 1.0 / (optionCount - 1);
                }
            }

            answers.add(new AttemptAnswerResultDto(
                    question.getId(),
                    selectedOptionId,
                    correctOptionId,
                    isCorrect,
                    question.getExplanation()
            ));
        }

        result.setScore(Math.max(0, (rawScore / result.getTotalQuestions()) * 10));
        result.setPercentage((result.getCorrectAnswers() * 100.0) / result.getTotalQuestions());
        result.setAnswers(answers);

        return result;
    }
}
