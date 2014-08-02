/*
 * FamiliaNovedadDAO.java
 *
 * Created on Thu Sep 10 13:47:47 COT 2009
 * by DaoGen2
 * Author: Camilo Cuesta
 *
 */

package timesoft.dao.impl;

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import timesoft.model.Novedad;
import timesoft.dao.NovedadDAO;
import timesoft.model.Criterios;
import timesoft.model.Registro;

/** Models a FamiliaNovedadDAO
 *
 */
@Repository
public class FamiliaNovedadDAO extends FamiliaBasicDAO implements NovedadDAO {
    

    @Transactional
    @SuppressWarnings("unchecked")
    @Override
    public List<Registro> find(Criterios criteria, int firstResult, int maxResults ) {
        Criteria hbCriteria = getCriteria(criteria,firstResult,maxResults,true);
        return hbCriteria.list();
    }
    
    private Criteria getCriteria(Criterios criteria, Integer firstResult, Integer maxResults, boolean doOrder) {
        Session session = sessionFactory.getCurrentSession();
        Criteria hbCriteria = getHibernateCriteria(Novedad.class, criteria, session);
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

                // Si es begda, entonces también ordene por beguz, que es la hora
                if ( criteria.getOrderBy().equalsIgnoreCase("begda") )
                {
                    if ( descOrder )
                        hbCriteria.addOrder( Order.desc("beguz") );
                    else
                        hbCriteria.addOrder( Order.asc("beguz") );
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
        return super.count(Novedad.class, criteria);
     }
    
    @Override
    @Transactional
    public int update(Registro registro) {
        return super.update(registro);
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