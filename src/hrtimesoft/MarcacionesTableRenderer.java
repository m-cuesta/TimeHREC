/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hrtimesoft;

import javax.swing.Icon;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author HugoAZ
 */
public class MarcacionesTableRenderer extends DefaultTableCellRenderer {



/*
    @Override
 public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {

	super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	return this;
 }
*/

    @Override
    protected void setValue(Object value)
    {

        if (value != null && value instanceof Icon)
        {
            setIcon((Icon)value);
        }
        else
            super.setValue(" ");
    }
}
