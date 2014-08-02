/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author usrconsultor19
 */
@Embeddable
public class UsuarioPlantaPK implements Serializable {
    
    @Column
    private String usuario;
    @Column 
    private String planta;
    
    public UsuarioPlantaPK() {
        
    }
    
    public UsuarioPlantaPK(String usuario, String planta) {
        this.usuario = usuario;
        this.planta = planta;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        
        if ( usuario != null )
            hash += usuario.hashCode();
        if ( planta != null )
            hash += planta.hashCode();
        
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UsuarioPlantaPK other = (UsuarioPlantaPK) obj;
        
        StringBuilder db1 = new StringBuilder();
        if ( this.usuario != null )
            db1.append( usuario );
        if ( this.planta != null )
            db1.append( planta );
        
        StringBuilder db2 = new StringBuilder();
        if ( other.getUsuario() != null )
            db2.append( other.getUsuario() );
        if ( other.getPlanta() != null )
            db2.append( other.getPlanta() );
        
        return db1.toString().equals( db2.toString() );
    }    
    
    /**
     * @return the usuario
     */
    public String getUsuario() {
        return usuario;
    }

    /**
     * @param usuario the usuario to set
     */
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    /**
     * @return the planta
     */
    public String getPlanta() {
        return planta;
    }

    /**
     * @param planta the planta to set
     */
    public void setPlanta(String planta) {
        this.planta = planta;
    }

    
}
