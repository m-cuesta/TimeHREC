/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.dao.impl;

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import timesoft.dao.EmpleadoDAO;
import timesoft.model.CriteriosEmpleado;
import timesoft.model.Empleado;

/**
 *
 * @author Camilo
 */
@Repository
public class EmpleadoDAOImpl implements EmpleadoDAO {
    

    @Autowired	
    SessionFactory sessionFactory;    

    @Transactional(readOnly = true)
    public List<Empleado> findEmployees(int maxResults) {
        Session session = sessionFactory.getCurrentSession();
        Criteria hbCriteria = session.createCriteria( Empleado.class );
        hbCriteria.setMaxResults( maxResults );
        hbCriteria.addOrder( Order.desc("strpkIdentificacion"));
        return hbCriteria.list();
    }

    @Transactional(readOnly=true)
    public List<Empleado> find(CriteriosEmpleado criteria) {
        Session session = sessionFactory.getCurrentSession();
        Criteria hbCriteria = session.createCriteria( Empleado.class );
        if ( criteria.getPernr() != null )
            hbCriteria.add(Restrictions.eq("strcodigo", criteria.getPernr()));
        return hbCriteria.list();
    }
    
}
