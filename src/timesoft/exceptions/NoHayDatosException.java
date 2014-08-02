/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timesoft.exceptions;

/**
 *
 * @author Usuario
 */
public class NoHayDatosException extends Exception {

    @Override
    public String getMessage() {
        return "No hay datos bajo ese criterio.";
    }

}
