/*
 * FamiliaCentroCostoDAO.java
 *
 * Created on Thu Sep 03 15:59:43 COT 2009
 * by DaoGen2
 * Author: Camilo Cuesta
 *
 */

package timesoft.dao.impl;

import org.hibernate.Query;
import org.hibernate.Session;
import timesoft.model.CentroCosto;
import timesoft.dao.CentroCostoDAO;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Models a FamiliaCentroCostoDAO
 *
 */
@Repository
@Transactional
public class FamiliaCentroCostoDAO implements CentroCostoDAO {

    @Autowired	
    SessionFactory sessionFactory;    

    @Override
    public int create(CentroCosto registro) {
        HibernateTemplate tp = new HibernateTemplate( sessionFactory );
        tp.save( registro );
        return 1;
    }

    @Override
    public int clear() {
        Session session = sessionFactory.getCurrentSession();
        Query deleteAll = session.createQuery("delete from Maestro");
        return deleteAll.executeUpdate();
    }
    
}