/*
 * CriteriosCiudad.java
 *
 * Created on Thu Oct 08 10:16:11 COT 2009
 * by DaoGen2
 * Author: Camilo Cuesta
 *
 */

package timesoft.model;
import com.inga.utils.DateRange;
import com.inga.utils.SqlClauseHelper;

/** Models a CriteriosCiudad
 *
 */
@SuppressWarnings("serial")
public class CriteriosCiudad implements java.io.Serializable {

   private java.lang.String intpkidciudad;
   private java.lang.String strdescripcionciudad;
   private DateRange dtmfechamodificado;
   private java.lang.String strloginmodificado;

    /** Creates a new instance of CriteriosCiudad*/
    public CriteriosCiudad() {
        intpkidciudad = null;
        strdescripcionciudad = null;
        dtmfechamodificado = null;
        strloginmodificado = null;
    }

    @Override
    public String toString() {
    return 
        " intpkidciudad=" + getIntpkidciudad() + 
        " strdescripcionciudad=" + getStrdescripcionciudad() + 
        " dtmfechamodificado=" + getDtmfechamodificado() + 
        " strloginmodificado=" + getStrloginmodificado()
        ;
    }

    public String getCriteriaStr() {
        SqlClauseHelper sh = new SqlClauseHelper();

        if ( getIntpkidciudad() != null )
            sh.append("&", "intpkidciudad=" + String.valueOf(getIntpkidciudad()) );
        if ( getStrdescripcionciudad() != null )
            sh.append("&", "strdescripcionciudad=" + String.valueOf(getStrdescripcionciudad()) );
        if ( getDtmfechamodificado() != null )
            sh.append("&", "dtmfechamodificado=" + String.valueOf(getDtmfechamodificado()) );
        if ( getStrloginmodificado() != null )
            sh.append("&", "strloginmodificado=" + String.valueOf(getStrloginmodificado()) );

        String cadena = sh.toString();
        if ( cadena.length() >  0 )
            cadena = "&" + cadena;

        return cadena;
    }

    public java.lang.String getIntpkidciudad() {
        return intpkidciudad;
    }

    public void setIntpkidciudad(java.lang.String value ) {
        intpkidciudad = value;
    }

    public java.lang.String getStrdescripcionciudad() {
        return strdescripcionciudad;
    }

    public void setStrdescripcionciudad(java.lang.String value ) {
        strdescripcionciudad = value;
    }

    public DateRange getDtmfechamodificado() {
        return dtmfechamodificado;
    }

    public void setDtmfechamodificado(DateRange value ) {
        dtmfechamodificado = value;
    }

    public java.lang.String getStrloginmodificado() {
        return strloginmodificado;
    }

    public void setStrloginmodificado(java.lang.String value ) {
        strloginmodificado = value;
    }

}