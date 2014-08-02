/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hrtimesoft;

import java.util.ArrayList;
import java.util.Properties;
import javax.swing.table.AbstractTableModel;
import timesoft.model.Propiedad;

/**
 *
 * @author ccuesta
 */
public class MailPropertiesModel  extends AbstractTableModel {


    private String[] labels = new String[]{ "Propiedad", "Valor" };
    private ArrayList<Propiedad> listaPropiedades;


    public MailPropertiesModel(Properties mailProps) {
        listaPropiedades = new ArrayList<Propiedad>();
        listaPropiedades.add( new Propiedad("SMTP", "smtp", mailProps.getProperty("smtp") ) );
        listaPropiedades.add( new Propiedad("Usuario", "from", mailProps.getProperty("from") ) );
        listaPropiedades.add( new Propiedad("Password", "password", mailProps.getProperty("password") ) );

    }

    public int getRowCount() {
        return listaPropiedades.size();
    }

    public int getColumnCount() {
        return labels.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Propiedad st = listaPropiedades.get( rowIndex ) ;
        switch ( columnIndex )
        {
            case 0:
                return st.getLabel();
            case 1:
                return st.getValue();
            default:
                return "";
        }

    }

    @Override
    public Class getColumnClass(int c) {
        return String.class;
    }

    @Override
    public String getColumnName(int col) {
        return labels[col];
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col > 0  )
            return true;
        else
            return false;
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    @Override
    public void setValueAt(Object value, int row, int col) {

        Propiedad st = listaPropiedades.get(row);

        switch ( col )
        {
            case 1:
                st.setValue((String) value );
                break;

        }

        fireTableCellUpdated(row, col);
    }

    /**
     * @return the props
     */
    public Properties getProps() {
        Properties props = new Properties();
        for ( Propiedad st : listaPropiedades )
           props.setProperty(st.getName(), st.getValue() );
        return props;
    }

}
