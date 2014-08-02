/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.dao.impl;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;
import timesoft.dao.UsuarioPlantaDAO;
import timesoft.model.UsuarioPlanta;

/**
 *
 * @author Manuel Camilo Cuesta
 */
@Repository
public class UsuarioPlantaDAOImpl implements UsuarioPlantaDAO {

    @Autowired
    SessionFactory sessionFactory;    

    public void create(UsuarioPlanta up) {
        if ( exists(up) )
            return;
        HibernateTemplate tp = new HibernateTemplate(sessionFactory);
        tp.save(up);
    }

    public void delete(UsuarioPlanta up) {
        if ( ! exists(up) )
            return;
        HibernateTemplate tp = new HibernateTemplate(sessionFactory);
        tp.delete(up);
    }

    public boolean exists(UsuarioPlanta up) {
        HibernateTemplate tp = new HibernateTemplate(sessionFactory);
        UsuarioPlanta retrieved = tp.get(UsuarioPlanta.class, up.getPk());
        return retrieved != null;
    }
    
}
