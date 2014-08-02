/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timesoft.exceptions;

/**
 *
 * @author Sensei
 */
public class SapDllNotFound extends Exception {

    private String msg;

    @Override
    public String getMessage() {
        return "Probablemente el archivo sapjco3.dll no se encuentra en la carpeta Windows/System32.";
    }


}
