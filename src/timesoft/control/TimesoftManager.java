/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timesoft.control;

import com.inga.exception.BDException;
import com.inga.exception.RegistroNoExisteException;
import com.inga.security.User;
import com.inga.utils.DateRange;
import com.inga.utils.SigarUtils;
import com.inga.utils.SqlClauseHelper;
import com.inga.utils.StringItem;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.persistence.EntityManagerFactory;
import javax.swing.JLabel;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import timesoft.Constantes;
import timesoft.Main;
import timesoft.dao.CentroCostoDAO;
import timesoft.dao.CiudadDAO;
import timesoft.dao.DivisionDAO;
import timesoft.dao.EmpleadoDAO;
import timesoft.dao.IntentoDAO;
import timesoft.dao.MaestroDAO;
import timesoft.dao.MarcacionDAO;
import timesoft.dao.NovedadDAO;
import timesoft.dao.NovedadPaqueteDAO;
import timesoft.dao.RegistroDAO;
import timesoft.dao.SubdivisionCorreoDAO;
import timesoft.dao.UsuarioDAO;
import timesoft.dao.UsuarioPlantaDAO;
import timesoft.dao.UtilDAO;
import timesoft.exceptions.EncriptacionException;
import timesoft.exceptions.NoHayDatosException;
import timesoft.exceptions.RfcNoDisponible;
import timesoft.exceptions.SapDllNotFound;
import timesoft.model.CentroCosto;
import timesoft.model.Criterios;
import timesoft.model.CriteriosEmpleado;
import timesoft.model.CriteriosIntento;
import timesoft.model.CriteriosSubdivisionCorreo;
import timesoft.model.Empleado;
import timesoft.model.Intento;
import timesoft.model.Maestro;
import timesoft.model.Marcacion;
import timesoft.model.Novedad;
import timesoft.model.NovedadPaquete;
import timesoft.model.Registro;
import timesoft.model.Reporte;
import timesoft.model.RespuestaSAP;
import timesoft.model.SapError;
import timesoft.model.SubdivisionCorreo;
import timesoft.model.Usuario;
import timesoft.model.UsuarioPlanta;

/**
 *
 * @author Manuel C. Cuesta
 */
@Service
public class TimesoftManager {
    
    private EntityManagerFactory emf;

    public static final String SAP_CONFIG = "SAP Netzwerk" ;
    
    @Autowired SessionFactory sessionFactory;
    @Autowired MarcacionDAO marcDao;
    @Autowired UtilDAO famDao; 
    @Autowired IntentoDAO intentoDao;
    @Autowired NovedadDAO novedadDao;
    @Autowired SubdivisionCorreoDAO correoDao;
    @Autowired CiudadDAO ciudadDao;
    @Autowired NovedadPaqueteDAO novedadPaqueteDao;
    @Autowired MaestroDAO maestrosDao;
    @Autowired CentroCostoDAO ccDao;
    @Autowired UsuarioDAO usuarioDao;
    @Autowired EmpleadoDAO empleadoDao;
    @Autowired UsuarioPlantaDAO usuarioPlantaDao;
    @Autowired DivisionDAO divisionDao;
    

    private User user;
    private Properties props;
    private static JCoDestination destination;
    private JLabel status;
    private static TSDestinationProvider provider = null;
    private static final SimpleDateFormat sapDatum = new SimpleDateFormat( Constantes.DATUM_SAP_FORMAT );
    private static final SimpleDateFormat sapUhr = new SimpleDateFormat( Constantes.UHR_SAP_FORMAT );
    private static final SimpleDateFormat siboFormat = new SimpleDateFormat( SigarUtils.FECHA1 );

    private static Logger log = Logger.getLogger( TimesoftManager.class.getName() );

    private boolean running = false;
    private boolean detenido = false;
    private boolean coneccionSAPOK = false;
    
    
    
    NumberFormat contadorFormat = NumberFormat.getIntegerInstance();
    
    public TimesoftManager() throws EncriptacionException, IOException, BDException, JCoException  {
        status = null ;
        contadorFormat.setMaximumIntegerDigits(8);
        contadorFormat.setMinimumIntegerDigits(8);
        contadorFormat.setGroupingUsed(false);
    }

    public static synchronized void resetProvider( Properties pProps ) throws SapDllNotFound {
        if ( provider == null )
        {
            try
            {
                provider = new TSDestinationProvider();
                provider.addDestination( SAP_CONFIG, pProps );
                com.sap.conn.jco.ext.Environment.registerDestinationDataProvider( provider );
            }
            catch ( Throwable thx )
            {
                throw new SapDllNotFound();
            }
        }
    }

