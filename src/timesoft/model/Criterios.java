/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.model;

import com.inga.utils.DateRange;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Camilo
 */
public class Criterios {
    
   private DateRange cntrl;
   private DateRange capturaSibo;
   private java.lang.String cntdr;
   private java.lang.String mensaje;
   private java.lang.String retorno;
   private List<Integer> ids;
   private List<IdRange> rangosId;
   private List<String> pernrs;
   private List<IdRange> rangosPernr;
   private String orderBy;
   private String orderDirection;
   private String tipo;
   private boolean ignoreFechaInvalida;
   private boolean ignoreMarked;
   private Set<String> subdivisiones;
   private String operador;
   private String btrtl;

    /** Creates a new instance of CriteriosMarcacion*/
    public Criterios() {
        cntrl = null;
        cntdr = null;
        mensaje = null;
        retorno = null;
        ids = null;
        pernrs = null;
        rangosId = null;
        rangosPernr = null;
        capturaSibo = null;
        orderBy = null;
        orderDirection = null;
        tipo = null;
        ignoreFechaInvalida = false;
        ignoreMarked = false;
        subdivisiones = null;
        operador = null;
        btrtl = null;
    }

    @Override
    public String toString() {
        
        
    return 
            
        " ids=" + " " + getIds() + " " + rangosId + 
        " pernr=" + getPernrs() + " " + rangosPernr + 
        " cntrl=" + getCntrl() + 
        " cntdr=" + getCntdr() + 
        " mensaje=" + getMensaje() + 
        " retorno=" + getRetorno() +
        " Cap.Sibo=" + getCapturaSibo() + 
        " subs=" + subdivisiones
        ;
    }

    public DateRange getCntrl() {
        return cntrl;
    }

    public void setCntrl(DateRange value ) {
        cntrl = value;
    }

    public java.lang.String getCntdr() {
        return cntdr;
    }

    public void setCntdr(java.lang.String value ) {
        cntdr = value;
    }

    public java.lang.String getMensaje() {
        return mensaje;
    }

    public void setMensaje(java.lang.String value ) {
        mensaje = value;
    }

    public java.lang.String getRetorno() {
        return retorno;
    }

    public void setRetorno(java.lang.String value ) {
        retorno = value;
    }

    /**
     * @return the ids
     */
    public List<Integer> getIds() {
        return ids;
    }

    /**
     * @param ids the ids to set
     */
    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    /**
     * @return the pernrs
     */
    public List<String> getPernrs() {
        return pernrs;
    }

    /**
     * @param pernrs the pernrs to set
     */
    public void setPernrs(List<String> pernrs) {
        ArrayList<String> temp = new ArrayList<String>();

        if ( pernrs != null )
        {
            for ( String p : pernrs )
                temp.add( String.valueOf(Integer.parseInt(p)) );
        }

        
        this.pernrs = temp;
    }

    /**
     * @return the rangosPernr
     */
    public List<IdRange> getRangosPernr() {
        return rangosPernr;
    }

    /**
     * @param rangosPernr the rangosPernr to set
     */
    public void setRangosPernr(List<IdRange> rangosPernr) {
        this.rangosPernr = rangosPernr;
    }

    /**
     * @return the rangosId
     */
    public List<IdRange> getRangosId() {
        return rangosId;
    }

    /**
     * @param rangosId the rangosId to set
     */
    public void setRangosId(List<IdRange> rangosId) {
        this.rangosId = rangosId;
    }

    /**
     * @return the capturaSibo
     */
    public DateRange getCapturaSibo() {
        return capturaSibo;
    }

    /**
     * @param capturaSibo the capturaSibo to set
     */
    public void setCapturaSibo(DateRange range) {
        this.capturaSibo = range;
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

    /**
     * @return the tipo
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * @param tipo the tipo to set
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }


    /**
     * @return the ignoreFechaInvalida
     */
    public boolean isIgnoreFechaInvalida() {
        return ignoreFechaInvalida;
    }

    /**
     * @param ignoreFechaInvalida the ignoreFechaInvalida to set
     */
    public void setIgnoreFechaInvalida(boolean ignoreFechaInvalida) {
        this.ignoreFechaInvalida = ignoreFechaInvalida;
    }

    /**
     * @return the ignoreMarked
     */
    public boolean isIgnoreMarked() {
        return ignoreMarked;
    }

    /**
     * @param ignoreMarked the ignoreMarked to set
     */
    public void setIgnoreMarked(boolean ignoreMarked) {
        this.ignoreMarked = ignoreMarked;
    }

    /**
     * @return the subdivisiones
     */
    public Set<String> getSubdivisiones() {
        return subdivisiones;
    }

    /**
     * @param subdivisiones the subdivisiones to set
     */
    public void setSubdivisiones(Set<String> subdivisiones) {
        this.subdivisiones = subdivisiones;
    }

    /**
     * @return the operador
     */
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
    public String getBtrtl() {
        return btrtl;
    }

    /**
     * @param btrtl the btrtl to set
     */
    public void setBtrtl(String btrtl) {
        this.btrtl = btrtl;
    }

    }
