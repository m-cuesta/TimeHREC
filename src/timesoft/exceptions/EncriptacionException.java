/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timesoft.exceptions;

/**
 *
 * @author Usuario
 */
public class EncriptacionException extends Exception {


    public EncriptacionException(  ) {
        super();
    }

    /**
     * @return the message
     */
    @Override
    public String getMessage() {
        return "Error de seguridad";
    }



}