    public static void resetSAPConnection() throws JCoException {
        destination = JCoDestinationManager.getDestination( SAP_CONFIG );
    }

    
    private static void appendRegistroRow(JCoTable inRegs, Registro reg, int tipo ) {
        switch ( tipo ) 
        {
            case Constantes.MARCACIONES:
                Marcacion marc = (Marcacion) reg;
                inRegs.appendRow();
                inRegs.setValue( "PERNR" , marc.getPernr() );
                inRegs.setValue( "BEGDA" , marc.getLdate() );
                inRegs.setValue( "LTIME" , marc.getName2() );
                inRegs.setValue( "SATZA" , marc.getSatza() );
                inRegs.setValue( "DALLF" , marc.getDallf() );
                inRegs.setValue( "CNTDR" , marc.getCntdr() );
                inRegs.setValue( "FSDTS", sapDatum.format(marc.getFechaModificado()) ) ;
                inRegs.setValue( "HSDTS", sapUhr.format(marc.getFechaModificado()) );
                inRegs.setValue( "ID", String.valueOf(marc.getId()) );
                inRegs.setValue( "KOSTL", marc.getKostl() );
                if (marc.getAbwgr() != null && !marc.getAbwgr().isEmpty()) {
                   inRegs.setValue( "ABWGR", marc.getAbwgr() );
                }
                break;
            case Constantes.NOVEDADES:
                Novedad novedad = (Novedad) reg;
                inRegs.appendRow();
                inRegs.setValue( "ID", reg.getId() );
                inRegs.setValue( "PERNR" , reg.getPernr() );
                inRegs.setValue( "AWART" , novedad.getPinco() );
                inRegs.setValue( "BEGDA" , novedad.getBegda() );
                inRegs.setValue( "ENDDA" , novedad.getEndda() );
                inRegs.setValue( "BEGUZ" , novedad.getBeguz() );
                inRegs.setValue( "ENDUZ" , novedad.getEnduz() );
                inRegs.setValue( "KOSTL",  novedad.getKostl() );
                inRegs.setValue( "CNTDR",  novedad.getCntdr() );
                inRegs.setValue( "FSDTS", sapDatum.format(novedad.getFechaModificado()) ) ;
                inRegs.setValue( "HSDTS", sapUhr.format(novedad.getFechaModificado()) );
                break;
            case Constantes.NOVEDADES_PAQUETE:
                NovedadPaquete np = (NovedadPaquete) reg;
                inRegs.appendRow();
                inRegs.setValue( "PERNR" , np.getPernr() );
                inRegs.setValue( "BEGDA", np.getLdate() );
                inRegs.setValue( "SUBTY", np.getItipo() );
                inRegs.setValue( "HMCAS", np.getLhour() );
                inRegs.setValue( "ANZHL", np.getIcantidad() );
                inRegs.setValue( "PERRE", np.getCodreclama() );
                inRegs.setValue( "CNAME", np.getNombreeclama() );
                inRegs.setValue( "CNTDR", np.getCntdr() );
                inRegs.setValue( "ID", String.valueOf(np.getId()) );
                inRegs.setValue( "FSDTS", sapDatum.format(np.getFechaModificado()) ) ;
                inRegs.setValue( "HSDTS", sapUhr.format(np.getFechaModificado()) );
                break;
                    
        }
        
    }
    
    public static boolean ignorarRegistro(Registro reg) {
        
        boolean ignorado = false;
        
        if ( reg == null )
        {
            ignorado = true;
        }
        else
        {
            //Si tiene estado de retorno
            if ( reg.getRetorno() != null )
            {
                //El estado de retorno es exitoso
                if ( Constantes.EXITO.equalsIgnoreCase(reg.getRetorno()) )
                {
                    try
                    {
                        Integer.parseInt(reg.getCntdr());
                        ignorado = true;
                        //Salte este registro
                    }
                    catch(Exception ex)
                    {
                        ignorado = false;
                    }

                }
                else
                {
                    ignorado = false;
                }
            }
            else
            {
                ignorado = false;
            }

        }
        
        return ignorado;
        
    }
    
    public static boolean isExitoso(Registro reg, String retorno, String mensajeRespuesta) {
        
        if ( retorno == null || mensajeRespuesta == null )
        {
            reg.setMensaje(Constantes.NO_HAY_REPUESTA_DEL_SERVIDOR);
            reg.setRetorno(Constantes.ERROR);
            return false;
        }
        
        retorno = retorno.trim();
        mensajeRespuesta = mensajeRespuesta.trim();
        
        if (mensajeRespuesta != null && !mensajeRespuesta.isEmpty() )
        {
            if ( Constantes.EXITO.equalsIgnoreCase(retorno))
            {
                reg.setRetorno(retorno);
                reg.setMensaje(mensajeRespuesta);
                return true;
            }
            else
            {
                if ( mensajeRespuesta.toUpperCase().matches(Constantes.MATCH_REGISTRO_YA_EXISTE) )
                {
                    reg.setRetorno(Constantes.EXITO);
                    reg.setMensaje(mensajeRespuesta);
                    return true;
                }
                else
                {
                    reg.setRetorno(retorno);
                    reg.setMensaje(mensajeRespuesta);
                    return false;
                }
            }
        }
        else
        {
            reg.setMensaje(Constantes.NO_HAY_REPUESTA_DEL_SERVIDOR);
            reg.setRetorno(Constantes.ERROR);
            return false;
        }        
        
    }
    
    private List<RespuestaSAP> procesarEnSAP( List<Registro> marcs, int tipo, Long txId, Reporte reporte) throws RfcNoDisponible, JCoException {
        List<RespuestaSAP> respuestas = new ArrayList<RespuestaSAP>();
        
        ConfiguracionReg config = this.getConfiguracion(tipo);
        JCoFunction func = sapFunction( config.funcionSAP, txId);
        JCoTable inRegs = func.getTableParameterList().getTable( config.parameterTable );

        for ( Registro reg : marcs )
             appendRegistroRow(inRegs, reg, tipo);
        
        func.execute(destination);
        JCoTable returnTable = func.getTableParameterList().getTable("T_RETURN");
        int retornados = returnTable.getNumRows();
        for ( int i = 0; i < retornados; i++ )
        {
            returnTable.setRow( i );
            String mensaje = returnTable.getString("MESSAGE");
            String retorno = returnTable.getString("TYPE");
            RespuestaSAP r = new RespuestaSAP(retorno,mensaje);
            respuestas.add(r);
        }        
        return respuestas;
    }
    
