/*
 * FamiliaMarcacionDAO.java
 *
 * Created on Thu Sep 10 10:03:08 COT 2009
 * by DaoGen2
 * Author: Camilo Cuesta
 *
 */

package timesoft.dao.impl;

import java.util.Date;
import java.util.List;
import timesoft.model.Marcacion;
import timesoft.dao.MarcacionDAO;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import timesoft.Constantes;
import timesoft.model.Criterios;
import timesoft.model.Registro;

/** Models a FamiliaMarcacionDAO
 *
 */
@Repository
public class FamiliaMarcacionDAO extends FamiliaBasicDAO implements MarcacionDAO {


    @Transactional( readOnly=true )
    @SuppressWarnings("unchecked")
    @Override
    public List<Registro> find(Criterios criteria, int firstResult, int maxResults ) {
        Criteria hbCriteria = getCriteria(criteria,firstResult,maxResults,true);
        return hbCriteria.list();
    }
    
    private Criteria getCriteria(Criterios criteria, Integer firstResult, Integer maxResults, boolean doOrder) {
        
        Session session = sessionFactory.getCurrentSession();
        Criteria hbCriteria = getHibernateCriteria(Marcacion.class, criteria, session);
        if (firstResult != null)
            hbCriteria.setFirstResult( firstResult );
        if (maxResults != null)
            hbCriteria.setMaxResults( maxResults );
        
        // Aquí puede especificar el ordenamiento de los registros
        if (doOrder) 
        {
            if ( criteria.getOrderBy() != null )
            {
                boolean descOrder = true;
                if( criteria.getOrderDirection() != null ) 
                    descOrder = criteria.getOrderDirection().regionMatches(true, 0, "des", 0, 2);

                if ( descOrder )
                    hbCriteria.addOrder( Order.desc(criteria.getOrderBy()) );
                else
                    hbCriteria.addOrder( Order.asc(criteria.getOrderBy()) );

                // Si es ldate, entonces también ordene por name2, que es la hora
                if ( criteria.getOrderBy().equalsIgnoreCase("ldate") )
                {
                    if ( descOrder )
                        hbCriteria.addOrder( Order.desc("name2") );
                    else
                        hbCriteria.addOrder( Order.asc("name2") );
                }
            }
            else
                    hbCriteria.addOrder( Order.asc("id") );
        }
        
        return hbCriteria;
        
    }
    
   @Override
    @Transactional( readOnly=true )
    public int count(Criterios criteria) {
        return super.count(Marcacion.class, criteria);
     }
    
    @Override
    @Transactional
    public int update(Registro registro) {
        return super.update(registro);
    }
    
    @Override
    @Transactional( readOnly=true )
    public List<Marcacion> getPendientes() {
        Date ahora = new Date();
        long ahoraTime = ahora.getTime();
        long haceDoceHorasTime = ahoraTime - 12L*3600L*1000L;
        Date haceDoceHoras = new Date( haceDoceHorasTime );
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria( Marcacion.class );
        criteria.add( Restrictions.eq("cntdr", "") );
        criteria.add( Restrictions.lt("cntrl", Constantes.FECHA_1950_01_01) );
        criteria.add( Restrictions.lt("fechaModificado", haceDoceHoras) );
        return criteria.list();
    }

    @Transactional( readOnly=true )
    @SuppressWarnings("unchecked")
    @Override
    public List<Integer> findOnlyIds(Criterios criteria) {
        Criteria hbCriteria = getCriteria(criteria,null,null,false);
        hbCriteria.setProjection(Projections.id());        
        return hbCriteria.list();
    }


}