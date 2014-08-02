/*
 * HRTimesoftApp.java
 */

package hrtimesoft;

import com.inga.utils.Registro;
import cryptowerk.Cryptowerk;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import timesoft.Constantes;
import timesoft.Main;
import timesoft.control.TimesoftManager;


/**
 * The main class of the application.
 */
public class HRTimesoftApp extends SingleFrameApplication {

    public static TimesoftManager tm;
    public static Properties props;
    public static final String VERSION = "8";
    private static Logger log = Logger.getLogger(HRTimesoftApp.class);

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        show(new HRTimesoftView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of HRTimesoftApp
     */
    public static HRTimesoftApp getApplication() {
        return Application.getInstance(HRTimesoftApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) throws Exception {

        log.info( "Timecore version: " + Constantes.VERSION );
        log.info( "TimeHR Ecuador Familia: " + VERSION );
        
    try
    {
      props = Cryptowerk.readProperties("timesoft.properties", Constantes.KEY);
      
      log.info("URL: " + props.getProperty("url"));
      log.info("Client: " + props.getProperty("jco.client.client"));
      log.info("ASHost:" + props.getProperty("jco.client.ashost"));
      log.info("User:" + props.getProperty("jco.client.user"));
      
      TimesoftManager.resetProvider(props);
      TimesoftManager.resetSAPConnection();
    }
    catch (Exception e)
    {
      System.out.println("No hay conexión a SAP");
    }
    try
    {
      ApplicationContext ctx = new ClassPathXmlApplicationContext("META-INF/beans.xml");
      
      String[] names = ctx.getBeanDefinitionNames();
      tm = (TimesoftManager)ctx.getBean("timesoftManager");
      log.info("Manager created");
      Main.setMailConfiguration();
      
      Registro.setLevel(2);
    }
    catch (Exception ex)
    {
      System.out.println(ex.getMessage());
      tm = null;
    }
    launch(HRTimesoftApp.class, args);
  }
    
}
