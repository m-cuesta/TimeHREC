/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import javax.persistence.Table;

/**
 *
 * @author Camilo
 */
@Entity
@Table(name="dbo.tblUsuarioTimehr")
public class Usuario implements Serializable, Comparable<Usuario> {
    
    @Id
    @Column
    private String login;
    
    @Column
    private String password;
    
    @Column
    private String nombre;
    
    @Column
    private String rol;
    
    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, mappedBy="pk.usuario")
    private List<UsuarioPlanta> plantas = new ArrayList<UsuarioPlanta>();
    
    
    public Usuario() {
        login = null;
        password = null;
        rol = null;
        nombre = null;
    }
    
    public Usuario(String pLogin, String pPass, String pRol ) {
        login = pLogin;
        password = pPass;
        rol = pRol;
    }
    
    @Override
    public String toString() {
        return login;
    }

    /**
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * @param login the login to set
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the rol
     */
    public String getRol() {
        return rol;
    }

    /**
     * @param rol the rol to set
     */
    public void setRol(String rol) {
        this.rol = rol;
    }

    public int compareTo(Usuario o) {
        if ( login == null )
            return 0;
        else
            return login.compareTo(o.getLogin());
    }

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

 

    public List<UsuarioPlanta> getPlantas() {
        return plantas;
    }


    public void setPlantas(List<UsuarioPlanta> plantas) {
        this.plantas = plantas;
    }


    
}
