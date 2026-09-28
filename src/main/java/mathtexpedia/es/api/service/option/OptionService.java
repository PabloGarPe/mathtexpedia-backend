package mathtexpedia.es.api.service.option;

import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.option.CreateOptionDto;
import mathtexpedia.es.api.domain.model.option.OptionDto;
import mathtexpedia.es.api.domain.model.option.UpdateOptionDto;

import java.util.List;
import java.util.Optional;

public interface OptionService {

    List<OptionDto> getOptionsByQuestion(long questionId) throws MathtexpediaNotFoundException;

    Optional<OptionDto> getOption(long id);

    OptionDto create(CreateOptionDto dto) throws MathtexpediaNotFoundException, MathtexpediaConflictException;

    OptionDto update(long optionId, UpdateOptionDto dto) throws MathtexpediaNotFoundException, MathtexpediaConflictException;

    void delete(long id) throws MathtexpediaNotFoundException;
}
