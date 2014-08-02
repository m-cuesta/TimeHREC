/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hrtimesoft;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author ccuesta
 */
public class ColumnPropertiesCellRenderer extends DefaultTableCellRenderer {
    
    public ColumnPropertiesCellRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, 
                                                   Object value, 
                                                   boolean isSelected, 
                                                   boolean hasFocus, 
                                                   int row, 
                                                   int column) {
        
        Component renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if ( column == 0  )
        {
            renderer.setForeground(Color.black);
            renderer.setBackground(Color.lightGray);
        }
        else
        {
            if ( isSelected )
            {
                renderer.setForeground( table.getSelectionForeground() );
                renderer.setBackground( table.getSelectionBackground() );
            }
            else
            {
                renderer.setForeground( table.getForeground() );
                renderer.setBackground( table.getBackground() );
            }

        }


        return this;        
    }
    

}
