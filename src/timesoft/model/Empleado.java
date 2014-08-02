package timesoft.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="tblEmpleados")
public class Empleado
  implements Serializable
{
    
    private static final long serialVersionUID = -5797008822976563395L;
    private String strpkIdentificacion;
    private String strcodigo;
    private String intfkciudad;
    private String strNombres;
    private String strPrimerApellido;
    private String strSegundoApellido;
    
  
  
  public Empleado() {
	  this.strpkIdentificacion = "";
	  this.strcodigo = "";
	  this.intfkciudad = "";
  }
  
  @Override
  public String toString() {
      return this.getStrpkIdentificacion() + ": >" + 
              this.getStrcodigo() + "< Ciudad:" + 
              this.getIntfkciudad() + 
              this.getStrNombres() + " " + 
              this.getStrPrimerApellido() + " " +
              this.getStrSegundoApellido(); 
  }

  @Column
  public String getStrcodigo()
  {
    return this.strcodigo;
  }

  public void setStrcodigo(String strcodigo)
  {
    this.strcodigo = strcodigo;
  }

  @Column
  public String getIntfkciudad()
  {
    return this.intfkciudad;
  }

  public void setIntfkciudad(String intfkcuidad)
  {
    this.intfkciudad = intfkcuidad;
  }

  @Id
  @Column
  public String getStrpkIdentificacion()
  {
    return this.strpkIdentificacion;
  }

  public void setStrpkIdentificacion(String strpkIdentificacion)
  {
    this.strpkIdentificacion = strpkIdentificacion;
  }

    /**
     * @return the strNombres
     */
  @Column(nullable=true)
    public String getStrNombres() {
        return strNombres;
    }

    /**
     * @param strNombres the strNombres to set
     */
    public void setStrNombres(String strNombres) {
        this.strNombres = strNombres;
    }

    /**
     * @return the strPrimerApellido
     */
   @Column(nullable=true)
    public String getStrPrimerApellido() {
        return strPrimerApellido;
    }

    /**
     * @param strPrimerApellido the strPrimerApellido to set
     */
    public void setStrPrimerApellido(String strPrimerApellido) {
        this.strPrimerApellido = strPrimerApellido;
    }

    /**
     * @return the strSegundoApellido
     */
   @Column(nullable=true)
    public String getStrSegundoApellido() {
        return strSegundoApellido;
    }

    /**
     * @param strSegundoApellido the strSegundoApellido to set
     */
    public void setStrSegundoApellido(String strSegundoApellido) {
        this.strSegundoApellido = strSegundoApellido;
    }
}