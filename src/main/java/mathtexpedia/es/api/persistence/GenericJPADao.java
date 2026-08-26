package mathtexpedia.es.api.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenericJPADao {

    protected EntityManager em = null;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    public void setEm(EntityManager em) {
        this.em = em;
    }
}
