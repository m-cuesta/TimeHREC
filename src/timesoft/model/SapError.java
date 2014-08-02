/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timesoft.model;

/**
 *
 * @author Usuario
 */
public class SapError {

    private Integer id;
    private String pernr;
    private String mensaje;

    public SapError( Integer pId, String pPernr, String pMensaje ) {
        id = pId;
        pernr = pPernr;
        mensaje = pMensaje;
    }

    @Override
    public String toString() {
        return "Id:" + getId() + " No.Pers:" + getPernr() + " " + getMensaje();
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the pernr
     */
    public String getPernr() {
        return pernr;
    }

    /**
     * @param pernr the pernr to set
     */
    public void setPernr(String pernr) {
        this.pernr = pernr;
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
