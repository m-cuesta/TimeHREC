/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.dao;

import java.util.List;
import timesoft.model.Criterios;
import timesoft.model.Registro;

/**
 *
 * @author Camilo
 */
public interface RegistroDAO {
    
    /** Busca por criterio de bu'squeda */
    public List<Registro> find(Criterios criteria, int firstResult, int maxResults );

    /** Modifica un registro */
    public int update(Registro registro);

    public int count(Criterios criterios );
    
    public List<Integer> findOnlyIds(Criterios criteria);

}