    private List<SapError> internalSubirRegistroSAPFiltrados(List<Registro> marcs, int tipo, Long txId, Reporte reporte ) 
            throws JCoException, RfcNoDisponible
    {
        
        List<Registro> registrosNoIgnorados = new ArrayList<Registro>();
        for ( Registro r : marcs )
        {
            if ( ignorarRegistro(r) )
                reporte.addIgnorados();
            else
                registrosNoIgnorados.add(r);
        }
        return _procesarRegistrosSAP(registrosNoIgnorados,tipo,txId,reporte);
    }
    
    
    public static void incrementarContador(Registro r) {
        int contador = 0;
        try
        {
            contador = Integer.parseInt(r.getCntdr().trim());
        }
        catch ( Exception ex )
        {
            if ( "X".equalsIgnoreCase(r.getCntdr()) )
                contador = 1;
            else
                contador = 0;
        }
        r.setCntdr( String.valueOf(++contador) );
    }
    
    private List<SapError> _procesarRegistrosSAP( List<Registro> marcs, int tipo, Long txId, Reporte reporte ) 
            throws JCoException, RfcNoDisponible
    {
        ConfiguracionReg config = this.getConfiguracion(tipo);
        
        List<RespuestaSAP> respuestas = new ArrayList<RespuestaSAP>();
        
        respuestas = this.procesarEnSAP(marcs, tipo, txId, reporte);
        
        int regProcesados = 0 ;
        int erroneos = 0;

        ArrayList<SapError> regsConError = new ArrayList<SapError>();
        for ( int i = 0; i < marcs.size(); i++ )
        {
            RespuestaSAP r = respuestas.get(i);
            Registro registro = marcs.get(i);
            
            boolean exito = isExitoso(registro,r.getRetorno(),r.getMensaje());

            if (exito)
                reporte.addExitoso();
            else
                reporte.addErroneo();
            
            registro.setOperador(this.user.getLogin());
            
            incrementarContador(registro);
            
            config.regDao.update(registro);
            
            ++regProcesados;
            
            this.showMessage( "Procesando PERNR:" + registro.getPernr() + ". " + reporte.getMessage() ); 
            
            if (!exito)
            {
                regsConError.add( new SapError( registro.getId(), registro.getPernr(), registro.getMensaje() ) );
                erroneos++;
            }

        }
        
        this.addProcesados(txId, regProcesados);
        this.addConError(txId, erroneos);

        return regsConError;

    }

    public List<Maestro> loadMaestrosFromSap() throws JCoException {

        ArrayList<Maestro> result = new ArrayList<Maestro>();

        JCoFunction func =
                destination.getRepository().getFunction( Constantes.FUNCION_MAESTROS );

        func.execute(destination);

        JCoTable tab = func.getTableParameterList().getTable( "ZTHRPT_TBMAESTRO" );

        int rows = tab.getNumRows();

        Maestro o;

        for ( int i = 0; i < rows; i++ )
        {
            tab.nextRow();

            try
            {
                o = new Maestro();

                o.setPernr( tab.getString("PERNR") ); 
                o.setVorna( tab.getString("VORNA") );
                o.setName2( tab.getString("NAME2") );
                o.setNachn( tab.getString("NACHN") );
                o.setNach2( tab.getString("NACH2") );
                o.setKostl( tab.getString("KOSTL") );
                o.setDesccc( tab.getString("KOSTX") );
                o.setWrks( tab.getString("WERKS") );
                o.setDescdiv( tab.getString("NAME1") );
                o.setBtrtl( tab.getString("BTRTL") );
                o.setDescsubdiv( tab.getString("BTEXT") );
                o.setTipoempl( tab.getString("PERNRTIP") );
                o.setNumdocide( tab.getString("ICNUM") );
                o.setBukrs( tab.getString("BUKRS") );
                o.setUsrid( tab.getString("USRID") );
                o.setStatus( tab.getString("STAT2") );
                o.setCodempleadouno( tab.getString("PERA1") );
                o.setCodempleadodos( tab.getString("PERA2") );
                o.setSuperusuario( tab.getString("SUPUS"));

                o.setGesch( tab.getString("GESCH"));
                o.setFecin( tab.getString("FECIN"));
                o.setGbdat( tab.getString("GBDAT"));
                o.setStras( tab.getString("STRAS"));
                o.setTelnr( tab.getString("TELNR"));
                o.setCelul( tab.getString("CELUL"));
                o.setFax(   tab.getString("FAX"));
                o.setUseridLong( tab.getString("USRID_LONG"));

                
                result.add( o );

            }
            catch ( Exception ex )
            {
                showMessage( ex.getMessage() );
            }
        }

        return result;

    }



    private List<CentroCosto> loadCentrosCostoFromSap() throws JCoException {

        ArrayList<CentroCosto> result = new ArrayList<CentroCosto>();

        JCoFunction func =
                destination.getRepository().getFunction( "ZFNHRPT_TBCCOSTOS" );

        func.execute(destination);

        JCoTable tab = func.getTableParameterList().getTable( "ZTHRPT_TBCCOSTOS" );

        int rows = tab.getNumRows();

        CentroCosto o;

        for ( int i = 0; i < rows; i++ )
        {
            tab.nextRow();

            try
            {
                o = new CentroCosto();

                o.setKostl( tab.getString("KOSTL") );
                o.setDesccc( tab.getString("LTEXT") );
                o.setEstado( tab.getString("BKZKP") );

                result.add( o );

            }
            catch ( Exception ex )
            {
                showMessage( ex.getMessage() );
            }
        }

        return result;

    }

