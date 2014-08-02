/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timesoft.exceptions;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Usuario
 */
public class SAPException extends Exception {

    private ArrayList<String> errores;

    public SAPException(List<String> pErrores) {
        super();
        errores = new ArrayList<String>( pErrores );
    }

    @Override
    public String getMessage() {
        StringBuilder buffer = new StringBuilder();
        for ( String e : getErrores() )
            buffer.append( e + "<br/>\n" );
        return buffer.toString();
    }

    /**
     * @return the errores
     */
    public ArrayList<String> getErrores() {
        return errores;
    }



}
