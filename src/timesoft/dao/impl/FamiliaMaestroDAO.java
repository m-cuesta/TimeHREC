/*
 * FamiliaMaestroDAO.java
 *
 * Created on Fri Aug 28 14:15:09 COT 2009
 * by DaoGen2
 * Author: Camilo Cuesta
 *
 */

package timesoft.dao.impl;

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import timesoft.model.Maestro;
import timesoft.dao.MaestroDAO;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Models a FamiliaMaestroDAO
 *
 */
@Repository
public class FamiliaMaestroDAO implements MaestroDAO {

    @Autowired	
    SessionFactory sessionFactory;    

    @Override
    @Transactional
    public Integer create(Maestro registro) {
        HibernateTemplate tp = new HibernateTemplate( sessionFactory );
        tp.save( registro );
        return 1;
    }

    @Transactional
    public int clear() {
        Session session = sessionFactory.getCurrentSession();
        Query deleteAll = session.createQuery("delete from Maestro");
        return deleteAll.executeUpdate();
    }

    @Override
    @Transactional( readOnly=true)
    public int count() {
        Session session = sessionFactory.getCurrentSession();
        Criteria crit = session.createCriteria(Maestro.class);
        crit.setProjection( Projections.rowCount() );
        Integer rowCount = (Integer) crit.uniqueResult();
        return rowCount;
    }

    @Override
    @Transactional( readOnly=true)
    public List<String> listPernrs() {
        Session session = sessionFactory.getCurrentSession();
        Criteria crit = session.createCriteria(Maestro.class);
        crit.setProjection( Projections.property("pernr") );
        return crit.list();
    }

    @Override
    public void delete(Maestro m) {
        HibernateTemplate tp = new HibernateTemplate(sessionFactory);
        tp.delete(m);
    }

    public Maestro get(String pernr) {
        HibernateTemplate tp = new HibernateTemplate(sessionFactory);
        return tp.get(Maestro.class, pernr);
    }

}