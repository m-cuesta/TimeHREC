/*
 * MarcacionDAO.java
 *
 * Created on Thu Sep 10 10:03:08 COT 2009
 * by DaoGen2
 * Author: Camilo Cuesta
 *
 */

package timesoft.dao;

import timesoft.model.Marcacion;
import java.util.List;

/** Models a MarcacionDAO
 *
 */
public interface MarcacionDAO extends RegistroDAO {

    public List<Marcacion> getPendientes();

}