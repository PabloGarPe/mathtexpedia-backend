package mathtexpedia.es.api.persistence.pdf;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class PDFDao extends GenericJPADao implements PDFDataService{

    @Override
    @Transactional(readOnly = true)
    public List<PDF> getAll() {
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
    public Optional<PDF> getPDF(String pdfName) {
        logger.trace("Getting PDF with name {}", pdfName);
        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<PDF> cq = cb.createQuery(PDF.class);
        Root<PDF> root = cq.from(PDF.class);

        cq.select(root).where(cb.equal(root.get("name"), pdfName));

        return session.createQuery(cq).getResultList().stream().findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PDF> getAllForSubjectUnit(long subjectUnitId) {
        logger.trace("Getting PDFs for subject unit with id {}", subjectUnitId);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<PDF> cq = cb.createQuery(PDF.class);
        Root<PDF> root = cq.from(PDF.class);

        cq.select(root).where(cb.equal(root.get("subjectUnit").get("id"), subjectUnitId));

        return session.createQuery(cq).getResultList();
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
    public void deletePDF(PDF pdf) {
        logger.trace("Deleting PDF with name {}", pdf.getName());
        em.remove(em.contains(pdf) ? pdf : em.merge(pdf));
    }

    @Override
    @Transactional
    public PDF updatePDF(PDF pdf) {
        logger.trace("Updating PDF {}", pdf);
        return em.merge(pdf);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PDF> getPDFById(long pdfId) {
        logger.trace("Getting PDF with id {}", pdfId);
        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<PDF> cq = cb.createQuery(PDF.class);
        Root<PDF> root = cq.from(PDF.class);
        cq.select(root).where(cb.equal(root.get("id"), pdfId));
        return session.createQuery(cq).getResultList().stream().findFirst();
    }

}
