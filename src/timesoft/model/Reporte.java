/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.model;

/**
 *
 * @author Camilo
 */
public class Reporte {
    
    private int exitosos;
    private int erroneos;
    private int ignorados;

    public Reporte() {
        exitosos = 0;
        erroneos = 0;
        ignorados = 0;
    }
    
    @Override
    public String toString() {
        return getMessage();
    }
    
    /**
     * @return the procesados
     */
    public int getProcesados() {
        return exitosos + erroneos;
    }


    /**
     * @return the exitosos
     */
    public int getExitosos() {
        return exitosos;
    }

    /**
     * @param exitosos the exitosos to set
     */
    public void setExitosos(int exitosos) {
        this.exitosos = exitosos;
    }

    /**
     * @return the erroneos
     */
    public int getErroneos() {
        return erroneos;
    }

    /**
     * @param erroneos the erroneos to set
     */
    public void setErroneos(int erroneos) {
        this.erroneos = erroneos;
    }
    
    public void addExitoso() {
        exitosos++;
    }
    
    public void addErroneo() {
        erroneos++;
    }
    
    
    public void addIgnorados() {
        ignorados++;
    }
    
    public String getMessage() {
        
        StringBuilder sb = new StringBuilder();
        sb.append(getProcesados());
        if ( getProcesados() == 1 )
           sb.append(" registro procesado");
        else
            sb.append(" registros procesados");
        
        if ( getProcesados() > 0 )
        {
            sb.append(", ");
            sb.append(getExitosos());
            if ( getExitosos() == 1 )
                sb.append(" exitoso, ");
            else
                sb.append(" exitosos, ");
            sb.append(getErroneos());
            if ( getErroneos() == 1 )
                sb.append(" erróneo");
            else 
                sb.append(" erróneos");
        }
        if ( ignorados > 0 )
        {
            sb.append(". ");
            sb.append(ignorados);
            if ( ignorados == 1 )
                sb.append(" registro ignorado.");
            else 
                sb.append(" registros ignorados.");
        }
        else
        {
            sb.append(".");
        }
                
        return sb.toString();
        
    }
            
    
}