    public void syncCentrosCostoFromSAP() throws Exception {

        Long txId = this.startTry(user.getLogin(), Constantes.CENTROSCOSTO );
        int intentados = 0;
        int procesados = 0;
        int erroneos   = 0;

        // Trae los registros de SAP
        String msg1 = "Cargando tabla de Centros de Costo desde SAP " ; 
        showMessage( msg1 );
        this.followUp(txId, msg1 );
        
        List<CentroCosto> regs = null;
        try
        {
            regs = loadCentrosCostoFromSap();
        }
        catch ( Exception jx )
        {
            String msgError = "Error cargando centros costo de SAP:" + SigarUtils.stackTraceString(jx) ;
            this.followUp(txId, msgError);
            showMessage( msgError );
            throw jx;
        }

        intentados = regs.size() ;
        this.addIntentados(txId, intentados);

        List<String> errores = new ArrayList<String>();
        try
        {
            // Borra la tabla en la base de datos
            String msg2 = "Borrando tabla de centros de costo en SIBO..."; 
            showMessage( msg2 );
            this.followUp(txId, msg2 );
            
            ccDao.clear();
            
            // Ingresa los registros a la tabla
            String msg3 = "Descargando tabla centros de costo en SIBO ... "; 
            showMessage( msg3 );
            this.followUp(txId, msg3 );

            StringBuilder sb = new StringBuilder();
            int count = 0;
            for ( CentroCosto m : regs )
            {
                try
                {
                    showMessage( "Descargando centro de costo: " + m.getKostl() + ". " + count++ + " de " + intentados + " registros procesados.");
                    ccDao.create(m);
                    procesados++;
                }
                catch ( Exception ex )
                {
                    erroneos++;
                    String msg4 = m.getKostl() + " " + ex.getMessage() ;
                    showMessage( msg4 );
                    errores.add( msg4 );
                    if ( sb.length() < 600 )
                    {
                        sb.append( msg4 );
                        sb.append( " \n" );
                    }
                }
            }
            
            if ( sb.length() > 0 )
                this.followUp(txId, sb.toString() );
        }
        catch ( Exception ex )
        {
            this.followUp(txId, SigarUtils.stackTraceString( ex ) );
            String msg5 = "Ocurrió un error al sincronizar la tabla de centros de costo. " + ex.getMessage() ;
            showMessage( msg5 );
            throw ex;
        }
        
        finally
        {
            if ( ! errores.isEmpty() )
            {
                List<String> encabezado = new ArrayList<String>();
                encabezado.add("Hubo un error al descargar los siguientes registros en la tabla de centros de costo:");
                this.enviarCorreoAAdministradores(errores, encabezado, "Procesando Centros de Costo" );
            }
        }        

        this.addProcesados(txId, procesados);
        this.addConError(txId, erroneos);
        if ( erroneos <= 0 )
            this.summarySuccess(txId);
        else
            this.summaryError(txId);
            
        showMessage( procesados + " de " + intentados + " centros de costo procesados." );
        
    }
    
    
    
    private static List<CentroCosto> generateCentrosCosto() {
        List<CentroCosto> ccs = new ArrayList<CentroCosto>();
        
        for ( int i = 1; i <= 15; i++ )
        {
            CentroCosto cc = new CentroCosto();
            cc.setKostl(String.valueOf(i));
            cc.setEstado("estado");
            cc.setDesccc("desc");
            ccs.add( cc );
        }
        
        return ccs;
    }
       
    public static String stackTraceString( Exception ex ) {
        try
        {
            ByteArrayOutputStream bout2 = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter( bout2 );
            ex.printStackTrace( writer );
            writer.close();
            String stackTrace = new String( bout2.toByteArray() );
            if ( stackTrace.length() > 255 )
                stackTrace.substring(0,255);
            return stackTrace;
        }
        catch ( Exception ex2 )
        {
            return "Error al imprimir la pila de mensajes de error";
        }
    }
    

