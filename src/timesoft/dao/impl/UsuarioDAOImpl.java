/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.dao.impl;

import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;
import timesoft.dao.UsuarioDAO;
import timesoft.model.Usuario;

/**
 *
 * @author Camilo
 */
@Repository
public class UsuarioDAOImpl implements UsuarioDAO {
    
    @Autowired
    SessionFactory sessionFactory;
    

    public List<Usuario> list() {
        HibernateTemplate tp = new HibernateTemplate(sessionFactory);
        return tp.find(" from Usuario");
    }

    public void create(Usuario usr) {
        HibernateTemplate tp = new HibernateTemplate(sessionFactory);
        tp.save(usr);
    }

    public Usuario get(String login) {
        HibernateTemplate tp = new HibernateTemplate(sessionFactory);
        return (Usuario) tp.get(Usuario.class, login);
    }

    public void update(Usuario usr) {
        HibernateTemplate tp = new HibernateTemplate(sessionFactory);
        tp.update( usr );
    }

    public void delete(String login) {
        HibernateTemplate tp = new HibernateTemplate(sessionFactory);
        tp.delete( get(login) );
    }
    
}
