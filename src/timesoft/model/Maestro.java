package timesoft.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="tblMaestrosSap")
public class Maestro
  implements Serializable
{

  /**
	 * 
	 */
	private static final long serialVersionUID = 6809372584154744485L;

  @Id
  @Column
  private String pernr;

  @Column
  private String vorna;

  @Column
  private String name2;

  @Column
  private String nachn;

  @Column
  private String nach2;

  @Column
  private String kostl;

  @Column
  private String desccc;

  @Column
  private String wrks;

  @Column
  private String descdiv;

  @Column
  private String btrtl;

  @Column
  private String descsubdiv;

  @Column
  private String tipoempl;

  @Column
  private String numdocide;

  @Column
  private String bukrs;

  @Column
  private String usrid;

  @Column
  private String status;

  @Column
  private String codempleadouno;

  @Column
  private String codempleadodos;

  @Column
  private String superusuario;

  @Column
  private String gesch;

  @Column
  private String fecin;

  @Column
  private String gbdat;

  @Column
  private String stras;

  @Column
  private String telnr;

  @Column
  private String celul;

  @Column
  private String fax;

  @Column(name="USRID_LONG")
  private String useridLong;

  
  @Column(name="useridLong")
  private String useridLong2;

  @Transient
  private String estado;
  
  public Maestro()
  {
    this.pernr = "";
    this.vorna = "";
    this.name2 = "";
    this.nachn = "";
    this.nach2 = "";
    this.kostl = "";
    this.desccc = "";
    this.wrks = "";
    this.descdiv = "";
    this.btrtl = "";
    this.descsubdiv = "";
    this.tipoempl = "";
    this.numdocide = "";
    this.bukrs = "";
    this.usrid = "";
    this.status = "";
    this.codempleadouno = "";
    this.codempleadodos = "";
    this.superusuario = "";
    this.gesch = "";
    this.fecin = "";
    this.gbdat = "";
    this.stras = "";
    this.telnr = "";
    this.celul = "";
    this.fax = "";
    this.useridLong = "";
    useridLong2 = "";
  }

  @Override
  public String toString()
  {
    //return " pernr=" + getPernr() + " vorna=" + getVorna() + " name2=" + getName2() + " nachn=" + getNachn() + " nach2=" + getNach2() + " kostl=" + getKostl() + " desccc=" + getDesccc() + " wrks=" + getWrks() + " descdiv=" + getDescdiv() + " btrtl=" + getBtrtl() + " descsubdiv=" + getDescsubdiv() + " tipoempl=" + getTipoempl() + " numdocide=" + getNumdocide() + " bukrs=" + getBukrs() + " usrid=" + getUsrid() + " status=" + getStatus() + " codempleadouno=" + getCodempleadouno() + " codempleadodos=" + getCodempleadodos() + " superusuario=" + getSuperusuario() + " gesch=" + this.gesch + " fecin=" + this.fecin + " gbdat=" + getGbdat() + " stras=" + getStras() + " telnr=" + getTelnr() + " celul=" + getCelul() + " fax=" + getFax() + " userid_long=" + getUseridLong();
    return " pernr=" + getPernr() + " numdocide=" + getNumdocide() + " bukrs=" + getBukrs() + " usrid=" + getUsrid() + " vorna=" + getVorna() + " name2=" + getName2() + " nachn=" + getNachn() + " nach2=" + getNach2() + " kostl=" + getKostl() + " wrks=" + getWrks() + " btrtl=" + getBtrtl() + " codempleadouno=" + getCodempleadouno() + " codempleadodos=" + getCodempleadodos();
  }

  public String getPernr()
  {
    return this.pernr;
  }

  public void setPernr(String value) {
    this.pernr = ( value != null) ? value : "" ;
  }

  public String getVorna() {
    return this.vorna;
  }

  public void setVorna(String value) {
    this.vorna = ( value != null) ? value : "" ;
  }

  public String getName2() {
    return this.name2;
  }

  public void setName2(String value) {
    this.name2 = ( value != null) ? value : "" ;
  }

  public String getNachn() {
    return this.nachn;
  }

  public void setNachn(String value) {
    this.nachn = ( value != null) ? value : "" ;
  }

  public String getNach2() {
    return this.nach2;
  }

  public void setNach2(String value) {
    this.nach2 = ( value != null) ? value : "" ;
  }

  public String getKostl() {
    return this.kostl;
  }

  public void setKostl(String value) {
    this.kostl = ( value != null) ? value : "" ;
  }

  public String getDesccc() {
    return this.desccc;
  }

  public void setDesccc(String value) {
    this.desccc = ( value != null) ? value : "" ;
  }

  public String getWrks() {
    return this.wrks;
  }

  public void setWrks(String value) {
    this.wrks = ( value != null) ? value : "" ;
  }

  public String getDescdiv() {
    return this.descdiv;
  }

  public void setDescdiv(String value) {
    this.descdiv = ( value != null) ? value : "" ;
  }

  public String getBtrtl() {
    return this.btrtl;
  }

  public void setBtrtl(String value) {
    this.btrtl = ( value != null) ? value : "" ;
  }

  public String getDescsubdiv() {
    return this.descsubdiv;
  }

  public void setDescsubdiv(String value) {
    this.descsubdiv = ( value != null) ? value : "" ;
  }

  public String getTipoempl() {
    return this.tipoempl;
  }

  public void setTipoempl(String value) {
    this.tipoempl = ( value != null) ? value : "" ;
  }

  public String getNumdocide() {
    return this.numdocide;
  }

  public void setNumdocide(String value) {
    this.numdocide = ( value != null) ? value : "" ;
  }

  public String getBukrs() {
    return this.bukrs;
  }

  public void setBukrs(String value) {
    this.bukrs = ( value != null) ? value : "" ;
  }

  public String getUsrid() {
    return this.usrid;
  }

  public void setUsrid(String value) {
    this.usrid = ( value != null) ? value : "" ;
  }

  public String getStatus() {
    return this.status;
  }

  public void setStatus(String value) {
    this.status = ( value != null) ? value : "" ;
  }

  public String getCodempleadouno() {
    return this.codempleadouno;
  }

  public void setCodempleadouno(String value) {
    this.codempleadouno = ( value != null) ? value : "" ;
  }

  public String getCodempleadodos() {
    return this.codempleadodos;
  }

  public void setCodempleadodos(String value) {
    this.codempleadodos = ( value != null) ? value : "" ;
  }

  public String getSuperusuario() {
    return this.superusuario;
  }

  public void setSuperusuario(String value) {
    this.superusuario = ( value != null) ? value : "" ;
  }

  public String getGesch()
  {
    return this.gesch;
  }

  public void setGesch(String value)
  {
    this.gesch = ( value != null) ? value : "" ;
  }

  public String getFecin()
  {
    return this.fecin;
  }

  public void setFecin(String value)
  {
    this.fecin = ( value != null) ? value : "" ;
  }

  public String getGbdat()
  {
    return this.gbdat;
  }

  public void setGbdat(String value)
  {
    this.gbdat = ( value != null) ? value : "" ;
  }

  public String getStras()
  {
    return this.stras;
  }

  public void setStras(String value)
  {
    this.stras = ( value != null) ? value : "" ;
  }

  public String getTelnr()
  {
    return this.telnr;
  }

  public void setTelnr(String value)
  {
    this.telnr = ( value != null) ? value : "" ;
  }

  public String getCelul()
  {
    return this.celul;
  }

  public void setCelul(String value)
  {
    this.celul = ( value != null) ? value : "" ;
  }

  public String getFax()
  {
    return this.fax;
  }

  public void setFax(String value)
  {
    this.fax = ( value != null) ? value : "" ;
  }

  public String getUseridLong()
  {
    return this.useridLong;
  }

  public void setUseridLong(String value)
  {
    this.useridLong = ( value != null) ? value : "" ;
  }

    /**
     * @return the estado
     */
    public String getEstado() {
        return estado;
    }

    /**
     * @param estado the estado to set
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * @return the useridLong2
     */
    public String getUseridLong2() {
        return useridLong2;
    }

    /**
     * @param useridLong2 the useridLong2 to set
     */
    public void setUseridLong2(String useridLong2) {
        this.useridLong2 = useridLong2;
    }
}