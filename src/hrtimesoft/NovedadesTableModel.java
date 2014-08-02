/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hrtimesoft;

import com.inga.utils.SigarUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.table.DefaultTableModel;
import timesoft.control.TimesoftManager;
import timesoft.model.Novedad;
import timesoft.model.Registro;

/**
 *
 * @author Camilo
 */
public class NovedadesTableModel extends DefaultTableModel {


    private String[] labels = new String[]{ "ID",
                                            "PERNR",
                                            "DIV",
                                            "OK",
                                            "Fecha Ini",
                                            "Fecha Fin",
                                            "Hora Ini",
                                            "Hora Fin",
                                            "C. Costo",
                                            "VTKEN",
                                            "Proc. SAP",
                                            "# Rep.",
                                            "Op",
                                            "Subd",
                                            "Descripción",
                                            "Nombres"
                                          };

    SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat formatedDate = new SimpleDateFormat( SigarUtils.FECHA3 );
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat uhrzeit = new SimpleDateFormat("HH:mm:ss");
    SimpleDateFormat horaSibo = new SimpleDateFormat( "HHmmss" );


    public NovedadesTableModel(List<Registro> pNovedades) {
        dataVector = new Vector<Registro>( pNovedades );
    }

    @Override
    public Class getColumnClass(int c) {
        if ( c == 3 )
            return Icon.class;
        else
            return String.class;
    }

    @Override
    public int getColumnCount() {
        return labels.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Novedad r = (Novedad) dataVector.get(rowIndex);
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
                    Date ldate = simpleDate.parse( r.getBegda().trim() );
                    return formatedDate.format(ldate);
                }
                catch ( Exception ex )
                {
                    return "Fecha inválida";
                }
            case 5:
                try
                {
                    Date ldate = simpleDate.parse( r.getEndda().trim() );
                    return formatedDate.format(ldate);
                }
                catch ( Exception ex )
                {
                    return "Fecha inválida";
                }
            case 6:
                try
                {
                    Date beguz = horaSibo.parse( r.getBeguz() );
                    return uhrzeit.format(beguz);
                }
                catch ( Exception ex)
                {
                    return "";
                }
            case 7:
                try
                {
                    Date enduz = horaSibo.parse( r.getEnduz() );
                    return uhrzeit.format(enduz);
                }
                catch ( Exception ex)
                {
                    return "";
                }
            case 8:
                return r.getKostl();
            case 9:
                return r.getVtken();
            case 10:
                if ( r.getCntrl() != null )
                    return df.format(r.getCntrl());
                else
                    return "";
            case 11:
               return r.getCntdr();
            case 12:
                return r.getOperador();
            case 13:
                return r.getBtrtl();
            case 14:
                return r.getMensaje();
            case 15:
                return TimesoftManager.getNombres(r);
            default:
                return null;
        }
    }


    @Override
    public String getColumnName( int column ) {
        return labels[column];
    }
    
}
