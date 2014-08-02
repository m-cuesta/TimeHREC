/*
 * CentroCostoDAO.java
 *
 * Created on Thu Sep 03 15:59:43 COT 2009
 * by DaoGen2
 * Author: Camilo Cuesta
 *
 */

package timesoft.dao;

import timesoft.model.CentroCosto;

/** Models a CentroCostoDAO
 *
 */
public interface CentroCostoDAO {

    /** Crea una nuevo registro */
    public int create(CentroCosto registro);
    
    public int clear();
    
}