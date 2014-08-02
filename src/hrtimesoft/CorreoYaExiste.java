/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hrtimesoft;

/**
 *
 * @author Camilo
 */
public class CorreoYaExiste extends Exception {

    public CorreoYaExiste() {
        super();
    }

    @Override
    public String getMessage() {
        return "El correo ya existe";
    }

}
