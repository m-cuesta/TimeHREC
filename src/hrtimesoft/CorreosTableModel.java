/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hrtimesoft;

import com.inga.exception.BDException;
import com.inga.exception.NoConnectionException;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import timesoft.control.TimesoftManager;
import timesoft.model.CriteriosSubdivisionCorreo;
import timesoft.model.SubdivisionCorreo;

/**
 *
 * @author Camilo
 */
public class CorreosTableModel extends DefaultTableModel {


    private static String[] labels = new String[]{ "División de Personal", "Correo" };
    private static Logger log = Logger.getLogger(CorreosTableModel.class);

    public CorreosTableModel() {
        super();

        try
        {
            TimesoftManager tm = HRTimesoftApp.tm;
            this.dataVector = new Vector<SubdivisionCorreo>( tm.findSubdivisionCorreo(new CriteriosSubdivisionCorreo()));
        }
        catch ( Exception ex )
        {
            log.error( ex.getMessage() );
        }
    }

    @Override
    public String getColumnName(int column) {
        return labels[column];
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
    public Class getColumnClass(int column) {
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        SubdivisionCorreo s = (SubdivisionCorreo) dataVector.get(rowIndex);
        switch ( columnIndex )
        {
            case 0:
                return s.pk.getSubdivision();
            case 1:
                return s.pk.getCorreo();
            default:
                return null;
        }
    }

    public void add(SubdivisionCorreo reg) throws NoConnectionException, BDException, CorreoYaExiste {
        TimesoftManager tm = HRTimesoftApp.tm;

        if ( tm.existeCorreo(reg) )
            throw new CorreoYaExiste();

        tm.addSubdivisionCorreo(reg);
        dataVector.add( reg );
        this.fireTableRowsInserted( dataVector.size() - 1 ,
                                    dataVector.size() - 1
                                  );
        
    }

    public void delete(int row) throws NoConnectionException, BDException {
        TimesoftManager tm = HRTimesoftApp.tm;
        SubdivisionCorreo sc = this.get(row);
        tm.deleteSubdivisionCorreo( sc.pk.getSubdivision(), sc.pk.getCorreo() );
        dataVector.remove(row);
        this.fireTableRowsDeleted(row, row);
    }

    public SubdivisionCorreo get(int row) {
        return (SubdivisionCorreo) dataVector.get(row);
    }


}
