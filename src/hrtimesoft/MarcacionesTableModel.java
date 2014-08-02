/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hrtimesoft;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.table.DefaultTableModel;
import timesoft.control.TimesoftManager;
import timesoft.model.Marcacion;
import timesoft.model.Registro;

/**
 *
 * @author HugoAZ
 */
public class MarcacionesTableModel  extends DefaultTableModel {




    private String[] labels = new String[]{ "ID",
                                            "PERNR",
                                            "DIV",
                                            "OK",
                                            "Captura SIBO",
                                            "Procesado TimeHR",
                                            "# Repr.",
                                            "Op",
                                            "Subd",
                                            "Descripción",
                                            "Nombres",
                                            "ABWGR"
                                          };
    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");


    public MarcacionesTableModel(List<Registro> pMarcs ){
        dataVector = new Vector<Registro>(pMarcs);
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
        
        if ( c == 3 )
            return Icon.class;
        else
            return String.class;
    }

    @Override
    public String getColumnName(int col) {
        return labels[col];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Marcacion r = (Marcacion) dataVector.elementAt(rowIndex);
        switch ( columnIndex )
        {
            case 0:
                return r.getId();
            case 1:
                return r.getPernr();
            case 2:
                return r.getSubdivision();
            case 3:
                if ( r.getRetorno() != null )
                {
                    if ( r.getRetorno().equals("S"))
                        return Constantes.GREENLIGHT;
                    else if ( r.getRetorno().equals("E"))
                        return Constantes.REDLIGHT;
                    else
                        return Constantes.WHITE_IMAGE;
                }
                else
                    return Constantes.WHITE_IMAGE;
            case 4:
                try
                {
                    return df.format( r.getFechaCaptura());
                }
                catch ( Exception ex )
                {
                    return "Fecha inválida";
                }
            case 5:
                if ( r.getCntrl() != null )
                    return df.format(r.getCntrl());
                else
                    return "";
            case 6:
                if ( r.getCntdr() != null )
                    return r.getCntdr();
                else
                    return "";
            case 7:
                return r.getOperador();
            case 8:
                return r.getBtrtl();
            case 9:
                return r.getMensaje();
            case 10:
                return TimesoftManager.getNombres(r);
            case 11:
                return r.getAbwgr();
            default:
                return null;
        }

    }

}
