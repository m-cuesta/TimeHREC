package timesoft.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import timesoft.Constantes;

@Entity
@Table(name="tblNovedadesSap")
public class Novedad extends RegistroImpl
  implements Serializable, Registro
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 154405572142550403L;
	
private Integer id;
  private String pernr;
  private String pinco;
  private String begda;
  private String endda;
  private String beguz;
  private String enduz;
  private String vtken;
  private Timestamp cntrl;
  private String cntdr;
  private String kostl;
  private String mensaje;
  private String retorno;
  private Timestamp fechaModificado;
  private String subdivision;
  private String operador;  
  private String btrtl;
  private String nombres;
  private String primerApellido;
  private String segundoApellido;

  public Novedad()
  {
    this.id = -1;
    this.pernr = "";
    this.pinco = "";
    this.begda = "";
    this.endda = "";
    this.beguz = "";
    this.enduz = "";
    this.vtken = "";
    this.cntrl = new Timestamp(new Date().getTime());
    cntdr = "";
    kostl = "";
    this.mensaje = "";
    this.retorno = "";
    this.fechaModificado = new Timestamp(new Date().getTime());
    this.operador = "";
    this.subdivision = "";
    btrtl = "";
  }

  public String toString()
  {
    return " id=" + getId() + " pernr=" + getPernr() + " pinco=" + getPinco() + " begda=" + getBegda() + " endda=" + getEndda() + " beguz=" + getBeguz() + " enduz=" + getEnduz() + " vtken=" + getVtken() + " cntrl=" + getCntrl() + " cntdr=" + getCntdr() + " kostl=" + getKostl() + " mensaje=" + getMensaje() + " retorno=" + getRetorno();
  }

  @Id
  @Column(updatable=false)
  public Integer getId()
  {
    return this.id;
  }

  public void setId(Integer value) {
    this.id = value;
  }
  @Column(updatable=false)
  public String getPernr() {
    return this.pernr;
  }

  public void setPernr(String value) {
    this.pernr = ( value != null ) ? value : "" ;
  }
  @Column(updatable=false)
  public String getPinco() {
    return this.pinco;
  }

  public void setPinco(String value) {
    this.pinco = ( value != null ) ? value : "" ;
  }
  @Column(updatable=false)
  public String getBegda() {
    return this.begda;
  }

  public void setBegda(String value) {
    this.begda = ( value != null ) ? value : "" ;
  }
  @Column(updatable=false)
  public String getEndda() {
    return this.endda;
  }

  public void setEndda(String value) {
    this.endda = ( value != null ) ? value : "" ;
  }
  @Column(updatable=false)
  public String getBeguz() {
    return this.beguz;
  }

  public void setBeguz(String value) {
    this.beguz = ( value != null ) ? value : "" ;
  }
  @Column(updatable=false)
  public String getEnduz() {
    return this.enduz;
  }

  public void setEnduz(String value) {
    this.enduz = ( value != null ) ? value : "" ;
  }
  @Column(updatable=false)
  public String getVtken() {
    return this.vtken;
  }

  public void setVtken(String value) {
    this.vtken = ( value != null ) ? value : "" ;
  }
  @Column
  public Timestamp getCntrl() {
    return this.cntrl;
  }

  public void setCntrl(Timestamp value) {
    this.cntrl = value;
  }
  @Column
  public String getCntdr() {
    return this.cntdr;
  }

  public void setCntdr(String value) {
    this.cntdr = ( value != null ) ? value : "" ;
    cntdr = cntdr.trim();
    
  }
  @Column(  nullable=false, updatable=false )
  public String getKostl() {
    return this.kostl;
  }

  public void setKostl(String value) {
    this.kostl = ( value != null ) ? value : "" ;
    
  }
  @Column
  public String getMensaje() {
    return this.mensaje;
  }

  public void setMensaje(String value) {
    this.mensaje = ( value != null ) ? value : "" ;
    this.mensaje = mensaje.trim();
    if ((this.mensaje != null) && (this.mensaje.length() > 249))
      this.mensaje = this.mensaje.substring(0, 249); 
  }

  @Column
  public String getRetorno() {
    return this.retorno;
  }

  public void setRetorno(String value) {
    this.retorno = ( value != null ) ? value : "" ;
  }

  @Column
  public Timestamp getFechaModificado()
  {
    return this.fechaModificado;
  }

  public void setFechaModificado(Timestamp fechaModificado)
  {
    this.fechaModificado = fechaModificado;
  }

  @Transient
  public Date getFechaCaptura() {
    try {
      return Constantes.dfSibo.parse(getBegda().trim() + getBeguz().trim());
    }
    catch (Exception ex) {
    }
    return null;
  }

    /**
     * @return the subdivision
     */
    @Column(name="intfkCiudad", updatable=false, length=20)
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
    @Column(length=255)
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
    @Column
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
    @Column(nullable=true)
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
    @Column(nullable=true)
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
    @Column(nullable=true)
    public String getSegundoApellido() {
        return segundoApellido;
    }

    /**
     * @param segundoApellido the segundoApellido to set
     */
    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }




    
}