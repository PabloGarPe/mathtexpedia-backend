package mathtexpedia.es.api.persistence.pdf;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PDFDao extends GenericJPADao implements PDFDataService{

    @Override
    public List<PDF> getPDFWithNoLink() {
        logger.trace("Getting PDF with no link");

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<PDF> cq = cb.createQuery(PDF.class);
        Root<PDF> root = cq.from(PDF.class);

        cq.select(cb.construct(
                PDF.class,
                root.get("id"),
                root.get("name"),
                root.get("lastTimeEdited"),
                root.get("description"),
                root.get("tag")
        ));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public List<PDF> getPDFs() {
        logger.trace("Getting PDFs");
        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<PDF> cq = cb.createQuery(PDF.class);
        Root<PDF> root = cq.from(PDF.class);
        cq.select(root);

        return session.createQuery(cq).getResultList();
    }

    @Override
    public PDF getPDF(String pdfName) {
        logger.trace("Getting PDF with name {}", pdfName);
        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<PDF> cq = cb.createQuery(PDF.class);
        Root<PDF> root = cq.from(PDF.class);
        cq.select(root).where(cb.equal(root.get("name"), pdfName));

        return session.createQuery(cq).getSingleResult();
    }

}
