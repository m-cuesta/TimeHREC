/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.exceptions;

/**
 *
 * @author usrConsultor19
 */
public class NoTienePlantasAsignadas extends Exception {
    
    @Override
    public String getMessage() {
        return "El usuario no tiene plantas asignadas";
    }
    
}