    public List<Maestro> syncMaestrosFromSAP() throws Exception {

        Long txId = this.startTry(user.getLogin(), Constantes.MAESTROS );

        int intentados = 0;
        int procesados = 0;
        int erroneos = 0;

        // Trae los registros de SAP
        String msgCarga = "Cargando tabla Maestros desde SAP...";
        showMessage( msgCarga );
        this.followUp(txId, msgCarga);

        List<Maestro> maestros = new ArrayList<Maestro>();

        try
        {
            maestros = loadMaestrosFromSap();
            intentados = maestros.size() ;
        }
        catch ( Exception jx )
        {
            String msgError = "Error cargando maestros de SAP:" + stackTraceString(jx) ;
            this.followUp(txId, msgError);
            showMessage( msgError );
            throw jx;
        }

        this.addIntentados(txId, intentados);
        List<String> errores = new ArrayList<String>();

        try
        {

            // Borra la tabla en la base de datos
            String msgBorra = "Borrando tabla Maestros en SIBO...";
            showMessage( msgBorra );
            this.followUp(txId, msgBorra );
            maestrosDao.clear();
            
            // Ingresa los registros a la tabla
            String msgLoad = "Descargando tabla Maestros en SIBO...";
            showMessage( msgLoad) ;
            this.followUp(txId, msgLoad );

            StringBuilder sb= new StringBuilder();
            int count = 0;
            
            for ( Maestro m : maestros )
            {
                try
                {
                    showMessage( "Descargando PERNR:" + m.getPernr() + ". " + count++ + " de " + intentados + " registros procesados.");
                    maestrosDao.create( m );
                    procesados++;
                }
                catch ( Exception ex )
                {
                    erroneos++;
                    String stackTrace = stackTraceString(ex);
                    String msg = "PERNR " + m.getPernr() + ": " + stackTrace;
                    errores.add( msg );
                    showMessage( msg  );
                    if ( sb.length() < 600 )
                    {
                        sb.append( msg );
                        sb.append( " \n" );
                    }
                }
            }

            if ( sb.length() > 0 )
                this.followUp(txId, sb.toString() );

            String msgDone = "Sincronización de maestros finalizada.";
            showMessage( msgDone ) ;
            procesados = maestrosDao.count();
            
            this.addProcesados(txId, procesados );
            this.addConError(txId, erroneos);
            
            String msg = procesados + " de " + intentados + " maestros procesados. Registros en la tabla: " + procesados ;
            showMessage( msg );
            this.followUp(txId, msg );
            
            if ( erroneos <= 0 )
                this.summarySuccess(txId);
            else
                this.summaryError(txId);
            

        }
        catch ( Exception ex )
        {
            this.followUp(txId, SigarUtils.stackTraceString( ex ) );
            showMessage("Ocurrió un error al sincronizar la tabla de maestros. " );
            throw ex;
        }
        finally
        {
            if ( ! errores.isEmpty() )
            {
                List<String> encabezado = new ArrayList<String>();
                encabezado.add("Hubo un error al descargar los siguientes registros en la tabla de maestros:");
                this.enviarCorreoAAdministradores(errores, encabezado, "Procesando Maestros" );
            }
        }
        
        return maestros;
    }

    public List<Registro> getNovedadesPaquete(Criterios criteria, int firstResult, int maxResults) {
        ConfiguracionReg config = this.getConfiguracion(Constantes.NOVEDADES_PAQUETE);
        return config.regDao.find(criteria,firstResult,maxResults ) ;
    }

    public List<Registro> getNovedades(Criterios criteria,int firstResult, int maxResults) {
        ConfiguracionReg config = this.getConfiguracion(Constantes.NOVEDADES);
        return config.regDao.find(criteria,firstResult, maxResults );
    }

    /**
     * @return the status
     */
    public JLabel getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(JLabel status) {
        this.status = status;
    }

    public void showMessage(String msg) {
        if ( status != null )
            status.setText(msg);
    }
    
    public void preValidarRegistros(int tipo) {
        
        // Cuenta las fechas a futuro
        Criterios findFuturo = new Criterios();
        findFuturo.setIgnoreFechaInvalida(true);
        Date futuro = new Date( System.currentTimeMillis() + 1000*60*60*24*30 );
        findFuturo.setCapturaSibo(new DateRange(futuro,null));
        
        // Cuenta las fechas viejas (menores que 01/01/2000)
        Criterios findViejas = new Criterios();
        findViejas.setIgnoreFechaInvalida(true);
        findViejas.setCapturaSibo( new DateRange(null,Constantes.FECHA_LIMITE_VIEJAS) );

        // Procesa las fechas a futuro
        //this.internalPreValidarRegistros(findFuturo, tipo, " -- Fecha a futuro: " );
        //this.internalPreValidarRegistros(findViejas, tipo, " -- Fecha vieja: " );
        
    }
 
    /**
     * Procesa el batch. Se le pasa el tipo de registro, y busca las marcaciones o novedades nuevas.
     * 
     * @param tipo
     * @throws RfcNoDisponible
     * @throws JCoException
     * @throws NoHayDatosException 
     */
    public void procesarRegistrosBatch(int tipo) throws RfcNoDisponible, JCoException, NoHayDatosException {
        
        Long txId = startTry( user.getLogin(), tipo );
        
        Criterios registrosConError = new Criterios();
        registrosConError.setRetorno("E");
        Calendar diasAtras = Calendar.getInstance();
        diasAtras.add(Calendar.DAY_OF_MONTH, Constantes.DIAS_ATRAS);
        registrosConError.setCapturaSibo(new DateRange(diasAtras.getTime(),null));
        try 
        {
            procesarEnBufer(registrosConError, Constantes.BATCH_SIZE_LARGE, tipo,txId);
        }
        catch ( Exception ex )
        {
            // No hay registros con error.
        }
    
        // Procesar nuevas y lanzar una excepción si no hay nuevas
        Long txIdNuevas = startTry( user.getLogin(), tipo );        
        Criterios nuevas = new Criterios();
        nuevas.setRetorno(Constantes.NUEVA);
        procesarEnBufer(nuevas, Constantes.BATCH_SIZE_LARGE, tipo,txIdNuevas);

    }
    
