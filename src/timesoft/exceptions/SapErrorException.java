/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timesoft.exceptions;

import java.util.ArrayList;
import java.util.List;
import timesoft.model.SapError;

/**
 *
 * @author Usuario
 */
public class SapErrorException extends Exception {

    private ArrayList<SapError> errores;

    public SapErrorException(List<SapError> pErrores) {
        super();
        errores = new ArrayList<SapError>(pErrores);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for ( SapError e : errores )
        {
            sb.append( e );
            sb.append( "\n" );
        }
        return sb.toString();
    }

    public ArrayList<SapError> getErrores() {
        return errores;
    }

}
