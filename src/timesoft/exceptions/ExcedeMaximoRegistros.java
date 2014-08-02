/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timesoft.exceptions;

/**
 *
 * @author Usuario
 */
public class ExcedeMaximoRegistros extends Exception {

    private int numRegs;

    public ExcedeMaximoRegistros(int pNumRegs) {
        numRegs = pNumRegs;
    }


    @Override
    public String toString() {
        return "La cantidad de registros a procesar excede el máximo. Registros a procesar: " + numRegs;
    }

}
