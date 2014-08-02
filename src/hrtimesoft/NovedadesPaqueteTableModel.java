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
import timesoft.model.NovedadPaquete;
import timesoft.model.Registro;

/**
 *
 * @author Camilo
 */
public class NovedadesPaqueteTableModel extends DefaultTableModel {


    private String[] labels = new String[]{ "ID",
                                            "PERNR",
                                            "DIV",
                                            "OK",
                                            "Captura",
                                            "Cantidad",
                                            "Reclamado PERNR",
                                            "Reclamado por",
                                            "Tipo",
                                            "Registrado en SIBO",
                                            "Subida a SAP",
                                            "# Rep.",
                                            "Op",
                                            "Subd",
                                            "Descripción",
                                            "Nombres"
                                          };



    SimpleDateFormat simpleDate = new SimpleDateFormat("ddMMyyyy");
    SimpleDateFormat formatedDate = new SimpleDateFormat( SigarUtils.FECHA3 );
    SimpleDateFormat df = new SimpleDateFormat( SigarUtils.FECHA3 + " HH:mm:ss");
    SimpleDateFormat uhrzeit = new SimpleDateFormat("HH:mm:ss");
    SimpleDateFormat horaSibo = new SimpleDateFormat( "HHmmss" );
    SimpleDateFormat dfSibo = new SimpleDateFormat("yyyyMMddHHmmss");


    public NovedadesPaqueteTableModel(List<Registro> pNovedades) {
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
        NovedadPaquete r = (NovedadPaquete) dataVector.get(rowIndex);
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
                    Date ldate = dfSibo.parse( r.getLdate().trim() + " " + r.getLhour() ) ;
                    return df.format(ldate);
                }
                catch ( Exception ex )
                {
                    return "Fecha inválida";
                }
            case 5:
                return String.valueOf(r.getIcantidad());
            case 6:
                return r.getCodreclama();
            case 7:
                return r.getNombreeclama();
            case 8:
                if ( r.getItipo() != null )
                {
                    if ( r.getItipo().equals( timesoft.Constantes.CASINO ))
                        return "CASINO";
                    else if ( r.getItipo().equals( timesoft.Constantes.BOLSA_DE_PAPEL))
                        return "BOLSA";
                    else
                        return "";
                }
                return "";
            case 9:
                try
                {
                    return df.format( r.getFechacrea() );
                }
                catch ( Exception ex )
                {
                    return "";
                }
            case 10:
                try
                {
                    return df.format( r.getCntrl() );
                }
                catch ( Exception ex )
                {
                    return "";
                }
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
