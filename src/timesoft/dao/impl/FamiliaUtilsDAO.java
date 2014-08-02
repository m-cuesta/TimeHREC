/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timesoft.dao.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import timesoft.dao.UtilDAO;
import timesoft.model.Empleado;
import timesoft.model.IdRange;
import timesoft.model.Registro;

/**
 *
 * @author Usuario
 */
@Repository
public class FamiliaUtilsDAO implements UtilDAO {
    
    @Autowired	
    SessionFactory sessionFactory;    
    

    public FamiliaUtilsDAO() {
    }

    @Transactional
    @Override
    public List<String> getSubdivisiones(String pernr){

        List<String> subdivisiones = new ArrayList<String>();
        try
        {
            String pernrFormateado = String.valueOf(Integer.parseInt(pernr));
            
            Session session = sessionFactory.getCurrentSession();
            Criteria findSubs = session.createCriteria(Empleado.class);
            findSubs.add( Restrictions.eq("strcodigo", pernrFormateado));
            findSubs.setProjection( Projections.property("intfkciudad"));
            subdivisiones.addAll( findSubs.list() );
        
        }
        catch ( Exception e )
        {
            
        }

        return subdivisiones;
        


    }

    @Transactional
    @Override
    public List<String> listSubdivisiones() {

        Session session = sessionFactory.getCurrentSession();
        Criteria findSubs = session.createCriteria( Empleado.class);
        findSubs.setProjection( Projections.distinct(Projections.property("intfkciudad")));
        List<String> subdivisiones = findSubs.list();
        Set<String> subs = new LinkedHashSet<String>();
        for (String sub : subdivisiones )
            if ( sub != null )
                subs.add( sub );
        List<String> subsSinNulos = new ArrayList<String>(subs);

        return subsSinNulos;
        
    }
    
    
    
    public static void parse(String entrada, List<String> ids, List<IdRange> rangos ){


        String[] t1s = entrada.split(",|;");
        for( String t1 : t1s )
        {
            t1 = t1.trim();
            if ( t1 != null && !t1.isEmpty() )
            {
                String[] t2s = t1.split("-");

                if ( t2s.length == 1 )
                {
                    Integer item = Integer.parseInt( t2s[0] );
                    ids.add( String.valueOf(item) );
                }
                else if ( t2s.length > 1)
                {
                    Integer desde = Integer.parseInt( t2s[0] );
                    Integer hasta = Integer.parseInt( t2s[t2s.length-1] );
                    IdRange r = new IdRange(desde,hasta);
                    rangos.add( r );
                }
            }
        }
    }
    
    public static List<Integer> stringListToInteger(List<String> items) {
        List<Integer> enteros = new ArrayList<Integer>();
        for ( String item : items )
        {
            enteros.add( Integer.parseInt(item) );
        }
        return enteros;
    }

   @Override
    public void create(Registro r) {
        HibernateTemplate ht = new HibernateTemplate(sessionFactory);
        ht.save(r);
    }    
    
    @Override
    public void delete(Registro r) {
        HibernateTemplate ht = new HibernateTemplate(sessionFactory);
        ht.delete(r);
    }
    
}
