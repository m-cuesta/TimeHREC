/*
 * CiudadDAO.java
 *
 * Created on Thu Oct 08 10:16:11 COT 2009
 * by DaoGen2
 * Author: Camilo Cuesta
 *
 */

package timesoft.dao;

import com.inga.exception.RegistroNoExisteException;
import timesoft.model.Ciudad;

/** Models a CiudadDAO
 *
 */
public interface CiudadDAO {

    /** Obtiene un registro por llave primaria */
    public Ciudad get(java.lang.String intpkidciudad) throws RegistroNoExisteException;
    
}