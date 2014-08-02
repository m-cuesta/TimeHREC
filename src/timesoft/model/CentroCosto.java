package timesoft.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="tblCentroCostosSap")
public class CentroCosto
  implements Serializable
{

  /**
	 * 
	 */
	private static final long serialVersionUID = 4034831360118708893L;

@Id
  @Column
  private String kostl;

  @Column
  private String desccc;

  @Column
  private String estado;

  public CentroCosto()
  {
    this.kostl = "";
    this.desccc = "";
    this.estado = "";
  }

  @Override
  public String toString()
  {
    return " kostl=" + getKostl() + " desccc=" + getDesccc() + " estado=" + getEstado();
  }

  public String getKostl()
  {
    return this.kostl;
  }

  public void setKostl(String value) {
      this.kostl = value;
      if ( this.kostl == null )
    	  this.kostl = "";
  }

  public String getDesccc() {
    return this.desccc;
  }

  public void setDesccc(String value) {
	  this.desccc = value;
	  if ( this.desccc == null )
		  this.desccc = "";
  }

  public String getEstado() {
    return this.estado;
  }

  public void setEstado(String value) {
    if (value == null)
      this.estado = "";
    else
      this.estado = value;
  }
}