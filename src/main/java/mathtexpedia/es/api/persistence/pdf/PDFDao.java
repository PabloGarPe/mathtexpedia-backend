package mathtexpedia.es.api.persistence.pdf;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class PDFDao extends GenericJPADao implements PDFDataService{

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public PDF getPDF(String pdfName) {
        logger.trace("Getting PDF with name {}", pdfName);
        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<PDF> cq = cb.createQuery(PDF.class);
        Root<PDF> root = cq.from(PDF.class);
        cq.select(root).where(cb.equal(root.get("name"), pdfName));

        return session.createQuery(cq).getSingleResult();
    }

    @Override
    @Transactional
    public PDF createPDF(PDF pdf) {
        logger.trace("Creating PDF {}", pdf);
        em.persist(pdf);
        return pdf;

    }

    @Override
    @Transactional
    public void deletePDF(String pdfName) {
        logger.trace("Deleting PDF with name {}", pdfName);
        PDF pdf =  getPDF(pdfName);
        em.remove(pdf);
    }

}
