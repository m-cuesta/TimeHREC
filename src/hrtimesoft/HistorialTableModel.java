/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hrtimesoft;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import timesoft.model.Intento;

/**
 *
 * @author HugoAZ
 */
public class HistorialTableModel  extends DefaultTableModel {




    private String[] labels = new String[]{ "ID",
                                            "INICIO",
                                            "FIN",
                                            "USUARIO",
                                            "ESTADO",
                                            "TAREA",
                                            "TOTAL",
                                            "OK",
                                            "ERROR",
                                            "OBSERVACIÓN"
                                          };
    SimpleDateFormat dfSibo = new SimpleDateFormat("yyyyMMddHHmmss");
    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");


    public HistorialTableModel(List<Intento> pRegs ){
        dataVector = new Vector<Intento>(pRegs);
    }

    @Override
    public int getRowCount() {
        return dataVector.size();
    }

    @Override
    public int getColumnCount() {
        return labels.length;
    }

    @Override
    public Class getColumnClass(int c) {
        switch ( c )
        {
            case 6:
            case 7:
            case 8:
                return Integer.class;
            default:
                return String.class;
        }
    }

    @Override
    public String getColumnName(int col) {
        return labels[col];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Intento r = (Intento) dataVector.elementAt(rowIndex);
        Date d;
        
        
        switch ( columnIndex )
        {
            case 0:
                return String.valueOf(r.getId());
            case 1:
                d = r.getCreado();
                if ( d != null )
                    return df.format( d );
                else
                    return "";
            case 2:
                d = r.getModificado();
                if ( d != null )
                    return df.format( d );
                else
                    return "";
            case 3:
                return r.getFirma();
            case 4:
                return timesoft.Constantes.getEstadoIntento(r.getEstado());
            case 5:
                return timesoft.Constantes.getActividad( r.getTipoActividad() );
            case 6:
                return r.getRegistrosIntentados();
            case 7:
                return r.getRegistrosProcesados();
            case 8:
                return r.getRegistrosConError();
            case 9:
                return r.getObservacion();
            default:
                return null;
        }

    }

}
