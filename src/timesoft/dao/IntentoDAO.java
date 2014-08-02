/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.dao;

import java.util.List;
import timesoft.model.CriteriosIntento;
import timesoft.model.Intento;

/**
 *
 * @author Camilo
 */
public interface IntentoDAO {
    
    public Long startTry(String firma, int tipoActividad);
    
    public void followUp(Long id,String observacion);
    
    public void summary(Long id, boolean success, String observacion);

    public void summary(Long id, boolean success);
    
    public void addIntentados(Long id, int registrosIntentados );
    
    public void addProcesados(Long id, int registrosProcesados );
    
    public void addConError(Long id, int registrosConError );
    
    public int count(CriteriosIntento criteria);
    
    public List<Intento> find(CriteriosIntento criteria, int firstResult, int maxResults );
    
}
