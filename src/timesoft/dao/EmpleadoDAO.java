/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.dao;

import java.util.List;
import timesoft.model.CriteriosEmpleado;
import timesoft.model.Empleado;

/**
 *
 * @author Camilo
 */
public interface EmpleadoDAO {
    
    public List<Empleado> findEmployees(int maxResults);
    public List<Empleado> find(CriteriosEmpleado criteria);
    
}
