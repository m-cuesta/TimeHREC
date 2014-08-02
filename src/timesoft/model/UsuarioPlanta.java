/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Manuel Camilo Cuesta
 */
@Entity
@Table(name="dbo.tblUsuariosPlantas")
public class UsuarioPlanta implements Serializable {
    
    @Id
    private UsuarioPlantaPK pk;
    
    public UsuarioPlanta() {
        
    }
    
    @Override
    public int hashCode() {
        if ( this.pk == null )
           return 0;
        else 
            return pk.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UsuarioPlanta other = (UsuarioPlanta) obj;
        
        if ( this.pk == null && other.getPk() == null )
            return true;
        
        if ( this.pk == null && other.getPk() != null )
            return false;
        
        return this.pk.equals(other.getPk());
        
    }
    
    
    public UsuarioPlanta(String usuario, String planta) {
        pk = new UsuarioPlantaPK(usuario,planta);
    }

    /**
     * @return the pk
     */
    public UsuarioPlantaPK getPk() {
        return pk;
    }

    /**
     * @param pk the pk to set
     */
    public void setPk(UsuarioPlantaPK pk) {
        this.pk = pk;
    }
    
}
