/*
 * SubdivisionCorreoDAO.java
 *
 * Created on Sat Dec 04 23:12:34 COT 2010
 * by DaoGen2
 * Author: Camilo Cuesta
 *
 */

package timesoft.dao;

import com.inga.exception.RegistroNoExisteException;
import timesoft.model.CriteriosSubdivisionCorreo;
import java.util.List;
import timesoft.model.SubdivisionCorreo;

/** Models a SubdivisionCorreoDAO
 *
 */
public interface SubdivisionCorreoDAO {

    /** Busca por criterio de bu'squeda */
    public List<SubdivisionCorreo> find(CriteriosSubdivisionCorreo criteria);
    
    /** Obtiene un registro por llave primaria */
    public SubdivisionCorreo get(java.lang.String subdivision, java.lang.String correo) throws RegistroNoExisteException;
    
    /** Crea una nuevo registro */
    public Integer create(SubdivisionCorreo registro);
    
    /** Borra un registro */
    public int delete(java.lang.String subdivision, java.lang.String correo);
    
}