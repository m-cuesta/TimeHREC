/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.model;

import com.inga.utils.DateRange;

/**
 *
 * @author Camilo
 */
public class CriteriosIntento {
    
    private String firma;
    private DateRange creado;
    private DateRange modificado;
    private Character estado;
    private Integer tipoActividad;
    private String orderBy;
    private String orderDirection;
    
    public CriteriosIntento() {
        firma = null;
        creado = null;
        modificado = null;
        estado = null;
        tipoActividad = null;
        orderBy = null;
        orderDirection = null;
    }

    /**
     * @return the firma
     */
    public String getFirma() {
        return firma;
    }

    /**
     * @param firma the firma to set
     */
    public void setFirma(String firma) {
        this.firma = firma;
    }

    /**
     * @return the creado
     */
    public DateRange getCreado() {
        return creado;
    }

    /**
     * @param creado the creado to set
     */
    public void setCreado(DateRange creado) {
        this.creado = creado;
    }

    /**
     * @return the modificado
     */
    public DateRange getModificado() {
        return modificado;
    }

    /**
     * @param modificado the modificado to set
     */
    public void setModificado(DateRange modificado) {
        this.modificado = modificado;
    }

    /**
     * @return the estado
     */
    public Character getEstado() {
        return estado;
    }

    /**
     * @param estado the estado to set
     */
    public void setEstado(Character estado) {
        this.estado = estado;
    }

    /**
     * @return the tipoActividad
     */
    public Integer getTipoActividad() {
        return tipoActividad;
    }

    /**
     * @param tipoActividad the tipoActividad to set
     */
    public void setTipoActividad(Integer tipoActividad) {
        this.tipoActividad = tipoActividad;
    }

    /**
     * @return the orderBy
     */
    public String getOrderBy() {
        return orderBy;
    }

    /**
     * @param orderBy the orderBy to set
     */
    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    /**
     * @return the orderDirection
     */
    public String getOrderDirection() {
        return orderDirection;
    }

    /**
     * @param orderDirection the orderDirection to set
     */
    public void setOrderDirection(String orderDirection) {
        this.orderDirection = orderDirection;
    }
    
}
