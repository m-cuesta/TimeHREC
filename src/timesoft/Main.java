/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timesoft;

import com.inga.security.User;
import com.inga.utils.SigarUtils;
import com.sap.conn.jco.JCoException;
import cryptowerk.Cryptowerk;
import hrtimesoft.HRTimesoftApp;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.mail.MessagingException;
import mailhelper.MailHelper;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import timesoft.control.TimesoftManager;
import timesoft.exceptions.NoHayDatosException;
import timesoft.exceptions.SapDllNotFound;
import timesoft.model.Maestro;
import timesoft.model.SubdivisionCorreo;
import timesoft.model.Usuario;

/**
 *
 * @author Usuario
 */
public class Main {

    public static byte[] KEY = "ASD23G23G2365264GEGWEGWE_2323ETRE00235AGEGEW".getBytes();

    private static Logger log = Logger.getLogger(Main.class.getName());
    private static MailHelper mh;
    private static Properties mailProps    ;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        
        if (args.length == 0) {
            HRTimesoftApp.main(args);
            return;
        }
        
        log.info( "TimeHR versión " + Constantes.VERSION );

        try
        {
            setSapConnection();
            setMailConfiguration();

            Main m = new Main();
            m.procesarFamilia(args);
        }
        catch ( Exception ex )
        {
            log.error( SigarUtils.stackTraceString(ex) );
        }


    }

    /**
     * @return the mh
     */
    public static MailHelper getMh() {
        return mh;
    }

    /**
     * @param aMh the mh to set
     */
    public static void setMh(MailHelper aMh) {
        mh = aMh;
    }

    /**
     * @return the mailProps
     */
    public static Properties getMailProps() {
        return mailProps;
    }

    /**
     * @param aMailProps the mailProps to set
     */
    public static void setMailProps(Properties aMailProps) {
        mailProps = aMailProps;
    }

    public void procesarFamilia(String args[]) throws FileNotFoundException, IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, ClassNotFoundException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, SapDllNotFound, JCoException {
        
        Thread sincronizador = new Thread(){
            @Override
            public void run() {

                TimesoftManager tm = null;

                
                try
                {
                   tm = Main.getManager();
                   List<Maestro> maestros = tm.syncMaestrosFromSAP();

                   List<String> cadenasMaestro = new ArrayList<String>();
                   for (Maestro maestro : maestros) {
                       cadenasMaestro.add(maestro.toString());
                   }
                   String tarea = "Procesando maestros";
                   List<String> encabezado = new ArrayList<String>();
                   encabezado.add( tarea );
                   
                   tm.enviarCorreoAAdministradores(cadenasMaestro, encabezado, tarea);
                   
                   
                }
                catch ( Exception ex )
                {
                    log.error( SigarUtils.stackTraceString(ex) );
                    notificar( "Procesando maestros", SigarUtils.stackTraceString(ex) );
                }

            }
        };

        Thread thNovedades = new Thread(){
            @Override
            public void run() {
                try
                {
                   TimesoftManager tm = Main.getManager();
                   tm.preValidarRegistros(Constantes.NOVEDADES);
                   tm.procesarRegistrosBatch(Constantes.NOVEDADES);
                }
                catch ( NoHayDatosException nex ) 
                {
                    notificar( "Procesando novedades", "No se encontraron novedades nuevas." );
                }
                catch ( Exception ex )
                {
                    log.error( SigarUtils.stackTraceString(ex) );
                    notificar( "Procesando novedades", SigarUtils.stackTraceString(ex) );
                }
            }

        };


        Thread thOtros = new Thread(){
            @Override
            public void run() {
                try
                {
                   TimesoftManager tm = Main.getManager();
                   tm.preValidarRegistros(Constantes.NOVEDADES_PAQUETE);
                   tm.procesarRegistrosBatch(Constantes.NOVEDADES_PAQUETE);
                }
                catch ( NoHayDatosException nex ) 
                {
                    notificar( "Procesando novedades de paquete", "No se encontraron novedades casino nuevas.");
                }
                catch ( Exception ex )
                {
                    log.error( SigarUtils.stackTraceString(ex) );
                    notificar( "Procesando novedades paquete", SigarUtils.stackTraceString(ex) );
                }
            }

        };

        Thread thMarcaciones = new Thread(){
            @Override
            public void run() {
                try
                {
                   TimesoftManager tm = Main.getManager();
                   tm.preValidarRegistros(Constantes.MARCACIONES);
                   tm.procesarRegistrosBatch(Constantes.MARCACIONES);
                }
                catch ( NoHayDatosException nex ) 
                {
                    notificar( "Procesando marcaciones", "No se encontraron marcaciones nuevas." );
                }
                catch ( Exception ex )
                {
                    log.error( SigarUtils.stackTraceString(ex) );
                    notificar( "Procesando marcaciones", SigarUtils.stackTraceString(ex) );
                }
            }
            
        };


        String strArgs = "";
        log.info("Ejecutando TimeHR con parámetros: " + args[0] );
        strArgs = args[0].toLowerCase();

        if ( strArgs.contains("s") )
            sincronizador.start();
        if ( strArgs.contains("m"))
            thMarcaciones.start();
        if ( strArgs.contains("n"))
            thNovedades.start();
        


    }

    
    private static void notificar(String tarea, String mensaje ) {
        List<String> contenido = new ArrayList<String>();
        contenido.add( mensaje );
        try
        {
           TimesoftManager tm = Main.getManager();

           if ( tm != null )
           {
               List<String> encabezado = new ArrayList<String>();
               encabezado.add( tarea );
               tm.enviarCorreoAAdministradores(contenido, encabezado, tarea );
           }
        }
        catch ( Exception ex )
        {
            
        }
    }

    public static void enviarCorreo(String recipient, String subject, List<String> mensajes) throws MessagingException {

        StringBuilder buffer = new StringBuilder();

        SimpleDateFormat df = new SimpleDateFormat( SigarUtils.FECHA1 );


        buffer.append( "<font face=\"Calibri\"><b>Mensaje de TimeHR, " + df.format(new Date()) + "</b></font><br/>\n\n" );
        
        buffer.append("<p><font face=\"Calibri\">");
        for ( String s : mensajes )
            buffer.append(  s  + "<br/>");
        buffer.append("</font></p>");
        
        buffer.append("<p>&nbsp;</p>");
        buffer.append("<p>&nbsp;</p>");
        buffer.append("<hr/>");

        buffer.append("<p><font face=\"Calibri\">");
        buffer.append("Este es un mensaje auto generado");
        buffer.append("</font></p>");
        
        buffer.append("</div>");


        String msg = buffer.toString();

        mh.send( recipient, subject, msg );



    }

    
    public static void setMailConfiguration() {
        try
        {
            mailProps = null;
            mailProps = Cryptowerk.readProperties( "mail.properties", KEY );
            mh = new MailHelper( mailProps );
        }
        catch ( Exception mex )
        {
            log.error( "No se pudo leer el archivo de configuración de correo: " + mex );
            mh = null;
        }
        
    }
    
    public static TimesoftManager getManager() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
        "META-INF/beans.xml");
        final TimesoftManager tm = 
                (TimesoftManager) ctx.getBean("timesoftManager");
        System.out.println("Manager OK");
        User user = new User();
        user.setLogin("batch");
        tm.setUser( user );
        
        return tm;
    }
    
    public static void setSapConnection()  {
        try
        {
            // Set sap connection
            Properties props = Cryptowerk.readProperties("timesoft.properties",KEY);
            TimesoftManager.resetProvider(props);
            TimesoftManager.resetSAPConnection();
        }
        catch ( Exception ex )
        {
            log.error( "Error setting sap connection" );
            log.error( SigarUtils.stackTraceString(ex) );
        }
    }
    
    private static void createUsuarios() {
        TimesoftManager tm = getManager();
        
        if ( tm.getUsuario("batch") == null )
        {
            Usuario usr = new Usuario("batch", "23R23SEFWE3323", "admin" );
            tm.createUsuario(usr);
        }
        
        SubdivisionCorreo sub = new SubdivisionCorreo();
        sub.pk.setCorreo("camilocuesta@hotmail.se");
        sub.pk.setSubdivision("GENERAL");
        if ( ! tm.existeCorreo(sub) )
            tm.addSubdivisionCorreo(sub);

        
        
    }


}

