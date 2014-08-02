/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.model;

/**
 *
 * @author Camilo
 */
public abstract class RegistroImpl implements Registro {
    
    @Override
    public int hashCode() {
        return getId();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        Registro other = (Registro) obj;
        Integer id1 = this.getId();
        Integer id2 = other.getId();
        
        if ( id1 == null && id2 == null)
            return true;
        else if ( id1 != null && id2 == null)
            return false;
        else if ( id1 == null && id2 != null)
            return false;
        else
        {
            int i1 = this.getId();
            int i2 = other.getId();
            return i1 == i2;
        }
        
            
    }
    
}
