/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.model;

import java.util.Date;

/**
 *
 * @author Camilo
 */
public interface Registro {
    
    public Integer getId();
    public String getPernr();
    public Date   getFechaCaptura();
    public java.sql.Timestamp getCntrl();
    public void setCntrl(java.sql.Timestamp value );
    public java.lang.String getRetorno();
    public void setRetorno(java.lang.String value );
    public java.lang.String getMensaje();
    public void setMensaje(java.lang.String value );
    public java.lang.String getCntdr();
    public void setCntdr(java.lang.String value );
    public String getOperador();
    public void setOperador(String value);
    public String getSubdivision();
    public void setSubdivision(String value);
    public String getBtrtl();
    public void setBtrtl(String value);
    public String getNombres();
    public void setNombres(String value);
    public String getPrimerApellido();
    public void setPrimerApellido(String value);
    public String getSegundoApellido();
    public void setSegundoApellido(String value);
    
    
}


