/*
 * SubdivisionCorreo.java
 *
 * Created on Sat Dec 04 23:12:34 COT 2010
 * by DaoGen2
 * Author: Camilo Cuesta
 *
 */

package timesoft.model;

import com.inga.utils.SigarUtils;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/** Models a SubdivisionCorreo
 *
 */
@Entity
@Table(name="SubdivisionCorreo")
public class SubdivisionCorreo implements java.io.Serializable {

    @EmbeddedId
    public SubdivisionCorreoPK pk = new SubdivisionCorreoPK();

   
    /** Creates a new instance of SubdivisionCorreo*/
    public SubdivisionCorreo() {
    }

    @Override
    public String toString() {
        return pk.getSubdivision() + " " + pk.getCorreo();
    }        
    
}




