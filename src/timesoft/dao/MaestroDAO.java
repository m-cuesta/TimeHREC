/*
 * MaestroDAO.java
 *
 * Created on Fri Aug 28 14:15:09 COT 2009
 * by DaoGen2
 * Author: Camilo Cuesta
 *
 */

package timesoft.dao;

import java.util.List;
import timesoft.model.Maestro;

/** Models a MaestroDAO
 *
 */
public interface MaestroDAO {

    /** Crea una nuevo registro */
    public Integer create(Maestro registro) ;
    public Maestro get(String pernr);
    public List<String> listPernrs();
    public void delete(Maestro m);
    public int clear();
    /** Retorna el número de registros de la tabla */
    public int count();

    
}