    public void procesarEnBufer(Criterios criteria, int bufferSize, int tipo, Long txId) throws NoHayDatosException, JCoException, RfcNoDisponible {

        running = true;
        detenido = false;
        
        ConfiguracionReg config = this.getConfiguracion(tipo);
        RegistroDAO regDao = config.regDao;
        
        int numRegs = regDao.count(criteria);
        this.addIntentados(txId, numRegs);
        
        if ( numRegs <= 0 )
        {
            NoHayDatosException nox = new NoHayDatosException();
            this.summarySuccess(txId, "MSG001 No se encontraron registros");
            throw nox;
        }
        
        List<Integer> ids = regDao.findOnlyIds(criteria);

        int numErrores = 0;
        int numPages = (int) Math.ceil( (double) numRegs / (double) bufferSize );
        Reporte procesados = new Reporte();
        int from = 0;
        int to   = 0;
        for ( int page = 0; page < numPages && running ; page++ )
        {
            Criterios criteriosLote = new Criterios();
            from = page*bufferSize;
            to   = page*bufferSize+bufferSize;
            if ( from < 0 || from >= ids.size() )
                from = 0;
            if ( to < 0 || to > ids.size() )
                to = ids.size();
            List<Integer> lote = ids.subList(from, to);
            criteriosLote.setIds(lote);
            List<Registro> regs = regDao.find(criteriosLote, 0, bufferSize);
            List<SapError> errores = this.internalSubirRegistroSAPFiltrados(regs,tipo,txId, procesados );

            if ( ! errores.isEmpty() ) 
            {
                ArrayList<String> encabezado = new ArrayList<String>();
                encabezado.add("Ejecutando " + config.nombre + ":" );
                enviarCorreosASubdivisiones( distribuirMensajesPorSubdivision( errores ), encabezado, null);
            }

            numErrores += errores.size();

            if ( numErrores > Constantes.MAX_ERRORES )
            {
                String m1 = "Excede el límite de errores (" + numErrores + "). Se suspende el proceso";
                this.summaryFail( txId, m1 );
                return;
            }

        }
        
        detenido = true;
        
        if ( running )
        {
            this.summarySuccess( txId, "Tarea completada" );
        }
        else
        {
            this.followUp( txId, "Detenido por usuario" );
        }
        
    }
    
    public List<SubdivisionCorreo> findSubdivisionCorreo(CriteriosSubdivisionCorreo criteria ) {
        return new ArrayList<SubdivisionCorreo>(correoDao.find(criteria));
    }

    public List<String> getSubdivisiones(String pernr) {
        return famDao.getSubdivisiones(pernr);
    }
    
    /**
     * Lista todas las subdivisiones válidas en el sistema. Las saca de la 
     * tabla de empleados, obteniendo los distintos valores de ciudades 
     * de todos los empleados.
     * @return 
     */
    public List<String> listarTodasSubdivisiones() {
        return famDao.listSubdivisiones();
    }

    /**
     * Lista todas las subdivisiones que tienen asociados correos para el
     * reporte. Es posible que liste identificadores de subdivisiones que no
     * existen, si el valor asociado al correo es erróneo.
     * @return 
     */
    public List<String> listarSubdivisiones() {

        List<SubdivisionCorreo> correos = correoDao.find( new CriteriosSubdivisionCorreo() );

        Set<String> subs = new HashSet<String>();

        for ( SubdivisionCorreo sc : correos )
            subs.add( sc.pk.getSubdivision() );

        return new ArrayList<String>( subs );
        
    }

    private List<String> getDirecciones(String pSub) {
        CriteriosSubdivisionCorreo criteria = new CriteriosSubdivisionCorreo();
        criteria.setSubdivision(pSub);
        List<SubdivisionCorreo> scs = correoDao.find(criteria);
        ArrayList<String> correos = new ArrayList<String>();
        for ( SubdivisionCorreo sc : scs )
            correos.add( sc.pk.getCorreo() );
        return correos;
    }

    private HashMap<String,List<String>> distribuirMensajesPorSubdivision(List<SapError> errores) {

        HashMap<String,List<String>> mensajesPorSubdivision = new HashMap<String,List<String>>();

        if( ! mensajesPorSubdivision.containsKey(Constantes.SUBDIVISION_GENERAL) )
            mensajesPorSubdivision.put( Constantes.SUBDIVISION_GENERAL, new ArrayList<String>() );

        String pernr = null;
        List<String> subs1 = null;

        for ( SapError error : errores )
        {
            try
            {
                pernr = error.getPernr();
                if ( pernr != null && ! pernr.isEmpty() )
                    subs1 = getSubdivisiones(pernr);
                else
                    subs1 = new ArrayList<String>();

                
                for ( String sub1 : subs1 )
                {
                    
                    if ( !mensajesPorSubdivision.containsKey(sub1) )
                        mensajesPorSubdivision.put(sub1, new ArrayList<String>() );
                    
                    mensajesPorSubdivision.get( sub1 ).add( error.toString() );
                    
                }

                mensajesPorSubdivision.get( Constantes.SUBDIVISION_GENERAL ).add( error.toString() );

            }
            catch ( Exception ex )
            {

            }
        }

        return mensajesPorSubdivision;
    }
    
    public void enviarCorreoAAdministradores(List<String> contenido, List<String> encabezado, String subject ) {
        Map<String,List<String>> mensajesASubdivisiones = new HashMap<String,List<String>>();
        mensajesASubdivisiones.put(Constantes.SUBDIVISION_GENERAL,  contenido );
        enviarCorreosASubdivisiones( mensajesASubdivisiones, encabezado, subject );
    }

