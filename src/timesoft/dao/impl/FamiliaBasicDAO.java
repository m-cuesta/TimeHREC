/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.dao.impl;

import com.inga.utils.SigarUtils;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import timesoft.Constantes;
import timesoft.dao.RegistroDAO;
import timesoft.model.Criterios;
import timesoft.model.IdRange;
import timesoft.model.Novedad;
import timesoft.model.NovedadPaquete;
import timesoft.model.Registro;

/**
 *
 * @author Camilo
 */
public abstract class FamiliaBasicDAO implements RegistroDAO {
    
    private static Logger log = Logger.getLogger(FamiliaBasicDAO.class.getName());
    
    // Ignorar estos registros al procesar nuevas:
    // F : Registros con fecha inválida
    // I : Registros marcados para ignorar
    public static String[] procesadas = new String[]{"S","E"};
    
    
    static NumberFormat nf = NumberFormat.getInstance();
    
    @Autowired	
    SessionFactory sessionFactory;    
    
    protected Criteria getHibernateCriteria(Class clase, Criterios criteria, Session session) {

        log.info( criteria );
        
        Criteria hbCriteria = session.createCriteria( clase );
        String capturaSiboBegda = "ldate";
        if ( clase.equals(Novedad.class) )
            capturaSiboBegda = "begda";

        // ID 
        Disjunction dis = getRangosRestriction(hbCriteria, criteria.getIds(), criteria.getRangosId() );
        hbCriteria.add( dis );

        // pernr
        Disjunction disPernr = getPernrRestriction(hbCriteria, criteria.getPernrs(), criteria.getRangosPernr() );
        hbCriteria.add( disPernr );

        // Subdivisión
        if ( criteria.getSubdivisiones() != null && !criteria.getSubdivisiones().isEmpty() )
            hbCriteria.add( Restrictions.in( "subdivision", criteria.getSubdivisiones()) );            

        // Operador 
        if ( criteria.getOperador() != null )
            hbCriteria.add( Restrictions.eq("operador", criteria.getOperador()));

        // Estado 
        if ( criteria.getRetorno() != null )
        {
            // Nuevas marcaciones: Reproceso en falso, y findMode en falso
            if ( Constantes.NUEVA.equals(criteria.getRetorno()))
            {
                Disjunction estadosRetorno = Restrictions.disjunction();
                estadosRetorno.add( Restrictions.isNull("retorno") );
                estadosRetorno.add( Restrictions.eq("retorno", "") );
                estadosRetorno.add( Restrictions.not( Restrictions.in( "retorno", procesadas ) ) );
                hbCriteria.add( estadosRetorno );
            }         
            else 
            {
                hbCriteria.add( Restrictions.eq("retorno", criteria.getRetorno() ));                    
            }
        }

        // Ignora los que estén marcados con F
        if ( criteria.isIgnoreFechaInvalida() )
            hbCriteria.add( Restrictions.or( Restrictions.isNull("retorno"),Restrictions.ne("retorno", "F")) );

        // Ignora los que estén marcados con I
        if ( criteria.isIgnoreMarked() )
            hbCriteria.add( Restrictions.or( Restrictions.isNull("retorno"),Restrictions.ne("retorno", "I")) );

        if ( criteria.getCntrl() != null )
        {
           if ( criteria.getCntrl().getInicio() != null )
               hbCriteria.add( Restrictions.ge("cntrl", criteria.getCntrl().getInicio() ));
           if ( criteria.getCntrl().getFin() != null )
               hbCriteria.add( Restrictions.le("cntrl", criteria.getCntrl().getFin() ));
        }

        if ( criteria.getCapturaSibo() != null )
        {
           if ( criteria.getCapturaSibo().getInicio() != null )
               hbCriteria.add( Restrictions.ge( capturaSiboBegda, Constantes._df2.format(criteria.getCapturaSibo().getInicio())));
           if ( criteria.getCapturaSibo().getFin() != null )
               hbCriteria.add( Restrictions.le( capturaSiboBegda, Constantes._df2.format(criteria.getCapturaSibo().getFin())));
        }

        if ( clase.equals(NovedadPaquete.class) )
        {
            // itipo
            if ( criteria.getTipo() != null ) 
                hbCriteria.add( Restrictions.eq("itipo", criteria.getTipo()));
        }
        
        if ( criteria.getMensaje() != null && !criteria.getMensaje().isEmpty() )
        {
            if ( Constantes.OTROS_MENSAJES.equals(criteria.getMensaje()) )
            {
                Conjunction ignorarErrores = Restrictions.conjunction();
                for ( String mensaje : Constantes.CODIGOS_DE_ERROR )
                    ignorarErrores.add( Restrictions.not(Restrictions.ilike("mensaje", mensaje, MatchMode.ANYWHERE)));
                hbCriteria.add(ignorarErrores);
            }
            else 
            {
                hbCriteria.add(Restrictions.ilike("mensaje", criteria.getMensaje(), MatchMode.ANYWHERE));
            }
        }
        
        if ( criteria.getBtrtl() != null && !criteria.getBtrtl().isEmpty() )
            hbCriteria.add(Restrictions.eq("btrtl", criteria.getBtrtl()));

        return hbCriteria;
    }
    
    protected static Disjunction getRangosRestriction( Criteria hbCriteria, List<Integer> ids, List<IdRange> rangos ) {
        
            Disjunction dis = Restrictions.disjunction();
            
            if ( ids != null && !ids.isEmpty() )
                dis.add( Restrictions.in( "id", ids) );
            
            if ( rangos != null )
                for ( IdRange r : rangos )
                    dis.add( Restrictions.and( Restrictions.ge( "id" , r.getLow()) , Restrictions.le( "id" , r.getHigh() ) ));
            
            return dis;
        
    }
    
    protected static Disjunction getPernrRestriction( Criteria hbCriteria, List<String> ids, List<IdRange> rangos ) {
        
            Disjunction dis = Restrictions.disjunction();
            
            nf.setMaximumIntegerDigits(8);
            nf.setMinimumIntegerDigits(8);
            nf.setGroupingUsed(false);
            List<String> formatedIds = new ArrayList<String>();
            
            
            if ( ids != null && !ids.isEmpty() )
            {
                for ( String id : ids )
                    formatedIds.add( nf.format(Integer.parseInt(id)) );
 
                SigarUtils.printList( formatedIds );
                
                dis.add( Restrictions.in( "pernr", formatedIds ) );
            }
            
            if ( rangos != null )
            {
                for ( IdRange r : rangos )
                {
                        dis.add( Restrictions.and( Restrictions.ge( "pernr" , nf.format(r.getLow())) , Restrictions.le( "pernr" , nf.format(r.getHigh())) ));
                }
            }
            
            return dis;
        
    }

    


    public int update(Registro registro) {
        HibernateTemplate ht = new HibernateTemplate(sessionFactory);
        registro.setCntrl( new Timestamp( System.currentTimeMillis() ) );
        ht.update( registro );
        return 1;
    }

    public int count(Class clase, Criterios criteria) {
        Session session = sessionFactory.getCurrentSession();
        Criteria crit = this.getHibernateCriteria(clase, criteria, session);
        crit.setProjection( Projections.rowCount() );
        Integer rowCount = (Integer) crit.uniqueResult();
        return rowCount;
    }

 
}
