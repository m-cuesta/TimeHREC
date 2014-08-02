/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.dao;

import java.util.List;
import timesoft.model.Usuario;

/**
 *
 * @author Camilo
 */
public interface UsuarioDAO {
    
    public List<Usuario> list();
    
    public void create(Usuario usr);
    
    public Usuario get(String login);
    
    public void update(Usuario usr);
    
    public void delete(String login);
    
}
