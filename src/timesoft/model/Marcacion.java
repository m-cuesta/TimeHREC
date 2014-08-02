package timesoft.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import timesoft.Constantes;

@Entity
@Table(name="tblMarcacionesSap")
public class Marcacion extends RegistroImpl
  implements Serializable, Registro
{
  /**
	 * 
	 */
	private static final long serialVersionUID = -3492334261456667263L;

  @Id
  @Column(updatable=false)
  private Integer id;
  
  @Column(updatable=false)  
  private String pernr;
  
  @Column(updatable=false)
  private String ldate;
  
  @Column(updatable=false)  
  private String name2;
  
  @Column(updatable=false)  
  private String satza;
  
  @Column(updatable=false)  
  private String dallf;
  
  @Column(updatable=false)  
  private String kostl;
  
  @Column(name="intfkCiudad", updatable=false, length=20)
  private String subdivision;
  
  @Column
  private String btrtl;
  
  @Column(length=255)
  private String operador;
  
  @Column  
  private Timestamp cntrl;

  @Column
  private String cntdr;

  @Column
  private String mensaje;

  @Column
  private String retorno;

  @Column
  private Timestamp fechaModificado;
  
  @Column(nullable=true)
  private String nombres;

  @Column(nullable=true)
  private String primerApellido;

  @Column(nullable=true)
  private String segundoApellido;
  
  @Column(nullable=true)
  private String abwgr;
  
  
  
  public Marcacion()
  {
    this.id = -1;
    this.pernr = "";
    this.ldate = "";
    this.name2 = "";
    this.satza = "";
    this.dallf = "";
    this.cntrl = new Timestamp(new Date().getTime());
    cntdr = "";
    kostl = "";
    this.mensaje = "";
    this.retorno = "";
    this.fechaModificado = new Timestamp(new Date().getTime());
    this.operador = "";
    this.subdivision = "";
    this.btrtl = "";
    this.abwgr = "";
  }

  @Override
  public String toString()
  {
    return " id=" + getId() + " pernr=" + getPernr() + " ldate=" + getLdate() + " name2=" + getName2() + " satza=" + getSatza() + " dallf=" + getDallf() + " cntrl=" + getCntrl() + " cntdr=" + getCntdr() + " mensaje=" + getMensaje() + " retorno=" + getRetorno() + " fechaModificado=" + getFechaModificado() + " kostl=" + getKostl() + " abwgr=" + abwgr;
  }

  public Integer getId()
  {
    return this.id;
  }

  public void setId(Integer value) {
    this.id = value;
  }

  public String getPernr() {
    return this.pernr;
  }

  public void setPernr(String value) {
    this.pernr = ( value != null ) ? value : "" ;
  }

  public String getLdate() {
    return this.ldate;
  }

  public void setLdate(String value) {
    this.ldate = ( value != null ) ? value : "" ;
  }

  public String getName2() {
    return this.name2;
  }

  public void setName2(String value) {
    this.name2 = ( value != null ) ? value : "" ;
  }

  public String getSatza() {
    return this.satza;
  }

  public void setSatza(String value) {
    this.satza = ( value != null ) ? value : "" ;
  }

  public String getDallf() {
    return this.dallf;
  }

  public void setDallf(String value) {
    this.dallf = ( value != null ) ? value : "" ;
  }
  

  public Timestamp getCntrl() {
    return this.cntrl;
  }

  public void setCntrl(Timestamp value) {
    this.cntrl = value;
  }

  public String getCntdr() {
    return this.cntdr;
  }

  public void setCntdr(String value) {
    this.cntdr = ( value != null ) ? value : "" ;
    cntdr = cntdr.trim();
  }

  public String getMensaje() {
    return this.mensaje;
  }

  public void setMensaje(String value) {
    this.mensaje = ( value != null ) ? value : "" ;
    this.mensaje = mensaje.trim();
    if ((this.mensaje != null) && (this.mensaje.length() > 249))
      this.mensaje = this.mensaje.substring(0, 249); 
  }


  public String getRetorno() {
    return this.retorno;
  }

  public void setRetorno(String value) {
    this.retorno = ( value != null ) ? value : "" ;
  }


  public Timestamp getFechaModificado()
  {
    return this.fechaModificado;
  }

  public void setFechaModificado(Timestamp fechaModificado)
  {
    this.fechaModificado = fechaModificado;
  }


  public String getKostl()
  {
    return this.kostl;
  }

  public void setKostl(String kostl)
  {
    this.kostl = kostl;
    if ( this.kostl == null )
    	this.kostl = "";
  }


  public Date getFechaCaptura() {
    try {
      return Constantes.dfSibo.parse(getLdate().trim() + getName2().trim());
    }
    catch (Exception ex) {
    }
    return null;
  }

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
     * @return the operador
     */
    public String getOperador() {
        return operador;
    }

    /**
     * @param operador the operador to set
     */
    public void setOperador(String operador) {
        this.operador = operador;
    }

    /**
     * @return the btrtl
     */
    public String getBtrtl() {
        return btrtl;
    }

    /**
     * @param btrtl the btrtl to set
     */
    public void setBtrtl(String btrtl) {
        this.btrtl = btrtl;
    }

    /**
     * @return the nombres
     */
    public String getNombres() {
        return nombres;
    }

    /**
     * @param nombres the nombres to set
     */
    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    /**
     * @return the primerApellido
     */
    public String getPrimerApellido() {
        return primerApellido;
    }

    /**
     * @param primerApellido the primerApellido to set
     */
    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    /**
     * @return the segundoApellido
     */
    public String getSegundoApellido() {
        return segundoApellido;
    }

    /**
     * @param segundoApellido the segundoApellido to set
     */
    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    /**
     * @return the abwgr
     */
    public String getAbwgr() {
        return abwgr;
    }

    /**
     * @param abwgr the abwgr to set
     */
    public void setAbwgr(String abwgr) {
        this.abwgr = abwgr;
    }

    }
