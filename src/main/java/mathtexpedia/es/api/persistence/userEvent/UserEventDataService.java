package mathtexpedia.es.api.persistence.userEvent;

import mathtexpedia.es.api.domain.model.userEvent.EventType;
import mathtexpedia.es.api.persistence.user.UserAccount;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface UserEventDataService {

    void record(UserAccount user, EventType type, String payload);

    Map<Long, Long> countPdfViews(Collection<Long> pdfIds);

    List<Long> getTopViewedPdfIds(int limit);

    List<Long> getTopViewedPdfIdsByUser(long userAccountId, int limit);

}
