/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timesoft.exceptions;

/**
 *
 * @author Usuario
 */
public class RfcNoDisponible extends Exception {

    private String rfcName;

    public RfcNoDisponible(String pRfcName) {
        super();
        rfcName = pRfcName;
    }

    @Override
    public String getMessage() {
        return "La función " + rfcName + " no está disponible en el servidor SAP";
    }

}
