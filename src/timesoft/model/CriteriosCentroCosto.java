/*
 * CriteriosCentroCosto.java
 *
 * Created on Thu Sep 03 15:59:43 COT 2009
 * by DaoGen2
 * Author: Camilo Cuesta
 *
 */

package timesoft.model;
import com.inga.utils.DateRange;
import com.inga.utils.SqlClauseHelper;

/** Models a CriteriosCentroCosto
 *
 */
@SuppressWarnings("serial")
public class CriteriosCentroCosto implements java.io.Serializable {

   private java.lang.String kostl;
   private java.lang.String desccc;
   private java.lang.String estado;

    /** Creates a new instance of CriteriosCentroCosto*/
    public CriteriosCentroCosto() {
        kostl = null;
        desccc = null;
        estado = null;
    }

    @Override
    public String toString() {
    return 
        " kostl=" + getKostl() + 
        " desccc=" + getDesccc() + 
        " estado=" + getEstado()
        ;
    }

    public String getCriteriaStr() {
        SqlClauseHelper sh = new SqlClauseHelper();

        if ( getKostl() != null )
            sh.append("&", "kostl=" + String.valueOf(getKostl()) );
        if ( getDesccc() != null )
            sh.append("&", "desccc=" + String.valueOf(getDesccc()) );
        if ( getEstado() != null )
            sh.append("&", "estado=" + String.valueOf(getEstado()) );

        String cadena = sh.toString();
        if ( cadena.length() >  0 )
            cadena = "&" + cadena;

        return cadena;
    }

    public java.lang.String getKostl() {
        return kostl;
    }

    public void setKostl(java.lang.String value ) {
        kostl = value;
    }

    public java.lang.String getDesccc() {
        return desccc;
    }

    public void setDesccc(java.lang.String value ) {
        desccc = value;
    }

    public java.lang.String getEstado() {
        return estado;
    }

    public void setEstado(java.lang.String value ) {
        estado = value;
    }

}