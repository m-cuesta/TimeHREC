package timesoft.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.inga.utils.SigarUtils;

@Entity
@Table(name="tblCiudad")
public class Ciudad
  implements Serializable
{
  /**
	 * 
	 */
	private static final long serialVersionUID = -1588167555206861968L;
private String intpkidciudad;
  private String strdescripcionciudad;
  private Timestamp dtmfechamodificado;
  private String strloginmodificado;

  public Ciudad()
  {
    this.intpkidciudad = "";
    this.strdescripcionciudad = "";
    this.dtmfechamodificado = new Timestamp(new Date().getTime() );
    this.strloginmodificado = "";
  }

  public String toString()
  {
    return " intpkidciudad=" + getIntpkidciudad() + " strdescripcionciudad=" + getStrdescripcionciudad() + " dtmfechamodificado=" + getDtmfechamodificado() + " strloginmodificado=" + getStrloginmodificado();
  }

  @Id
  public String getIntpkidciudad()
  {
    return this.intpkidciudad;
  }

  public void setIntpkidciudad(String value) {
	if (value == null)
	    this.intpkidciudad = "";
    else
	    this.intpkidciudad = value;
	  
  }
  @Column
  public String getStrdescripcionciudad() {
    return this.strdescripcionciudad;
  }

  public void setStrdescripcionciudad(String value) {
		if (value == null)
		    this.strdescripcionciudad = "";
	    else
	        this.strdescripcionciudad = value;
  }
  @Column
  public Timestamp getDtmfechamodificado() {
    return this.dtmfechamodificado;
  }

  public void setDtmfechamodificado(Timestamp value) {
		if (value == null)
	        this.dtmfechamodificado = new Timestamp(new Date().getTime());
	    else
	        this.dtmfechamodificado = value;
  }
  @Column
  public String getStrloginmodificado() {
    return this.strloginmodificado;
  }

  public void setStrloginmodificado(String value) {
    this.strloginmodificado = SigarUtils.validarCadena(value);
  }
}