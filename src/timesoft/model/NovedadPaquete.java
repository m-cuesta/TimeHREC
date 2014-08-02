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
@Table(name="tblNovedadesPaquete")
public class NovedadPaquete extends RegistroImpl
  implements Serializable, Registro
{
  private static final long serialVersionUID = -2412271907622437613L;
  private Integer id;
  private String pernr;
  private String ldate;
  private String lhour;
  private Integer icantidad;
  private String codreclama;
  private String nombreeclama;
  private String itipo;
  private Timestamp fechacrea;
  private Timestamp cntrl;
  private String cntdr;
  private String mensaje;
  private String retorno;
  private Timestamp fechaModificado;
  private String subdivision;
  private String operador;  
  private String btrtl;
  private String nombres;
  private String primerApellido;
  private String segundoApellido;

  public NovedadPaquete()
  {
    this.id = -1;
    this.pernr = "";
    this.ldate = "";
    this.icantidad = 0;
    this.codreclama = "";
    this.nombreeclama = "";
    this.itipo = "";
    this.fechacrea = new Timestamp( new Date().getTime());
    this.cntrl = new Timestamp( new Date().getTime());
    this.cntdr = "";
    this.mensaje = "";
    this.retorno = "";
    this.fechaModificado = new Timestamp( new Date().getTime());
    this.lhour = "";
    this.operador = "";
    this.subdivision = "";
    btrtl = "";
  }

  @Override
  public String toString()
  {
    return " id=" + getId() + " pernr=" + getPernr() + " ldate=" + getLdate() + " lhour= " + getLhour() + " icantidad=" + getIcantidad() + " codreclama=" + getCodreclama() + " nombreeclama=" + getNombreeclama() + " itipo=" + getItipo() + " fechacrea=" + getFechacrea() + " cntrl=" + getCntrl() + " cntdr=" + getCntdr() + " mensaje=" + getMensaje() + " retorno=" + getRetorno();
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
  public String getLdate() {
    return this.ldate;
  }

  public void setLdate(String value) {
    this.ldate = ( value != null ) ? value : "" ;
  }
  @Column(updatable=false)
  public Integer getIcantidad() {
    return this.icantidad;
  }

  public void setIcantidad(Integer value) {
    this.icantidad = value;
  }
  @Column(updatable=false)
  public String getCodreclama() {
    return this.codreclama;
  }

  public void setCodreclama(String value) {
    this.codreclama = ( value != null ) ? value : "" ;
  }
  @Column(updatable=false)
  public String getNombreeclama() {
    return this.nombreeclama;
  }

  public void setNombreeclama(String value) {
    this.nombreeclama = ( value != null ) ? value : "" ;
  }
  @Column(updatable=false)
  public String getItipo() {
    return this.itipo;
  }

  public void setItipo(String value) {
    this.itipo = ( value != null ) ? value : "" ;
  }
  @Column(updatable=false)
  public Timestamp getFechacrea() {
    return this.fechacrea;
  }

  public void setFechacrea(Timestamp value) {
    this.fechacrea = value;
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

  @Column(name="ltime", updatable=false)
  public String getLhour()
  {
    return this.lhour;
  }

  public void setLhour(String lhour)
  {
    this.lhour = lhour;
  }

  @Transient
  public Date getFechaCaptura() {
    try {
      return Constantes.dfSibo.parse(getLdate().trim() + getLhour().trim());
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
