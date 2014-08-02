/*
 * CriteriosMaestro.java
 *
 * Created on Fri Aug 28 14:15:09 COT 2009
 * by DaoGen2
 * Author: Camilo Cuesta
 *
 */

package timesoft.model;
import com.inga.utils.DateRange;
import com.inga.utils.SqlClauseHelper;

/** Models a CriteriosMaestro
 *
 */
@SuppressWarnings("serial")
public class CriteriosMaestro implements java.io.Serializable {

   private java.lang.String pernr;
   private java.lang.String vorna;
   private java.lang.String name2;
   private java.lang.String nachn;
   private java.lang.String nach2;
   private java.lang.String kostl;
   private java.lang.String desccc;
   private java.lang.String wrks;
   private java.lang.String descdiv;
   private java.lang.String btrtl;
   private java.lang.String descsubdiv;
   private java.lang.String tipoempl;
   private java.lang.String numdocide;
   private java.lang.String bukrs;
   private java.lang.String usrid;
   private java.lang.String status;
   private java.lang.String codempleadouno;
   private java.lang.String codempleadodos;
   private java.lang.String superusuario;

    /** Creates a new instance of CriteriosMaestro*/
    public CriteriosMaestro() {
        pernr = null;
        vorna = null;
        name2 = null;
        nachn = null;
        nach2 = null;
        kostl = null;
        desccc = null;
        wrks = null;
        descdiv = null;
        btrtl = null;
        descsubdiv = null;
        tipoempl = null;
        numdocide = null;
        bukrs = null;
        usrid = null;
        status = null;
        codempleadouno = null;
        codempleadodos = null;
        superusuario = null;
    }

    @Override
    public String toString() {
    return 
        " pernr=" + getPernr() + 
        " vorna=" + getVorna() + 
        " name2=" + getName2() + 
        " nachn=" + getNachn() + 
        " nach2=" + getNach2() + 
        " kostl=" + getKostl() + 
        " desccc=" + getDesccc() + 
        " wrks=" + getWrks() + 
        " descdiv=" + getDescdiv() + 
        " btrtl=" + getBtrtl() + 
        " descsubdiv=" + getDescsubdiv() + 
        " tipoempl=" + getTipoempl() + 
        " numdocide=" + getNumdocide() + 
        " bukrs=" + getBukrs() + 
        " usrid=" + getUsrid() + 
        " status=" + getStatus() + 
        " codempleadouno=" + getCodempleadouno() + 
        " codempleadodos=" + getCodempleadodos() + 
        " superusuario=" + getSuperusuario()
        ;
    }

    public String getCriteriaStr() {
        SqlClauseHelper sh = new SqlClauseHelper();

        if ( getPernr() != null )
            sh.append("&", "pernr=" + String.valueOf(getPernr()) );
        if ( getVorna() != null )
            sh.append("&", "vorna=" + String.valueOf(getVorna()) );
        if ( getName2() != null )
            sh.append("&", "name2=" + String.valueOf(getName2()) );
        if ( getNachn() != null )
            sh.append("&", "nachn=" + String.valueOf(getNachn()) );
        if ( getNach2() != null )
            sh.append("&", "nach2=" + String.valueOf(getNach2()) );
        if ( getKostl() != null )
            sh.append("&", "kostl=" + String.valueOf(getKostl()) );
        if ( getDesccc() != null )
            sh.append("&", "desccc=" + String.valueOf(getDesccc()) );
        if ( getWrks() != null )
            sh.append("&", "wrks=" + String.valueOf(getWrks()) );
        if ( getDescdiv() != null )
            sh.append("&", "descdiv=" + String.valueOf(getDescdiv()) );
        if ( getBtrtl() != null )
            sh.append("&", "btrtl=" + String.valueOf(getBtrtl()) );
        if ( getDescsubdiv() != null )
            sh.append("&", "descsubdiv=" + String.valueOf(getDescsubdiv()) );
        if ( getTipoempl() != null )
            sh.append("&", "tipoempl=" + String.valueOf(getTipoempl()) );
        if ( getNumdocide() != null )
            sh.append("&", "numdocide=" + String.valueOf(getNumdocide()) );
        if ( getBukrs() != null )
            sh.append("&", "bukrs=" + String.valueOf(getBukrs()) );
        if ( getUsrid() != null )
            sh.append("&", "usrid=" + String.valueOf(getUsrid()) );
        if ( getStatus() != null )
            sh.append("&", "status=" + String.valueOf(getStatus()) );
        if ( getCodempleadouno() != null )
            sh.append("&", "codempleadouno=" + String.valueOf(getCodempleadouno()) );
        if ( getCodempleadodos() != null )
            sh.append("&", "codempleadodos=" + String.valueOf(getCodempleadodos()) );
        if ( getSuperusuario() != null )
            sh.append("&", "superusuario=" + String.valueOf(getSuperusuario()) );

        String cadena = sh.toString();
        if ( cadena.length() >  0 )
            cadena = "&" + cadena;

        return cadena;
    }

    public java.lang.String getPernr() {
        return pernr;
    }

    public void setPernr(java.lang.String value ) {
        pernr = value;
    }

    public java.lang.String getVorna() {
        return vorna;
    }

    public void setVorna(java.lang.String value ) {
        vorna = value;
    }

    public java.lang.String getName2() {
        return name2;
    }

    public void setName2(java.lang.String value ) {
        name2 = value;
    }

    public java.lang.String getNachn() {
        return nachn;
    }

    public void setNachn(java.lang.String value ) {
        nachn = value;
    }

    public java.lang.String getNach2() {
        return nach2;
    }

    public void setNach2(java.lang.String value ) {
        nach2 = value;
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

    public java.lang.String getWrks() {
        return wrks;
    }

    public void setWrks(java.lang.String value ) {
        wrks = value;
    }

    public java.lang.String getDescdiv() {
        return descdiv;
    }

    public void setDescdiv(java.lang.String value ) {
        descdiv = value;
    }

    public java.lang.String getBtrtl() {
        return btrtl;
    }

    public void setBtrtl(java.lang.String value ) {
        btrtl = value;
    }

    public java.lang.String getDescsubdiv() {
        return descsubdiv;
    }

    public void setDescsubdiv(java.lang.String value ) {
        descsubdiv = value;
    }

    public java.lang.String getTipoempl() {
        return tipoempl;
    }

    public void setTipoempl(java.lang.String value ) {
        tipoempl = value;
    }

    public java.lang.String getNumdocide() {
        return numdocide;
    }

    public void setNumdocide(java.lang.String value ) {
        numdocide = value;
    }

    public java.lang.String getBukrs() {
        return bukrs;
    }

    public void setBukrs(java.lang.String value ) {
        bukrs = value;
    }

    public java.lang.String getUsrid() {
        return usrid;
    }

    public void setUsrid(java.lang.String value ) {
        usrid = value;
    }

    public java.lang.String getStatus() {
        return status;
    }

    public void setStatus(java.lang.String value ) {
        status = value;
    }

    public java.lang.String getCodempleadouno() {
        return codempleadouno;
    }

    public void setCodempleadouno(java.lang.String value ) {
        codempleadouno = value;
    }

    public java.lang.String getCodempleadodos() {
        return codempleadodos;
    }

    public void setCodempleadodos(java.lang.String value ) {
        codempleadodos = value;
    }

    public java.lang.String getSuperusuario() {
        return superusuario;
    }

    public void setSuperusuario(java.lang.String value ) {
        superusuario = value;
    }

}