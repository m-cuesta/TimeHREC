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
import timesoft.model.Maestro;

/**
 *
 * @author usrConsultor19
 */
public class MaestroTableModel extends DefaultTableModel {
    


    private String[] labels = new String[]{ "PERNR",   //0
                                            "OK",     //1
                                            "VORNA",  //2
                                            "NAME2",  //3
                                            "NACHN",  //4
                                            "NACH2",  //5
                                            "KOSTL",  //6
                                            "DESCCC", //7
                                            "WRKS",   //8
                                            "DESCDIV", //9
                                            "BTRTL",   //10
                                            
                                            "DESCSUBDIV", //11
                                            "TIPOEMPL",  //12
                                            "NUMDOCIDE", //13
                                            "BUKRS",     //14
                                            "USRID",     //15
                                            "STATUS",    //16
                                            "COD1",      //17
                                            "COD2",      //18
                                            "SUPER",     //19
                                            "GESCH",  //20
                                            "FECIN",//21
                                            "GBDAT",//22
                                            "STRAS",//23
                                            "TELNR",//24
                                            "CELUL",//25
                                            "FAX",
                                            "USRIDLONG",
                                            
                                          };
    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");


    public MaestroTableModel(List<Maestro> pMarcs ){
        dataVector = new Vector<Maestro>(pMarcs);
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
        
        if ( c == 1 )
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
        Object value = internalValueAt(rowIndex,columnIndex);
        if ( value == null )
            return "NULL";
        else
            return value;
    }
    
    private Object internalValueAt(int rowIndex, int columnIndex) {
        Maestro r = (Maestro) dataVector.elementAt(rowIndex);
        switch ( columnIndex )
        {
            case 0:
                return r.getPernr();
            case 1:
                if ( r.getEstado() != null )
                {
                    if ( r.getEstado().equals("S"))
                        return Constantes.GREENLIGHT;
                    else if ( r.getEstado().equals("E"))
                        return Constantes.REDLIGHT;
                    else
                        return Constantes.WHITE_IMAGE;
                }
                else
                    return Constantes.WHITE_IMAGE;
            case 2:
                return r.getVorna();
            case 3:
                return r.getName2();
            case 4:
                return r.getNachn();
            case 5:
                return r.getNach2();
            case 6:
                return r.getKostl();
            case 7:
                return r.getDesccc();
            case 8:
                return r.getWrks();
            case 9:
                return r.getDescdiv();
            case 10:
                return r.getBtrtl();
                
                
            case 11:
                return r.getDescsubdiv();
            case 12:
                return r.getTipoempl();
            case 13:
                return r.getNumdocide();
            case 14:
                return r.getBukrs();
            case 15:
                return r.getUsrid();
            case 16:
                return r.getStatus();
                
            case 17:
                return r.getCodempleadouno();
            case 18:
                return r.getCodempleadodos();
            case 19:
                return r.getSuperusuario();
            case 20:
                return r.getGesch();
                
                
            case 21:
                return r.getFecin();
            case 22:
                return r.getGbdat();
            case 23:
                return r.getStras();
            case 24:
                return r.getTelnr();
            case 25:
                return r.getCelul();
            case 26:
                return r.getFax();
            case 27:
                return r.getUseridLong();
            default:
                return null;
        }

    }
    
    
}
