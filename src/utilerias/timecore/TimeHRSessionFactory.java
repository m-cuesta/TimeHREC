/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilerias.timecore;

import com.inga.utils.SigarUtils;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

/**
 *
 * @author Camilo
 */
public class TimeHRSessionFactory extends AnnotationSessionFactoryBean {
    
    
    private static Logger log = Logger.getLogger(DataSourceProvider.class.getName());
    
    public TimeHRSessionFactory() {
        try 
        {
            Properties props = SigarUtils.loadProperties("config.properties");
        System.out.println( "Dialect:" + props.getProperty("hibernate.dialect") );
            setHibernateProperties( props );
        }
        catch ( Exception ex )
        {
            log.error( SigarUtils.stackTraceString(ex) );
        }
    }
    
}