    public void enviarCorreosASubdivisiones(Map<String,List<String>> mensajesPorSubdivision, List<String> encabezado, String subject1 ) {

        
        List<String> direcciones;
        ArrayList<String> contenido;

        for ( String sub : mensajesPorSubdivision.keySet() )
        {

            Long txId = this.startTry(user.getLogin(), Constantes.CORREO );
            
            direcciones = getDirecciones(sub);
            this.followUp(txId, "Subdivision:" + sub + ". Enviando correo a:" + direcciones );
            
            if ( direcciones.isEmpty() )
                continue;
            

            SqlClauseHelper sh = new SqlClauseHelper();

            for ( String destinatario : direcciones )
                sh.append(",", destinatario);

            String destino = sh.toString();
            


            try
            {

                log.info( "Subdivision: " + sub + " " + mensajesPorSubdivision.get(sub).size() + " mensajes.");

                if ( mensajesPorSubdivision.get(sub).isEmpty() )
                {
                    log.info("No hay mensajes a la subdivisión " + sub );
                    continue;
                }
                

                contenido = new ArrayList<String>();

                contenido.addAll( encabezado );

                String subject2 = "TimeHR - Reporte";

                if ( sub.equals( Constantes.SUBDIVISION_GENERAL ))
                {
                    subject2 = subject2 + " General";
                    contenido.add(0, "Reporte general<br/>" );
                }
                else
                {
                    String nombreSubdivision = "";
                    nombreSubdivision = ciudadDao.get(sub).getStrdescripcionciudad();
                    subject2 = subject2 + " " + nombreSubdivision;
                    contenido.add( 0, "Reporte para " + nombreSubdivision + "<br/>");
                }

                contenido.addAll( mensajesPorSubdivision.get( sub ) );

                String subject = "";
                
                if ( subject1 != null )
                    subject = subject1;
                else 
                    subject = subject2;
                
                Main.enviarCorreo( destino, subject, contenido );
                this.summarySuccess(txId);

            }
            catch ( Exception ex )
            {
                String stackTrace = SigarUtils.stackTraceString(ex);
                this.summaryFail(txId, stackTrace );
            }

        }
    }
    
    public List<Registro> getMarcaciones(Criterios criteria, int firstResult, int maxResults ) {
        ConfiguracionReg config = this.getConfiguracion(Constantes.MARCACIONES);
        return config.regDao.find(criteria, firstResult, maxResults  );
    }



    /**
     * @return the props
     */
    public Properties getProps() {
        return props;
    }

    public void deleteSubdivisionCorreo(String subdivision, String correo) {
        correoDao.delete(subdivision,correo);
    }

    public void addSubdivisionCorreo(SubdivisionCorreo reg) {
        correoDao.create(reg);
    }

    public boolean existeCorreo(SubdivisionCorreo reg) {
        try
        {
            SubdivisionCorreo sub = correoDao.get(reg.pk.getSubdivision(), reg.pk.getCorreo());
            if ( sub != null )
                return true;
            else
                return false;
        }
        catch ( RegistroNoExisteException nex )
        {
            return false;
        }
    }
    
    private JCoFunction sapFunction( String funcName, Long txId ) throws RfcNoDisponible {
        JCoFunction func = null;
        try
        {
           func = destination.getRepository().getFunction( funcName );
           return func ;
        }
        catch ( JCoException jex )
        {
            RfcNoDisponible rfx = new RfcNoDisponible( funcName );
            if ( txId != null )
                this.followUp(txId, rfx.getMessage() );
            throw rfx;
        }
    }

    public Reporte subirRegistrosSAP(List<Registro> regs, int tipo ) throws RfcNoDisponible, JCoException {

        running  = true;
        detenido = false;
        
        Long txId = this.startTry( user.getLogin(), tipo );

        this.addIntentados(txId, regs.size() );
        Reporte reporte = new Reporte();
        try
        {
            this.internalSubirRegistroSAPFiltrados(regs, tipo, txId, reporte);
            detenido = true;
        }
        catch ( RfcNoDisponible rex ) 
        {
            this.followUp(txId, "No pudo encontrar la función de SAP: " + rex.getMessage() );
            throw rex;
        }
        catch ( JCoException jox ) {
            this.followUp(txId, "Error al conectarse al servidor de SAP: " + jox.getMessage() );
            throw jox;
        }
        
        this.summarySuccess(txId, "Tarea completada");
        
        return reporte;

    }

    /**
     * @return the emf
     */
    public EntityManagerFactory getEmf() {
        return emf;
    }

    /**
     * @param emf the emf to set
     */
    public void setEmf(EntityManagerFactory emf) {
        this.emf = emf;
    }
    
    public Long startTry(String firma, int tipoActividad) {
        return intentoDao.startTry(firma, tipoActividad);
    }
    
    public void followUp(Long id, String observacion) {
        intentoDao.followUp(id, observacion);
    }

    public void summarySuccess(Long id, String observacion) {
        intentoDao.summary(id, true, observacion);
    }
    
    public void summaryFail(Long id, String observacion) {
        intentoDao.summary(id, false, observacion);
    }
    
    public void summarySuccess(Long id) {
        intentoDao.summary(id, true );
    }
    
    public void summaryError(Long id) {
        intentoDao.summary(id, false );
    }
    
    public void addIntentados(Long id, int registrosIntentados) {
        intentoDao.addIntentados(id, registrosIntentados);
    }

    public void addProcesados(Long id, int registrosProcesados) {
        intentoDao.addProcesados(id, registrosProcesados);
    }

