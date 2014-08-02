/*
 * FamiliaCiudadDAO.java
 *
 * Created on Thu Oct 08 10:16:11 COT 2009
 * by DaoGen2
 * Author: Camilo Cuesta
 *
 */

package timesoft.dao.impl;

import com.inga.exception.RegistroNoExisteException;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;
import timesoft.model.Ciudad;
import timesoft.dao.CiudadDAO;

/** Models a FamiliaCiudadDAO
 *
 */
@Repository
public class FamiliaCiudadDAO implements CiudadDAO {
    
    @Autowired	
    SessionFactory sessionFactory;    

    @Override
    public Ciudad get( java.lang.String intpkidciudad ) throws RegistroNoExisteException {
        HibernateTemplate ht = new HibernateTemplate(sessionFactory);
        Ciudad reg = ht.get( Ciudad.class, intpkidciudad, LockMode.NONE);
        if ( reg == null )
            throw new RegistroNoExisteException( intpkidciudad );
        return reg;
    }


}