/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.dao;

import java.util.List;
import timesoft.model.Registro;

/**
 *
 * @author Camilo
 */
public interface UtilDAO {


    public List<String> getSubdivisiones(String pernr);

    public List<String> listSubdivisiones();
    
    public void create(Registro r);
    
    public void delete(Registro r);
        
    
}
