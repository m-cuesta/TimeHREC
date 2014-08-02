/*
 * CriteriosSubdivisionCorreo.java
 *
 * Created on Sat Dec 04 23:12:34 COT 2010
 * by DaoGen2
 * Author: Camilo Cuesta
 *
 */

package timesoft.model;
import com.inga.utils.DateRange;
import com.inga.utils.SqlClauseHelper;

/** Models a CriteriosSubdivisionCorreo
 *
 */
@SuppressWarnings("serial")
public class CriteriosSubdivisionCorreo implements java.io.Serializable {

   private java.lang.String subdivision;
   private java.lang.String correo;

    /** Creates a new instance of CriteriosSubdivisionCorreo*/
    public CriteriosSubdivisionCorreo() {
        subdivision = null;
        correo = null;
    }

    @Override
    public String toString() {
    return 
        " subdivision=" + getSubdivision() + 
        " correo=" + getCorreo()
        ;
    }

    public String getCriteriaStr() {
        SqlClauseHelper sh = new SqlClauseHelper();

        if ( getSubdivision() != null )
            sh.append("&", "subdivision=" + String.valueOf(getSubdivision()) );
        if ( getCorreo() != null )
            sh.append("&", "correo=" + String.valueOf(getCorreo()) );

        String cadena = sh.toString();
        if ( cadena.length() >  0 )
            cadena = "&" + cadena;

        return cadena;
    }

    public java.lang.String getSubdivision() {
        return subdivision;
    }

    public void setSubdivision(java.lang.String value ) {
        subdivision = value;
    }

    public java.lang.String getCorreo() {
        return correo;
    }

    public void setCorreo(java.lang.String value ) {
        correo = value;
    }

}