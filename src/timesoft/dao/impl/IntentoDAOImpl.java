/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.dao.impl;


import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import timesoft.dao.IntentoDAO;
import timesoft.model.CriteriosIntento;
import timesoft.model.Intento;

/**
 *
 * <p> 2011.05.05 Manuel C. Cuesta
 * @author Manuel C. Cuesta
 */
@Repository
@Transactional( propagation=Propagation.REQUIRES_NEW )
public class IntentoDAOImpl implements IntentoDAO {
    
    @Autowired	
    SessionFactory sessionFactory;    

    public Long startTry(String firma, int tipoActividad) {
        Intento in = new Intento();
        in.setFirma(firma);
        in.setTipoActividad(tipoActividad);
        Timestamp now = new Timestamp( new Date().getTime() );
        in.setCreado( now );
        in.setModificado( now );
        in.setObservacion("Iniciando");
        in.setEstado('I');
        HibernateTemplate tp = new HibernateTemplate( sessionFactory );
        Long id = (Long) tp.save(in);
        
        return id;
        
    }

    public void followUp(Long id, String observacion) {
        HibernateTemplate tp = new HibernateTemplate( sessionFactory );
        Intento intento = tp.get( Intento.class, id );
        intento.setObservacion(observacion);
        intento.setEstado('I');
        Timestamp now = new Timestamp( new Date().getTime() );
        intento.setModificado( now );
        
        tp.update(intento);
    }

    public void summary(Long id, boolean success, String observacion) {
        HibernateTemplate tp = new HibernateTemplate( sessionFactory );
        Intento intento = tp.get( Intento.class, id );
        Timestamp now = new Timestamp( new Date().getTime() );
        intento.setModificado( now );
        
        intento.setObservacion( observacion );
        if ( success )
            intento.setEstado('S');
        else
            intento.setEstado('E');
        tp.update(intento);
    }

    public void summary(Long id, boolean success) {
        HibernateTemplate tp = new HibernateTemplate( sessionFactory );
        Intento intento = tp.get( Intento.class, id );
        Timestamp now = new Timestamp( new Date().getTime() );
        intento.setModificado( now );
        
        if ( success )
            intento.setEstado('S');
        else
            intento.setEstado('E');
        tp.update(intento);
    }

    public void addIntentados(Long id, int registrosIntentados) {
        HibernateTemplate tp = new HibernateTemplate( sessionFactory );
        Intento intento = tp.get(Intento.class, id);
        intento.setRegistrosIntentados( intento.getRegistrosIntentados() + registrosIntentados );
        tp.update(intento);
    }

    public void addProcesados(Long id, int registrosProcesados) {
        HibernateTemplate tp = new HibernateTemplate( sessionFactory );
        Intento intento = tp.get(Intento.class, id);
        intento.setRegistrosProcesados( intento.getRegistrosProcesados() + registrosProcesados );
        tp.update(intento);
    }

    public void addConError(Long id, int registrosConError) {
        HibernateTemplate tp = new HibernateTemplate( sessionFactory );
        Intento intento = tp.get(Intento.class, id);
        intento.setRegistrosConError( intento.getRegistrosConError() + registrosConError );
        tp.update(intento);
    }

    public int count(CriteriosIntento criteria) {
        Session session = sessionFactory.getCurrentSession();
        Criteria crit = this.getHibernateCriteria(criteria, session);
        crit.setProjection( Projections.rowCount() );
        Integer rowCount = (Integer) crit.uniqueResult();
        return rowCount;
     }

    public List<Intento> find(CriteriosIntento criteria, int firstResult, int maxResults ) {
        
        Session session = sessionFactory.getCurrentSession();
        Criteria hbCriteria = this.getHibernateCriteria(criteria, session);

        
        // Aquí puede especificar el ordenamiento de los registros
        if ( criteria.getOrderBy() != null )
        {
            boolean descOrder = true;
            if( criteria.getOrderDirection() != null ) 
                descOrder = criteria.getOrderDirection().regionMatches(true, 0, "des", 0, 2);
            
            if ( descOrder )
                hbCriteria.addOrder( Order.desc(criteria.getOrderBy()) );
            else
                hbCriteria.addOrder( Order.asc(criteria.getOrderBy()) );

        }
        else
                hbCriteria.addOrder( Order.desc("creado") );
        
        hbCriteria.setFirstResult( firstResult );
        hbCriteria.setMaxResults( maxResults );
        
        return hbCriteria.list();
    }
    
    private Criteria getHibernateCriteria(CriteriosIntento criteria, Session session) {
        
        Criteria hbCriteria = session.createCriteria( Intento.class );

        // creado
        if ( criteria.getCreado() != null )
        {
           if ( criteria.getCreado().getInicio() != null )
               hbCriteria.add( Restrictions.ge("creado", criteria.getCreado().getInicio() ) );
           if ( criteria.getCreado().getFin() != null )
               hbCriteria.add( Restrictions.le("creado", criteria.getCreado().getFin() ) );
        }

        // modificado
        if ( criteria.getModificado() != null )
        {
           if ( criteria.getModificado().getInicio() != null )
               hbCriteria.add( Restrictions.ge("modificado", criteria.getModificado().getInicio() ) );
           if ( criteria.getModificado().getFin() != null )
               hbCriteria.add( Restrictions.le("modificado", criteria.getModificado().getFin() ) );
        }
        
        if ( criteria.getEstado() != null )
            hbCriteria.add( Restrictions.eq("estado", criteria.getEstado() ));
        
        if ( criteria.getFirma() != null )
            hbCriteria.add( Restrictions.eq("firma", criteria.getFirma() ));
        
        if ( criteria.getTipoActividad() != null )
            hbCriteria.add( Restrictions.eq("tipoActividad", criteria.getTipoActividad() )) ;
        
        
        return hbCriteria;
        
    }
    
    
    
}
