/*
 * FamiliaNovedadPaqueteDAO.java
 *
 * Created on Thu Sep 10 18:39:56 COT 2009
 * by DaoGen2
 * Author: Camilo Cuesta
 *
 */

package timesoft.dao.impl;

import timesoft.dao.NovedadPaqueteDAO;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import timesoft.model.Criterios;
import timesoft.model.NovedadPaquete;
import timesoft.model.Registro;

/** Models a FamiliaNovedadPaqueteDAO
 *
 */
@Repository
public class FamiliaNovedadPaqueteDAO extends FamiliaBasicDAO implements NovedadPaqueteDAO {

    @Transactional
    @SuppressWarnings("unchecked")
    @Override
    public List<Registro> find(Criterios criteria, int firstResult, int maxResults ) {
        Criteria hbCriteria = getCriteria(criteria,firstResult,maxResults,true);
        return hbCriteria.list();
        
     }
    
    private Criteria getCriteria(Criterios criteria, Integer firstResult, Integer maxResults, boolean doOrder) {
        Session session = sessionFactory.getCurrentSession();
        Criteria hbCriteria = this.getHibernateCriteria(NovedadPaquete.class, criteria, session);
        
        if (firstResult != null) {
            hbCriteria.setFirstResult( firstResult );
        }
        if (maxResults != null) {
            hbCriteria.setMaxResults( maxResults );
        }
        
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
                        hbCriteria.addOrder( Order.desc("lhour") );
                    else
                        hbCriteria.addOrder( Order.asc("lhour") );
                }
            }
            else
                    hbCriteria.addOrder( Order.asc("id") );        
        }
        
        return hbCriteria;
    }


    @Override
    @Transactional
    public int update(Registro registro) {
        return super.update(registro);
    }
    

    @Override
    @Transactional( readOnly=true )
    public int count(Criterios criteria) {
        return super.count(NovedadPaquete.class, criteria);
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