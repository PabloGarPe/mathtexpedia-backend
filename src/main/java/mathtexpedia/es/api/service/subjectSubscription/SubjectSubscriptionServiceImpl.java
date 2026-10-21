package mathtexpedia.es.api.service.subjectSubscription;

import jakarta.persistence.PersistenceException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.subjectSubscription.CourseSubscriptionDto;
import mathtexpedia.es.api.domain.model.subjectSubscription.SubjectSubscriptionDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.persistence.courseSubscription.CourseSubscription;
import mathtexpedia.es.api.persistence.courseSubscription.CourseSubscriptionDataService;
import mathtexpedia.es.api.persistence.degree.Degree;
import mathtexpedia.es.api.persistence.degree.DegreeDataService;
import mathtexpedia.es.api.persistence.subject.SubjectDataService;
import mathtexpedia.es.api.persistence.subjectSubscription.SubjectSubscription;
import mathtexpedia.es.api.persistence.subjectSubscription.SubjectSubscriptionDataService;
import mathtexpedia.es.api.persistence.user.UserAccount;
import mathtexpedia.es.api.service.degree.DegreeMapper;
import mathtexpedia.es.api.service.subject.SubjectMapper;
import mathtexpedia.es.api.service.userAccount.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubjectSubscriptionServiceImpl implements SubjectSubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(SubjectSubscriptionServiceImpl.class);

    private final SubjectSubscriptionDataService subscriptionDataService;
    private final CourseSubscriptionDataService courseSubscriptionDataService;
    private final SubjectDataService subjectDataService;
    private final DegreeDataService degreeDataService;
    private final UserAccountService userAccountService;
    private final SubjectMapper subjectMapper;
    private final DegreeMapper degreeMapper;

    public SubjectSubscriptionServiceImpl(SubjectSubscriptionDataService subscriptionDataService,
                                          CourseSubscriptionDataService courseSubscriptionDataService,
                                          SubjectDataService subjectDataService,
                                          DegreeDataService degreeDataService,
                                          UserAccountService userAccountService,
                                          SubjectMapper subjectMapper,
                                          DegreeMapper degreeMapper) {
        this.subscriptionDataService = subscriptionDataService;
        this.courseSubscriptionDataService = courseSubscriptionDataService;
        this.subjectDataService = subjectDataService;
        this.degreeDataService = degreeDataService;
        this.userAccountService = userAccountService;
        this.subjectMapper = subjectMapper;
        this.degreeMapper = degreeMapper;
    }

    @Override
    public List<SubjectSubscriptionDto> getMySubscriptions(UserProfile user) throws MathtexpediaUnauthorizedException {
        logger.info("Fetching subscriptions of user {}", user.getId());

        UserAccount account = userAccountService.getOrProvision(user);
        return subscriptionDataService.getByUser(account.getId()).stream().map(this::toDto).toList();
    }

    @Override
    public List<CourseSubscriptionDto> getMyCourseSubscriptions(UserProfile user) throws MathtexpediaUnauthorizedException {
        logger.info("Fetching course subscriptions of user {}", user.getId());

        UserAccount account = userAccountService.getOrProvision(user);
        return courseSubscriptionDataService.getByUser(account.getId()).stream().map(this::toDto).toList();
    }

    @Override
    public SubjectSubscriptionDto subscribe(UserProfile user, long subjectId)
            throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException {
        logger.info("User {} subscribing to subject {}", user.getId(), subjectId);

        UserAccount account = userAccountService.getOrProvision(user);
        if (subjectDataService.getById(subjectId).isEmpty())
            throw new MathtexpediaNotFoundException("Subject not found with id: " + subjectId);

        try {
            subscriptionDataService.add(account.getId(), subjectId);
        } catch (PersistenceException e) {
            // La asignatura se ha borrado justo entre la comprobación y el alta: la FK lo impide
            throw new MathtexpediaNotFoundException("Subject not found with id: " + subjectId);
        }

        return subscriptionDataService.getByUserAndSubject(account.getId(), subjectId)
                .map(this::toDto)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Subject not found with id: " + subjectId));
    }

    @Override
    public void unsubscribe(UserProfile user, long subjectId) throws MathtexpediaUnauthorizedException {
        logger.info("User {} unsubscribing from subject {}", user.getId(), subjectId);

        UserAccount account = userAccountService.getOrProvision(user);
        subscriptionDataService.remove(account.getId(), subjectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SubjectSubscriptionDto> subscribeToCourse(UserProfile user, long degreeId, int year, Integer semester)
            throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaInvalidException {
        logger.info("User {} subscribing to degree {} year {} (semester: {})", user.getId(), degreeId, year, semester);

        UserAccount account = userAccountService.getOrProvision(user);
        validateCourse(degreeId, year, semester);

        try {
            subscriptionDataService.addAll(account.getId(), degreeId, year, semester);
            courseSubscriptionDataService.add(account.getId(), degreeId, year, semester);
        } catch (PersistenceException e) {
            // El grado se ha borrado justo entre la comprobación y el alta: la FK lo impide
            throw new MathtexpediaNotFoundException("Degree not found with id: " + degreeId);
        }

        return subscriptionDataService.getByUserAndCourse(account.getId(), degreeId, year, semester)
                .stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unsubscribeFromCourse(UserProfile user, long degreeId, int year, Integer semester)
            throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaInvalidException {
        logger.info("User {} unsubscribing from degree {} year {} (semester: {})", user.getId(), degreeId, year, semester);

        UserAccount account = userAccountService.getOrProvision(user);
        validateCourse(degreeId, year, semester);

        subscriptionDataService.removeAll(account.getId(), degreeId, year, semester);
        courseSubscriptionDataService.remove(account.getId(), degreeId, year, semester);
    }

    /** 404 si el grado no existe; 400 si el año está fuera de 1..años del grado o el cuatrimestre no es 1 ni 2. */
    private void validateCourse(long degreeId, int year, Integer semester)
            throws MathtexpediaNotFoundException, MathtexpediaInvalidException {
        Degree degree = degreeDataService.getById(degreeId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Degree not found with id: " + degreeId));

        if (year < 1 || year > degree.getYears())
            throw new MathtexpediaInvalidException("Year " + year + " is out of range for degree " + degreeId + " (1.." + degree.getYears() + ")");
        if (semester != null && semester != 1 && semester != 2)
            throw new MathtexpediaInvalidException("Semester must be 1 or 2");
    }

    private SubjectSubscriptionDto toDto(SubjectSubscription subscription) {
        return new SubjectSubscriptionDto(subjectMapper.toDto(subscription.getSubject()), subscription.getCreatedAt());
    }

    private CourseSubscriptionDto toDto(CourseSubscription subscription) {
        return new CourseSubscriptionDto(degreeMapper.toDto(subscription.getDegree()), subscription.getYear(),
                subscription.getSemester(), subscription.getCreatedAt());
    }
}
