package mathtexpedia.es.api.persistence.courseSubscription;

import java.util.List;

public interface CourseSubscriptionDataService {

    List<CourseSubscription> getByUser(long userAccountId);

    void add(long userAccountId, long degreeId, int year, Integer semester);

    void remove(long userAccountId, long degreeId, int year, Integer semester);

    void removeAboveYear(long degreeId, int years);
}
