/*
 * FamiliaSubdivisionCorreoDAO.java
 *
 * Created on Wed Oct 07 15:34:49 COT 2009
 * by DaoGen2
 * Author: Camilo Cuesta
 *
 */

package timesoft.dao.impl;

import com.inga.exception.RegistroNoExisteException;
import com.inga.utils.SigarUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import timesoft.dao.SubdivisionCorreoDAO;
import timesoft.model.CriteriosSubdivisionCorreo;
import timesoft.model.SubdivisionCorreo;

/** Models a FamiliaSubdivisionCorreoDAO
 *
 */
@Repository
public class FamiliaSubdivisionCorreoDAO implements SubdivisionCorreoDAO {
    
    @Autowired	
    SessionFactory sessionFactory;    
    

    protected static SimpleDateFormat _df = new SimpleDateFormat( SigarUtils.FECHA4 );

    protected static String tableName = "SubdivisionCorreo";


    @Transactional
    @SuppressWarnings("unchecked")
    @Override
    public List<SubdivisionCorreo> find(CriteriosSubdivisionCorreo criteria) {
        
        Session session = sessionFactory.getCurrentSession();
        Criteria hbCriteria = session.createCriteria(SubdivisionCorreo.class);
        List<SubdivisionCorreo> results =  hbCriteria.list();
        List<SubdivisionCorreo> filter = new ArrayList<SubdivisionCorreo>();
        for ( SubdivisionCorreo s : results ) 
        {
            if ( criteria.getSubdivision() == null && criteria.getCorreo() == null )
            {
                filter.add( s );
                continue;
            }
            
            if ( criteria.getSubdivision() != null )
                if ( s.pk.getSubdivision().equals(criteria.getSubdivision()))
                    filter.add(s);
            if ( criteria.getCorreo() != null )
                if ( s.pk.getCorreo().equals(criteria.getCorreo()))
                    filter.add(s);
            
        }
        return filter;
     }

    @Override
    public SubdivisionCorreo get( java.lang.String subdivision, java.lang.String correo ) throws RegistroNoExisteException {
        HibernateTemplate tp = new HibernateTemplate(sessionFactory);
        SubdivisionCorreo s = new SubdivisionCorreo();
        s.pk.setSubdivision(subdivision);
        s.pk.setCorreo(correo);
        return tp.get( SubdivisionCorreo.class , s.pk );
    }

    @Override
    public Integer create(SubdivisionCorreo registro) {
        HibernateTemplate tp = new HibernateTemplate(sessionFactory);
        tp.save( registro );
        return 1;
    }

    @Override
    public int delete( java.lang.String subdivision, java.lang.String correo ) {
        HibernateTemplate tp = new HibernateTemplate(sessionFactory);
        SubdivisionCorreo registro = new SubdivisionCorreo();
        registro.pk.setSubdivision(subdivision);
        registro.pk.setCorreo(correo);
        tp.delete( registro );
        return 1;
    }

 
}