/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilerias.timecore;

import java.util.Comparator;
import timesoft.model.Maestro;

/**
 *
 * @author Camilo
 */
public class ComparadorMaestro implements Comparator<Maestro> {

    public int compare(Maestro o1, Maestro o2) {
        if ( o1 == null && o2 == null) 
            return 0;
        if ( o1 != null && o2 == null)
            return 1;
        if ( o1 == null && o2 != null )
            return -1;
        if ( o1 != null && o2 != null )
        {
            String e1 = o1.getEstado();
            String e2 = o2.getEstado();

            if ( e1 != null && e2 == null )
                return 1;
            else if ( e1 == null && e2 != null )
                return -1;
            else 
            {
                if ( e1.equals(e2)) 
                {

                    String p1 = o1.getPernr();
                    String p2 = o2.getPernr();

                    if ( p1 == null && p2 == null )
                        return 0;
                    else if ( p1 == null && p2 != null )
                        return -1;
                    else if ( p1 != null && p2 == null ) 
                        return 1;
                    else 
                    {
                        return p1.compareTo(p2);
                    }
                    
                }
                else
                {
                   if ( "".equals(e1) )   
                       return -1;
                           
                   else if ( "E".equals(e1)  )
                       return 1;
                   else if ( "S".equals(e1) )
                       return -1;
                   else 
                       return e1.compareTo(e2);
                   
                }
                    
            }
                
            
        }
        return 0;
        
    }
    
}