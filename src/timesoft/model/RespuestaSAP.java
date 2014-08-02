/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.model;

/**
 *
 * @author Camilo
 */
public class RespuestaSAP {
    
    private String retorno;
    private String mensaje;
    
    public RespuestaSAP(String retorno, String mensaje) {
        this.retorno = retorno;
        this.mensaje = mensaje;
    }

    /**
     * @return the retorno
     */
    public String getRetorno() {
        return retorno;
    }

    /**
     * @param retorno the retorno to set
     */
    public void setRetorno(String retorno) {
        this.retorno = retorno;
    }

    /**
     * @return the mensaje
     */
    public String getMensaje() {
        return mensaje;
    }

    /**
     * @param mensaje the mensaje to set
     */
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    
    
}
