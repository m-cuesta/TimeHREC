/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.model;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/** Models a Marcacion
 *@author Manuel C. Cuesta
 */
@Entity
@Table(name="tblIntentos")
@SuppressWarnings("serial")
public class Intento implements Serializable {
    
    
    private Long id;
    
    private Timestamp creado;
    private Timestamp modificado;
    private String firma;
    private Integer tipoActividad;
    private Character estado;
    private String observacion;
    private Integer registrosIntentados;
    private Integer registrosProcesados;
    private Integer registrosConError;
    
    public Intento() {
        id = null;
        creado = null;
        modificado = null;
        firma = null;
        tipoActividad = null;
        estado = null;
        observacion = null;
        registrosIntentados = 0;
        registrosProcesados = 0;
        registrosConError = 0;
    }
    
    @Override
    public String toString() {
        return  "Id: " + id + 
                " Creado:" + creado + 
                " Modificado: " + modificado + 
                " Firma:" + firma + 
                " Tipo:" + tipoActividad + 
                " Estado:" + estado;
    }
    

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the creado
     */
    @Column
    public Timestamp getCreado() {
        return creado;
    }

    /**
     * @param creado the creado to set
     */
    public void setCreado(Timestamp creado) {
        this.creado = creado;
    }

    /**
     * @return the modificado
     */
    @Column
    public Timestamp getModificado() {
        return modificado;
    }

    /**
     * @param modificado the modificado to set
     */
    public void setModificado(Timestamp modificado) {
        this.modificado = modificado;
    }

    /**
     * @return the firma
     */
    @Column
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
     * @return the tipoActividad
     */
    @Column
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
     * @return the estado
     */
    @Column
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
     * @return the observacion
     */
    @Column( length=512 )
    public String getObservacion() {
        return observacion;
    }

    /**
     * @param observacion the observacion to set
     */
    public void setObservacion(String pObservacion) {
        this.observacion = pObservacion;
        
        if ( observacion != null && observacion.length() > 512 )
            observacion = observacion.substring(0,512);
    }

    /**
     * @return the registrosIntentados
     */
    public Integer getRegistrosIntentados() {
        return registrosIntentados;
    }

    /**
     * @param registrosIntentados the registrosIntentados to set
     */
    public void setRegistrosIntentados(Integer registrosIntentados) {
        this.registrosIntentados = registrosIntentados;
    }

    /**
     * @return the registrosProcesados
     */
    public Integer getRegistrosProcesados() {
        return registrosProcesados;
    }

    /**
     * @param registrosProcesados the registrosProcesados to set
     */
    public void setRegistrosProcesados(Integer registrosProcesados) {
        this.registrosProcesados = registrosProcesados;
    }

    /**
     * @return the regitrosConError
     */
    public Integer getRegistrosConError() {
        return registrosConError;
    }

    /**
     * @param regitrosConError the regitrosConError to set
     */
    public void setRegistrosConError(Integer registrosConError) {
        this.registrosConError = registrosConError;
    }
    
}