    public void addConError(Long id, int registrosConError) {
        intentoDao.addConError(id, registrosConError);
    }    

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }
    
    public int countIntentos(CriteriosIntento criteria) {
        return intentoDao.count(criteria);
    }
    
    public List<Intento> findIntentos(CriteriosIntento criteria, int firstResult, int maxResults ) {
        return intentoDao.find(criteria, firstResult, maxResults);
    }
    
    public List<Usuario> listUsuarios() {
        return usuarioDao.list();
    }
    
    public void createUsuario(Usuario usr) {
        Long txId = this.startTry( user.getLogin(), Constantes.ADMIN_USUARIOS );
        usuarioDao.create(usr);
        this.summarySuccess(txId, "Usuario "+ usr + " creado");
    }
    
    public Usuario getUsuario(String login) {
        
        return usuarioDao.get(login);
    }
    
    public void updateUsuario(Usuario usr) {
        Long txId = this.startTry( user.getLogin(), Constantes.ADMIN_USUARIOS );
        usuarioDao.update(usr);
        this.summarySuccess(txId, "Usuario " + usr + " modificado");
    }
    
    public void deleteUsuario(String login) {
        Long txId = this.startTry( user.getLogin(), Constantes.ADMIN_USUARIOS );
        usuarioDao.delete(login);
        this.summarySuccess(txId, "Usuario " + login + " eliminado");
    }
    
    private ConfiguracionReg getConfiguracion(int tipo) {
        ConfiguracionReg config = new ConfiguracionReg();
        config.tipo  = tipo;
        
        switch ( tipo )
        {
            case Constantes.MARCACIONES:
                config.clase = Marcacion.class;
                config.nombre = "marcaciones";
                config.funcionSAP = Constantes.FUNCION_MARCACIONES;
                config.regDao = marcDao;
                config.parameterTable = "ZTHRPT_TBMARCA";
                break;
            case Constantes.NOVEDADES:
                config.clase = Novedad.class;
                config.nombre = "novedades";
                config.funcionSAP = Constantes.FUNCION_NOVEDADES;
                config.regDao = novedadDao;
                config.parameterTable = "ZTHRPT_TBNOVEDAD";
                break;
            case Constantes.NOVEDADES_PAQUETE:
                config.clase = NovedadPaquete.class;
                config.nombre = "novedades paquete";
                config.funcionSAP = Constantes.FUNCION_NOVEDADES_PAQUETE;
                config.regDao = novedadPaqueteDao;
                config.parameterTable = "ZTHRPT_TBBOLCAS";
                break;
        }
        return config;
    }
    
    public List<Empleado> findEmpleados(int maxResults) {
        return empleadoDao.findEmployees(maxResults);
    }
    
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    
    public void detener() {
        showMessage("Deteniendo...");
        running = false;
    }
    
    public boolean isDone() {
        return detenido;
    }
    
    
    public int count(Criterios criterios, int tipo) {
        ConfiguracionReg config = this.getConfiguracion(tipo);
        RegistroDAO regDao = config.regDao;
        return regDao.count(criterios);        
    }
    
    public void agregarPlanta(String usuario, String planta) {
        UsuarioPlanta up = new UsuarioPlanta(usuario,planta);
        if ( !tienePlantaAsignada(up) )
        {
            usuarioPlantaDao.create(up);
        }
    }
    
    public boolean tienePlantaAsignada(UsuarioPlanta up) {
        return usuarioPlantaDao.exists(up);   
    }
    
    public void desasignarPlanta(UsuarioPlanta up) {
        if ( tienePlantaAsignada(up)) {
            usuarioPlantaDao.delete(up);
        }
    }

    public List<String> listPernrs() {
        return maestrosDao.listPernrs();
    }
    
    
    public void descargarMaestro(Maestro m) {
        if ( maestrosDao.get(m.getPernr()) == null )
           maestrosDao.create(m);
    }
    
    public boolean existeMaestro(String pernr) {
        return maestrosDao.get(pernr) != null;
    }
    
    public List<StringItem> getDivisiones() {
        return divisionDao.getDivisiones();
    }
    
    public List<StringItem> getSubdivisiones(StringItem division) {
        return divisionDao.getSubdivisiones(division);
    }
    
    public StringItem getItem(String value) {
        return divisionDao.getItem(value);
    }

    public Empleado getEmpleadoByPernr(String pernr) {
        try 
        {
            CriteriosEmpleado criteria = new CriteriosEmpleado();
            Integer pernrInt = Integer.parseInt(pernr);
            criteria.setPernr(String.valueOf(pernrInt));
            List<Empleado> results = empleadoDao.find(criteria);
            if ( results.isEmpty() )
            {
                return null;
            }
            else
                return results.get(0);
        }
        catch ( Exception ex )
        {
            return null;
        }
    }
    
    public static String getNombres(Registro r) {
        SqlClauseHelper sq = new SqlClauseHelper();
        if ( r != null )
        {
            if ( r.getNombres() != null )
                sq.append(" ",r.getNombres());
            if ( r.getPrimerApellido() != null )
                sq.append(" ",r.getPrimerApellido());
            if ( r.getSegundoApellido() != null )
                sq.append(" ",r.getSegundoApellido());
        }
        return sq.toString();
    }
    
    public void setConeccionSAPOK(boolean valor) {
        this.coneccionSAPOK = valor;
    }
    
    public boolean getConeccionSAPOK() {
        return coneccionSAPOK;
    }
    
    
/*    
    public static List<Maestro> generarMaestros(int num) {
      List<Maestro> lista = new ArrayList<Maestro>();
      for (int i = 0; i < num; i++) {
          Maestro mo = new Maestro();
          //mo.se
          mo.setPernr(String.valueOf(i));
          mo.setNachn("Nachn");
          mo.setName2("name2");
          mo.setKostl("PRIO");
          lista.add(mo);
          
      }
      return lista;
    }
  */  
}
