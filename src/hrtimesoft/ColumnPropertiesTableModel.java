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
public class ColumnPropertiesTableModel  extends AbstractTableModel {


    private String[] labels = new String[]{ "Propiedad", "Valor" };
    private ArrayList<Propiedad> listaPropiedades;


    public ColumnPropertiesTableModel() {
        listaPropiedades = new ArrayList<Propiedad>();
        listaPropiedades.add( new Propiedad("Usuario DB", "user", "" ) );
        listaPropiedades.add( new Propiedad("Pwd DB", "password", "" ) );
        listaPropiedades.add( new Propiedad("Url DB", "url", "jdbc:sqlserver://host;databaseName=db"));
        listaPropiedades.add( new Propiedad("Idioma SAP", "jco.client.lang", "ES"));
        listaPropiedades.add( new Propiedad("Cliente SAP", "jco.client.client", ""));
        listaPropiedades.add( new Propiedad("Usuario SAP","jco.client.user", ""));
        listaPropiedades.add( new Propiedad("Pwd SAP", "jco.client.passwd", ""));
        listaPropiedades.add( new Propiedad("Num Sistema SAP","jco.client.sysnr", "00"));
        listaPropiedades.add( new Propiedad("Host SAP", "jco.client.ashost",""));
        listaPropiedades.add( new Propiedad("Directorio trabajo", "workingDirectory","c:/"));

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
