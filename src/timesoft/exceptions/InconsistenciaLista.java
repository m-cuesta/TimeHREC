/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timesoft.exceptions;

/** Representa una inconsistencia en el número de marcaciones,novedades,etc. encontradas usando
 *  una consulta con "count", y el número de registros realmente encontrados con
 *  el método find del dao de marcaciones, ej: FamiliaMarcacionDAO
 *
 * @author Manuel Cuesta 2010
 */
public class InconsistenciaLista extends Exception {

    private String msg = "El número de registros a procesar es inconsistente con los criterios de búsqueda";


    @Override
    public String getMessage() {
        return msg;
    }

}
