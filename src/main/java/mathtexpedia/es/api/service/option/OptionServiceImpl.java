package mathtexpedia.es.api.service.option;

import jakarta.persistence.PersistenceException;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.option.CreateOptionDto;
import mathtexpedia.es.api.domain.model.option.OptionDto;
import mathtexpedia.es.api.domain.model.option.UpdateOptionDto;
import mathtexpedia.es.api.persistence.option.Option;
import mathtexpedia.es.api.persistence.option.OptionDataService;
import mathtexpedia.es.api.persistence.question.Question;
import mathtexpedia.es.api.persistence.question.QuestionDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OptionServiceImpl implements OptionService {

    private static final Logger logger = LoggerFactory.getLogger(OptionServiceImpl.class);

    private final OptionDataService optionDataService;
    private final QuestionDataService questionDataService;
    private final OptionMapper optionMapper;

    public OptionServiceImpl(
            OptionDataService optionDataService,
            QuestionDataService questionDataService,
            OptionMapper optionMapper
    ) {
        this.optionDataService = optionDataService;
        this.questionDataService = questionDataService;
        this.optionMapper = optionMapper;
    }


    @Override
    public List<OptionDto> getOptionsByQuestion(long questionId) throws MathtexpediaNotFoundException {
        logger.info("Fetching options for question with id: {}", questionId);

        questionDataService.getQuestionById(questionId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Question not found with id: " + questionId));

        return optionDataService.getOptionsByQuestionId(questionId)
                .stream()
                .map(optionMapper::toDto)
                .toList();
    }

    @Override
    public Optional<OptionDto> getOption(long id) {
        logger.info("Fetching option with id: {}", id);

        Optional<Option> option = optionDataService.getOptionById(id);
        return option.map(optionMapper::toDto);
    }

    @Override
    public OptionDto create(CreateOptionDto dto) throws MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Creating option with text: {}", dto.getText());

        Option option = optionMapper.toEntity(dto);

        resolveQuestion(option, dto.getQuestionId());

        try {
            Option created = optionDataService.create(option);
            return optionMapper.toDto(created);
        } catch (PersistenceException e) {
            throw new MathtexpediaConflictException("Error creating option: " + e.getMessage(), e);
        }
    }

    @Override
    public OptionDto update(long optionId, UpdateOptionDto dto) throws MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Updating option with id: {}", optionId);

        Option toUpdate = optionDataService.getOptionById(optionId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Option not found with id: " + optionId));

        optionMapper.updateEntity(toUpdate, dto);

        resolveQuestion(toUpdate, dto.getQuestionId());

        try {
            Option updated = optionDataService.update(toUpdate);
            return optionMapper.toDto(updated);
        } catch (PersistenceException e) {
            throw new MathtexpediaConflictException("Error updating option: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(long id) throws MathtexpediaNotFoundException {
        logger.info("Deleting option with id: {}", id);

        Option toDelete = optionDataService.getOptionById(id)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Option not found with id: " + id));

        optionDataService.delete(toDelete);
    }

    private void resolveQuestion(Option target, long questionId) throws MathtexpediaNotFoundException {
        Optional<Question> question = questionDataService.getQuestionById(questionId);

        if (question.isEmpty())
            throw new MathtexpediaNotFoundException("Question not found with id: " + questionId);

        target.setQuestion(question.get());
    }
}
