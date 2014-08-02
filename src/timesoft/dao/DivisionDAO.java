/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.dao;

import com.inga.utils.StringItem;
import java.util.List;

/**
 *
 * @author Camilo
 */
public interface DivisionDAO {
    public List<StringItem> getDivisiones();
    public List<StringItem> getSubdivisiones(StringItem division);
    public StringItem getItem(String value);
}
