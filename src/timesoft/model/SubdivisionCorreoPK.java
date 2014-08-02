 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.model;

import java.io.Serializable;
import javax.persistence.Embeddable;

/**
 *
 * @author Camilo
 */
@Embeddable
public class SubdivisionCorreoPK implements Serializable {
    
    private String subdivision;
    private String correo;

    /**
     * @return the subdivision
     */
    public String getSubdivision() {
        return subdivision;
    }

    /**
     * @param subdivision the subdivision to set
     */
    public void setSubdivision(String subdivision) {
        this.subdivision = subdivision;
    }

    /**
     * @return the correo
     */
    public String getCorreo() {
        return correo;
    }

    /**
     * @param correo the correo to set
     */
    public void setCorreo(String correo) {
        this.correo = correo;
    }
    
}
