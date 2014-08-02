/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.dao;

import timesoft.model.UsuarioPlanta;

/**
 *
 * @author usrconsultor19
 */
public interface UsuarioPlantaDAO {
    
    public void create(UsuarioPlanta up);
    
    public void delete(UsuarioPlanta up);
    
    public boolean exists(UsuarioPlanta up);
    
}
