package mathtexpedia.es.api.persistence.option;

import java.util.List;
import java.util.Optional;

public interface OptionDataService {

    List<Option> getOptionsByQuestionId(long questionId);

    Optional<Option> getOptionById(long id);

    Option create(Option option);

    Option update(Option option);

    void delete(Option option);
}
