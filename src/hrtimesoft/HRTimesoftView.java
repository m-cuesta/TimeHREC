/*
 * HRTimesoftView.java
 */

package hrtimesoft;

import com.inga.exception.BDException;
import com.inga.exception.NoConnectionException;
import com.inga.security.User;
import com.inga.utils.DateRange;
import com.inga.utils.Fecha;
import com.inga.utils.StringItem;
import cryptowerk.Cryptowerk;
import java.awt.CardLayout;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import javax.swing.text.BadLocationException;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import timesoft.Constantes;
import timesoft.Main;
import timesoft.control.TimesoftManager;
import timesoft.dao.impl.FamiliaUtilsDAO;
import timesoft.exceptions.NoTienePlantasAsignadas;
import timesoft.model.Criterios;
import timesoft.model.CriteriosIntento;
import timesoft.model.IdRange;
import timesoft.model.Intento;
import timesoft.model.Maestro;
import timesoft.model.Novedad;
import timesoft.model.NovedadPaquete;
import timesoft.model.Registro;
import timesoft.model.Reporte;
import timesoft.model.SubdivisionCorreo;
import timesoft.model.Usuario;
import timesoft.model.UsuarioPlanta;
import utilerias.timecore.ComparadorMaestro;

/**
 * The application's main frame.
 */
public class HRTimesoftView extends FrameView implements WindowListener {
    
  private String bdErrorMessage = "";
  private String mensajeErrorSAP = "";
  private boolean conexionSAP = false;
  private boolean bdOK = false;
  private Properties props;    

    private static Logger log = Logger.getLogger(HRTimesoftView.class);

    public static final String PROPS_FILE = "timesoft.properties" ;
    
    private Usuario usr;
    private static String versionInfo = "<html>Versión " + HRTimesoftApp.VERSION + "&nbsp;&nbsp;&nbsp;</html>";

    private TimesoftManager tm;
    private ColumnPropertiesTableModel model;
    private MailPropertiesModel mailModel;
    DefaultComboBoxModel subdivisiones;
    DefaultComboBoxModel orderItems;
    DefaultComboBoxModel estados;
    DefaultComboBoxModel descripcionesModel;
    DefaultComboBoxModel divisionesModel;

    DefaultListModel usrsListModel;
    
    Properties mailProps;
    
    Task tareaProcesarRegistros;
    
    private static final int PAGE_SIZE = 500;
    private int paginaMarcaciones = 0;
    private int paginaNovedades = 0;
    private int paginaPaquete = 0;
    private int paginaHistorial = 0;
    // Determina para qué listado se ejecutará la acción de ir a la página:
    // Marcacione, Novedades, etc.
    private int irapagMode = 0;
    private static final int HISTORIAL_MODE = 2;
    
    List<Maestro> maestrosEnSap;

    public HRTimesoftView(SingleFrameApplication app) {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

        ///////////////////

        try
        {
            myInit();
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
            log.error( ex.getMessage() );
        }

    }
    

    @Action
    public void showLoginPanel() {
        showPanel( loginPanel );
        this.showFooterPanel(jPanel13);
    }

    public final void myInit() throws NoConnectionException, BDException {
        
        //this.getFrame().setResizable(false);
        getFrame().addWindowListener( this );
        com.inga.utils.Registro.setLevel( com.inga.utils.Registro.DEBUG );
        
        JPanel p = contenedor;
        this.getComponent().setVisible(false);
        p.setVisible(true);
        setComponent( p );

        showPanel( loginPanel );


    this.tm = HRTimesoftApp.tm;
    if (this.tm != null)
    {
      this.tm.setStatus(this.statusMessageLabel);
      this.tm.showMessage("Archivo de configuración cargado");
    }
        
    Main.setMailConfiguration();

    bdOK = false;
    if (this.tm != null)
    {
      this.tm.setStatus(this.statusMessageLabel);
      try {
          this.tm.listUsuarios();
          bdOK = true;
      }
      catch (Exception bdEx) {
          log.fatal("Error de conexión con la base de datos", bdEx);
          
          ByteArrayOutputStream bout = new ByteArrayOutputStream();
          PrintWriter writer = new PrintWriter(bout);
          bdEx.printStackTrace(writer);
          writer.close();
          
          bdErrorMessage = new String(bout.toByteArray());
          
          bdOK = false;
      }
    }
        
        model = new ColumnPropertiesTableModel();

        ColumnPropertiesCellRenderer renderer = new ColumnPropertiesCellRenderer();
        
        try
        {
            mailModel = new MailPropertiesModel( Main.getMailProps() );
            jTable2.setModel( mailModel );
            jTable2.getColumnModel().getColumn(0).setCellRenderer( renderer );
            jTable2.getColumnModel().getColumn(1).setCellRenderer( renderer );
        }
        catch ( Exception ex ) 
        {
            System.out.println("ERROR: No se pudo cargar el archivo de configuración de correo.");
        }

        jTable1.setModel( model );
        jTable1.getColumnModel().getColumn(0).setCellRenderer( renderer  );
        jTable1.getColumnModel().getColumn(1).setCellRenderer( renderer  );


        jLabel4.setText("");
        versionLabel.setText( versionInfo );


        subdivisiones = new DefaultComboBoxModel();
        subdivisiones.addElement( new StringItem("TODOS", "GENERAL"));
        List<String> allSubs = tm.listarTodasSubdivisiones();
        for ( String sub : allSubs )
            subdivisiones.addElement( new StringItem(sub,sub));

        orderItems = new DefaultComboBoxModel();
        orderItems.addElement(new StringItem("ID", "id"));
        orderItems.addElement(new StringItem("Núm. Pers.", "pernr"));
        orderItems.addElement(new StringItem("Captura en SIBO", "ldate"));
        orderItems.addElement(new StringItem("Subida a SAP","cntrl"));

        estados = new DefaultComboBoxModel();
        estados.addElement( new StringItem("Error", Constantes.ERROR));
        estados.addElement( new StringItem("Cualquiera",null));
        estados.addElement( new StringItem("OK", Constantes.EXITO));
        estados.addElement( new StringItem("Nueva", timesoft.Constantes.NUEVA));

        jComboBox1.setModel( orderItems );
        jComboBox1.setSelectedIndex( 2 );

        jComboBox2.setModel(estados);
        jComboBox2.setSelectedIndex(3);

        comboEstadoNovedades.setModel( estados );
        comboEstadoNovedades.setSelectedIndex(3);

        comboEstadoPaquete.setModel( estados );
        comboEstadoPaquete.setSelectedIndex(3);

        DefaultComboBoxModel novedadesOrder = new DefaultComboBoxModel();
        novedadesOrder.addElement(new StringItem("ID","id"));
        novedadesOrder.addElement(new StringItem("Núm. Pers.", "pernr"));
        novedadesOrder.addElement( new StringItem("Captura en SIBO", "begda"));
        novedadesOrder.addElement(new StringItem("Subida a SAP", "cntrl"));

        DefaultComboBoxModel novedadesPaqueteOrder = new DefaultComboBoxModel();
        novedadesPaqueteOrder.addElement( new StringItem("ID","id"));
        novedadesPaqueteOrder.addElement( new StringItem("Núm. Per.","pernr"));
        novedadesPaqueteOrder.addElement( new StringItem("Captura en SIBO", "ldate"));
        novedadesPaqueteOrder.addElement( new StringItem("Subida a SAP", "cntrl"));

        jComboBox3.setModel( novedadesOrder );
        jComboBox3.setSelectedIndex( 2 );

        jComboBox5.setModel( novedadesPaqueteOrder );
        jComboBox5.setSelectedIndex( 2 );

        subdivisionesCombo.setModel( subdivisiones );
        subdivisionesCombo.setSelectedIndex(0);

        jRadioButton3.setSelected(true);
        jRadioButton4.setSelected(false);

        CorreosTableModel correosTableModel = new CorreosTableModel();
        correosTable.setModel( correosTableModel );

        DefaultComboBoxModel tipoComboModel = new DefaultComboBoxModel();
        tipoComboModel.addElement(  new StringItem( "Cualquiera", null) );
        tipoComboModel.addElement( new StringItem( "Casino",Constantes.CASINO));
        tipoComboModel.addElement( new StringItem( "Bolsa de papel", Constantes.BOLSA_DE_PAPEL));
        jComboBox7.setModel( tipoComboModel );
        
        DefaultComboBoxModel comboTareaModel = new DefaultComboBoxModel();
        comboTareaModel.addElement( new StringItem("Cualquiera",null));
        comboTareaModel.addElement( new StringItem("MAESTROS", "1"));
        comboTareaModel.addElement( new StringItem("C.COSTO", "2"));
        comboTareaModel.addElement( new StringItem("MARCACIONES","3"));
        comboTareaModel.addElement( new StringItem("NOVEDADES", "4"));
        comboTareaModel.addElement( new StringItem("PAQUETE", "5"));
        comboTareaModel.addElement( new StringItem("CORREO", "6"));
        this.comboTarea.setModel( comboTareaModel );
        
        DefaultComboBoxModel estadoIntentoModel = new DefaultComboBoxModel();
        estadoIntentoModel.addElement( new StringItem("Cualquiera", null));
        estadoIntentoModel.addElement( new StringItem("OK", Constantes.EXITO));
        estadoIntentoModel.addElement( new StringItem("ERROR", Constantes.ERROR));
        estadoIntentoModel.addElement( new StringItem("INCOMPLETO","I"));
        this.comboEstadoIntento.setModel( estadoIntentoModel );
        
        DefaultComboBoxModel usuarioModel = new DefaultComboBoxModel();
        usuarioModel.addElement( new StringItem("Todos", null));
        List<Usuario> usrs = tm.listUsuarios();
        Collections.sort(usrs);
        for ( Usuario user : usrs )
            usuarioModel.addElement( new StringItem( user.getLogin(), user.getLogin()));
        this.comboUsuario.setModel( usuarioModel );
        
        DefaultComboBoxModel orderIntentoModel = new DefaultComboBoxModel();
        orderIntentoModel.addElement( new StringItem("Fecha Inicio", "creado"));
        orderIntentoModel.addElement( new StringItem("Fecha Fin", "modificado"));
        orderIntentoModel.addElement( new StringItem("Usuario", "firma"));
        orderIntentoModel.addElement( new StringItem("Estado","estado"));
        orderIntentoModel.addElement( new StringItem("Tarea", "tipoActividad"));
        this.comboOrdenIntento.setModel ( orderIntentoModel );
        
        comboEstadoNovedades.setSelectedIndex(0);
        this.jComboBox2.setSelectedIndex(0);
        comboEstadoPaquete.setSelectedIndex(0);
        
        Fecha today = new Fecha( new Date() );
        SimpleDateFormat sdf = new SimpleDateFormat( timesoft.Constantes.STANDARD_DATE );
        String todayStr = sdf.format( today );
        this.desdeSiboMarcs.setText( todayStr );
        this.desdeSiboNovedades.setText( todayStr );
        this.desdeSiboPaquete.setText( todayStr );
        
        refreshUsuarios();
        
        descripcionesModel = new DefaultComboBoxModel();
        descripcionesModel.addElement(new StringItem("Cualquiera", null));
        descripcionesModel.addElement(new StringItem("Registro ya existe", timesoft.Constantes.REGISTRO_YA_EXISTE));
        descripcionesModel.addElement(new StringItem("Empleado no se encuentra Activo", timesoft.Constantes.NO_SE_ENCUENTRA_ACTIVO));
        descripcionesModel.addElement(new StringItem("Fecha retroactiva mas antigua",timesoft.Constantes.FECHA_RETROACTIVA_MAS_ANTIGUA));
        descripcionesModel.addElement(new StringItem("Empleado no tiene registro en infotipo",timesoft.Constantes.NO_TIENE_REGISTRO_INFOTIPO));
        descripcionesModel.addElement(new StringItem("No se creó registro",timesoft.Constantes.NO_SE_CREO_REGISTRO));
        descripcionesModel.addElement(new StringItem("Usuario estrá tratando la persona",timesoft.Constantes.USUARIO_ESTA_TRATANDO_LA_PERSONA));
        descripcionesModel.addElement(new StringItem("Área de nómina bloqueada",timesoft.Constantes.AREA_BLOQUEADA_PARA_ACTUALIZACION));
        descripcionesModel.addElement(new StringItem("Faltan campos obligatorios",timesoft.Constantes.CAMPOS_FALTANTES));
        descripcionesModel.addElement(new StringItem("No sea creado registro de gestión",timesoft.Constantes.NO_SE_HA_CREADO_REGISTRO_DE_GESTION));
        descripcionesModel.addElement(new StringItem("Otros",timesoft.Constantes.OTROS_MENSAJES));
        
        descripcionesMarcacion.setModel(descripcionesModel);
        descripcionesMarcacion.setSelectedIndex(0);
        descripcionesNovedad.setModel(descripcionesModel);
        descripcionesNovedad.setSelectedIndex(0);
        descripcionesPaquete.setModel(descripcionesModel);
        descripcionesPaquete.setSelectedIndex(0);
        
        divisionesModel = new DefaultComboBoxModel();
        
        System.out.println("Inicio OK");

    }
     
    public void refreshUsuarios() {
        List<Usuario> usrs = tm.listUsuarios();
        
        usrsListModel = new DefaultListModel();
        for ( Usuario usuario : usrs )
            usrsListModel.addElement(usuario);
        
        this.listUsuarios.setModel( usrsListModel );
        listUsuarios.setSelectionMode( ListSelectionModel.SINGLE_SELECTION);
    }


    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = HRTimesoftApp.getApplication().getMainFrame();
            aboutBox = new HRTimesoftAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        HRTimesoftApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        contenedor = new javax.swing.JPanel();
        centerPanel = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jTextField13 = new javax.swing.JTextField();
        jTextField14 = new javax.swing.JTextField();
        jButton29 = new javax.swing.JButton();
        jCheckBox7 = new javax.swing.JCheckBox();
        jCheckBox8 = new javax.swing.JCheckBox();
        jLabel16 = new javax.swing.JLabel();
        jButton30 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jButton26 = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        jButton27 = new javax.swing.JButton();
        jLabel23 = new javax.swing.JLabel();
        jTextField21 = new javax.swing.JTextField();
        jCheckBox5 = new javax.swing.JCheckBox();
        jLabel13 = new javax.swing.JLabel();
        jTextField11 = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        jTextField22 = new javax.swing.JTextField();
        jCheckBox6 = new javax.swing.JCheckBox();
        jTextField12 = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jTextField23 = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        jTextField24 = new javax.swing.JTextField();
        jCheckBox11 = new javax.swing.JCheckBox();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        correoPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton18 = new javax.swing.JButton();
        jTextField8 = new javax.swing.JTextField();
        jButton19 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        correosTable = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        eliminarCorreo = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jButton14 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jButton17 = new javax.swing.JButton();
        jCheckBox3 = new javax.swing.JCheckBox();
        jLabel19 = new javax.swing.JLabel();
        jTextField17 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jTextField18 = new javax.swing.JTextField();
        jCheckBox4 = new javax.swing.JCheckBox();
        jTextField7 = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jTextField19 = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jTextField20 = new javax.swing.JTextField();
        jCheckBox10 = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton7 = new javax.swing.JButton();
        jTextField3 = new javax.swing.JTextField();
        jButton9 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jButton11 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jButton16 = new javax.swing.JButton();
        jTextField9 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jTextField10 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        mainMenu = new javax.swing.JPanel();
        buttonSincronizarMaestros = new javax.swing.JButton();
        buttonGenerarArchivo = new javax.swing.JButton();
        buttonSincronizarCentrosCosto = new javax.swing.JButton();
        buttonCorreo = new javax.swing.JButton();
        jButton25 = new javax.swing.JButton();
        buttonCambiarPassword = new javax.swing.JButton();
        buttonHistorial = new javax.swing.JButton();
        buttonAdminUsuarios = new javax.swing.JButton();
        buttonEditarPerfil = new javax.swing.JButton();
        jPanel15 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        panelLogMarcaciones = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tablaMarcaciones = new javax.swing.JTable();
        logMarcsForm = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        desdeSiboMarcs = new javax.swing.JTextField();
        jTextField16 = new javax.swing.JTextField();
        jButton13 = new javax.swing.JButton();
        jTextField25 = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        jTextField26 = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jTextField27 = new javax.swing.JTextField();
        jTextField28 = new javax.swing.JTextField();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jLabel33 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        jLabel74 = new javax.swing.JLabel();
        jLabel75 = new javax.swing.JLabel();
        descripcionesMarcacion = new javax.swing.JComboBox();
        jLabel85 = new javax.swing.JLabel();
        jLabel87 = new javax.swing.JLabel();
        botonExportarExcel = new javax.swing.JButton();
        jLabel91 = new javax.swing.JLabel();
        comboDivsMarcacion = new javax.swing.JComboBox();
        comboSubsMarcacion = new javax.swing.JComboBox();
        marcacionesMenu = new javax.swing.JPanel();
        jButton36 = new javax.swing.JButton();
        jButton37 = new javax.swing.JButton();
        panelLogNovedades = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tablaNovedades = new javax.swing.JTable();
        logNovedadesForm = new javax.swing.JPanel();
        jLabel37 = new javax.swing.JLabel();
        desdeSiboNovedades = new javax.swing.JTextField();
        hastaSiboNovedades = new javax.swing.JTextField();
        jButton24 = new javax.swing.JButton();
        jTextField31 = new javax.swing.JTextField();
        jLabel38 = new javax.swing.JLabel();
        jTextField32 = new javax.swing.JTextField();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        desdeSapNovedades = new javax.swing.JTextField();
        hastaSapNovedades = new javax.swing.JTextField();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox();
        jRadioButton5 = new javax.swing.JRadioButton();
        jRadioButton6 = new javax.swing.JRadioButton();
        jLabel44 = new javax.swing.JLabel();
        comboEstadoNovedades = new javax.swing.JComboBox();
        jLabel72 = new javax.swing.JLabel();
        jLabel73 = new javax.swing.JLabel();
        jLabel84 = new javax.swing.JLabel();
        descripcionesNovedad = new javax.swing.JComboBox();
        jButton15 = new javax.swing.JButton();
        jLabel88 = new javax.swing.JLabel();
        jLabel92 = new javax.swing.JLabel();
        comboDivsNovedad = new javax.swing.JComboBox();
        comboSubsNovedad = new javax.swing.JComboBox();
        novedadesPaqueteForm = new javax.swing.JPanel();
        jLabel45 = new javax.swing.JLabel();
        desdeSiboPaquete = new javax.swing.JTextField();
        hastaSiboPaquete = new javax.swing.JTextField();
        jButton41 = new javax.swing.JButton();
        jTextField37 = new javax.swing.JTextField();
        jLabel46 = new javax.swing.JLabel();
        jTextField38 = new javax.swing.JTextField();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        desdeSapPaquete = new javax.swing.JTextField();
        hastaSapPaquete = new javax.swing.JTextField();
        jLabel50 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        jComboBox5 = new javax.swing.JComboBox();
        jRadioButton7 = new javax.swing.JRadioButton();
        jRadioButton8 = new javax.swing.JRadioButton();
        jLabel52 = new javax.swing.JLabel();
        comboEstadoPaquete = new javax.swing.JComboBox();
        jComboBox7 = new javax.swing.JComboBox();
        jLabel53 = new javax.swing.JLabel();
        jLabel70 = new javax.swing.JLabel();
        jLabel71 = new javax.swing.JLabel();
        jLabel86 = new javax.swing.JLabel();
        descripcionesPaquete = new javax.swing.JComboBox();
        jButton21 = new javax.swing.JButton();
        jLabel89 = new javax.swing.JLabel();
        jLabel93 = new javax.swing.JLabel();
        comboDivsPaquete = new javax.swing.JComboBox();
        comboSubsPaquete = new javax.swing.JComboBox();
        panelLogNovedadesPaquete = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        tablaNovedadesPaquete = new javax.swing.JTable();
        panelHistorial = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        tablaHistorial = new javax.swing.JTable();
        historialForm = new javax.swing.JPanel();
        jLabel54 = new javax.swing.JLabel();
        jTextField41 = new javax.swing.JTextField();
        jTextField42 = new javax.swing.JTextField();
        jButton46 = new javax.swing.JButton();
        jLabel55 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        jTextField45 = new javax.swing.JTextField();
        jTextField46 = new javax.swing.JTextField();
        jLabel59 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        comboOrdenIntento = new javax.swing.JComboBox();
        jRadioButton9 = new javax.swing.JRadioButton();
        jRadioButton10 = new javax.swing.JRadioButton();
        jLabel61 = new javax.swing.JLabel();
        comboEstadoIntento = new javax.swing.JComboBox();
        comboUsuario = new javax.swing.JComboBox();
        comboTarea = new javax.swing.JComboBox();
        jLabel62 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        loginPanel = new javax.swing.JPanel();
        jLabel64 = new javax.swing.JLabel();
        jLabel65 = new javax.swing.JLabel();
        jButton12 = new javax.swing.JButton();
        usuarioField = new javax.swing.JTextField();
        jButton47 = new javax.swing.JButton();
        passwordField = new javax.swing.JPasswordField();
        cambiarPasswordPanel = new javax.swing.JPanel();
        jLabel66 = new javax.swing.JLabel();
        jLabel67 = new javax.swing.JLabel();
        jButton49 = new javax.swing.JButton();
        jButton50 = new javax.swing.JButton();
        nuevaContrasena1 = new javax.swing.JPasswordField();
        nuevaContrasena2 = new javax.swing.JPasswordField();
        jLabel68 = new javax.swing.JLabel();
        contrasenaErrorMessage = new javax.swing.JLabel();
        viejaContrasena = new javax.swing.JPasswordField();
        adminUsuariosPanel = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        listUsuarios = new javax.swing.JList();
        buttonNuevoUsuario = new javax.swing.JButton();
        buttonEliminarUsuario = new javax.swing.JButton();
        buttonEditarUsuario = new javax.swing.JButton();
        nuevoUsuarioPanel = new javax.swing.JPanel();
        nuevoUsuarioLogin = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jLabel69 = new javax.swing.JLabel();
        nuevoErrorMessage = new javax.swing.JLabel();
        radioAdmin = new javax.swing.JRadioButton();
        radioUser = new javax.swing.JRadioButton();
        jButton4 = new javax.swing.JButton();
        contrasenaErrorMessage2 = new javax.swing.JLabel();
        editarPerfilPanel = new javax.swing.JPanel();
        jLabel76 = new javax.swing.JLabel();
        jButton51 = new javax.swing.JButton();
        jButton52 = new javax.swing.JButton();
        editarPerfilErrorMessage = new javax.swing.JLabel();
        nombreUsuario = new javax.swing.JTextField();
        editarUsuarioPanel = new javax.swing.JPanel();
        listasPlantasScrollPanel = new javax.swing.JScrollPane();
        listPlantasUsuario = new javax.swing.JList();
        botonAgregarPlanta = new javax.swing.JButton();
        botonEliminarPlanta = new javax.swing.JButton();
        jLabel78 = new javax.swing.JLabel();
        labelNombreUsuarioEditar = new javax.swing.JLabel();
        labelRolUsuarioEditar = new javax.swing.JLabel();
        jLabel81 = new javax.swing.JLabel();
        jLabel82 = new javax.swing.JLabel();
        labelPlantasTodas = new javax.swing.JLabel();
        regresarEditarUsuario = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        asignarContrasena = new javax.swing.JButton();
        listaMaestrosPanel = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        tablaMaestros = new javax.swing.JTable();
        headerPanel = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        welcomeLabel = new javax.swing.JLabel();
        versionLabel = new javax.swing.JLabel();
        footerPanel = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jButton23 = new javax.swing.JButton();
        footerMarcaciones = new javax.swing.JPanel();
        botonSubirSeleccionados = new javax.swing.JButton();
        botonRegresarFormulario = new javax.swing.JButton();
        botonActualizar = new javax.swing.JButton();
        botonDetener = new javax.swing.JButton();
        botonSubirTodos = new javax.swing.JButton();
        avPagMarcaciones = new javax.swing.JButton();
        rePagMarcaciones = new javax.swing.JButton();
        irapagMarcaciones = new javax.swing.JButton();
        footerNovedades = new javax.swing.JPanel();
        botonActualizarNovedades = new javax.swing.JButton();
        botonDetenerNovedades = new javax.swing.JButton();
        botonSubirNovedadesSeleccionadas = new javax.swing.JButton();
        botonSubirTodasNovedades = new javax.swing.JButton();
        botonRegresarNovedades = new javax.swing.JButton();
        rePagNovedades = new javax.swing.JButton();
        avPagNovedades = new javax.swing.JButton();
        irapagNovedades = new javax.swing.JButton();
        footerNovedadesPaquete = new javax.swing.JPanel();
        botonActualizarPaquete = new javax.swing.JButton();
        botonDetenerPaquete = new javax.swing.JButton();
        botonSubirPaqueteSeleccionados = new javax.swing.JButton();
        botonSubirTodasPaquete = new javax.swing.JButton();
        botonRegresarPaquete = new javax.swing.JButton();
        rePagPaquete = new javax.swing.JButton();
        avPagPaquete = new javax.swing.JButton();
        irapagPaquete = new javax.swing.JButton();
        footerHistorial = new javax.swing.JPanel();
        regresarHistorial = new javax.swing.JButton();
        rePagHistorial = new javax.swing.JButton();
        avPagHistorial = new javax.swing.JButton();
        irapagHistorial = new javax.swing.JButton();
        footerMaestros = new javax.swing.JPanel();
        regresarMaestros = new javax.swing.JButton();
        descargarMaestro = new javax.swing.JButton();
        footerVerConexiones = new javax.swing.JPanel();
        jButton28 = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        jDialog1 = new javax.swing.JDialog();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jFileChooser1 = new javax.swing.JFileChooser();
        jButton22 = new javax.swing.JButton();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jOptionPane1 = new javax.swing.JOptionPane();
        nuevoCorreoDialog = new javax.swing.JDialog();
        jLabel35 = new javax.swing.JLabel();
        subdivisionesCombo = new javax.swing.JComboBox();
        jLabel36 = new javax.swing.JLabel();
        nuevoCorreoText = new javax.swing.JTextField();
        jButton8 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        eliminarUsuarioPane = new javax.swing.JOptionPane();
        jDialog2 = new javax.swing.JDialog();
        jOptionPane2 = new javax.swing.JOptionPane();
        bgTipoUsuario = new javax.swing.ButtonGroup();
        jButton40 = new javax.swing.JButton();
        irapag = new javax.swing.JDialog();
        botonIrapagOK = new javax.swing.JButton();
        irapagTextField = new javax.swing.JTextField();
        jLabel77 = new javax.swing.JLabel();
        botonIrapagCancelar = new javax.swing.JButton();
        asignarPlanta = new javax.swing.JDialog();
        comboPlantasAsignar = new javax.swing.JComboBox();
        jLabel80 = new javax.swing.JLabel();
        botonAsignarPlantaOk = new javax.swing.JButton();
        botonCerrarAsignarPlanta = new javax.swing.JButton();
        jLabel79 = new javax.swing.JLabel();
        errorDetallado = new javax.swing.JDialog();
        scrollPane1 = new java.awt.ScrollPane();
        jScrollPane11 = new javax.swing.JScrollPane();
        areaErrorDetallado = new javax.swing.JTextArea();
        jLabel83 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        nuevaContrasena = new javax.swing.JDialog();
        jLabel90 = new javax.swing.JLabel();
        nuevaContrasenaText = new javax.swing.JTextField();
        okNuevaContrasena = new javax.swing.JButton();
        cancelarNuevaContrasena = new javax.swing.JButton();
        jFileChooser2 = new javax.swing.JFileChooser();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(hrtimesoft.HRTimesoftApp.class).getContext().getResourceMap(HRTimesoftView.class);
        mainPanel.setBackground(resourceMap.getColor("mainPanel.background")); // NOI18N
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mainPanelMouseClicked(evt);
            }
        });
        mainPanel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
                mainPanelAncestorRemoved(evt);
            }
        });
        mainPanel.setLayout(new javax.swing.BoxLayout(mainPanel, javax.swing.BoxLayout.LINE_AXIS));

        contenedor.setName("contenedor"); // NOI18N
        contenedor.setLayout(new java.awt.BorderLayout());

        centerPanel.setBackground(resourceMap.getColor("centerPanel.background")); // NOI18N
        centerPanel.setName("centerPanel"); // NOI18N
        centerPanel.setPreferredSize(new java.awt.Dimension(500, 20));
        centerPanel.setLayout(new java.awt.CardLayout());

        jPanel8.setName("jPanel8"); // NOI18N
        jPanel8.setPreferredSize(new java.awt.Dimension(579, 371));

        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        jTextField13.setName("jTextField13"); // NOI18N

        jTextField14.setName("jTextField14"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(hrtimesoft.HRTimesoftApp.class).getContext().getActionMap(HRTimesoftView.class, this);
        jButton29.setAction(actionMap.get("okBolsaDePapel")); // NOI18N
        jButton29.setName("jButton29"); // NOI18N

        jCheckBox7.setAction(actionMap.get("jCheckBox7Change")); // NOI18N
        jCheckBox7.setName("jCheckBox7"); // NOI18N

        jCheckBox8.setText(resourceMap.getString("jCheckBox8.text")); // NOI18N
        jCheckBox8.setName("jCheckBox8"); // NOI18N

        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        jButton30.setAction(actionMap.get("cancelarNovedades")); // NOI18N
        jButton30.setName("jButton30"); // NOI18N

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jButton29)
                                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jCheckBox8)
                                        .addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton30)))
                        .addGap(22, 22, 22)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField14, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBox7))))
                .addGap(477, 477, 477))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jCheckBox7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField14, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 244, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton29)
                    .addComponent(jButton30))
                .addGap(83, 83, 83))
        );

        centerPanel.add(jPanel8, "jPanel8");

        jPanel7.setName("jPanel7"); // NOI18N
        jPanel7.setPreferredSize(new java.awt.Dimension(579, 371));

        jButton26.setAction(actionMap.get("okCassino")); // NOI18N
        jButton26.setName("jButton26"); // NOI18N

        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        jButton27.setAction(actionMap.get("cancelarNovedades")); // NOI18N
        jButton27.setName("jButton27"); // NOI18N

        jLabel23.setText(resourceMap.getString("jLabel23.text")); // NOI18N
        jLabel23.setName("jLabel23"); // NOI18N

        jTextField21.setName("jTextField21"); // NOI18N

        jCheckBox5.setAction(actionMap.get("okReprocesoPaquete")); // NOI18N
        jCheckBox5.setName("jCheckBox5"); // NOI18N

        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        jTextField11.setName("jTextField11"); // NOI18N

        jLabel24.setText(resourceMap.getString("jLabel24.text")); // NOI18N
        jLabel24.setName("jLabel24"); // NOI18N

        jTextField22.setName("jTextField22"); // NOI18N

        jCheckBox6.setAction(actionMap.get("okHastaPaquete")); // NOI18N
        jCheckBox6.setName("jCheckBox6"); // NOI18N

        jTextField12.setName("jTextField12"); // NOI18N

        jLabel25.setText(resourceMap.getString("jLabel25.text")); // NOI18N
        jLabel25.setName("jLabel25"); // NOI18N

        jTextField23.setName("jTextField23"); // NOI18N

        jLabel26.setText(resourceMap.getString("jLabel26.text")); // NOI18N
        jLabel26.setName("jLabel26"); // NOI18N

        jTextField24.setName("jTextField24"); // NOI18N

        jCheckBox11.setAction(actionMap.get("okRangoPaquete")); // NOI18N
        jCheckBox11.setName("jCheckBox11"); // NOI18N

        jRadioButton1.setAction(actionMap.get("selCasino")); // NOI18N
        jRadioButton1.setName("jRadioButton1"); // NOI18N

        jRadioButton2.setAction(actionMap.get("selBolsaPapel")); // NOI18N
        jRadioButton2.setName("jRadioButton2"); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(71, 71, 71)
                        .addComponent(jButton26)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton27))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel23)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField21, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jCheckBox5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 808, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel7Layout.createSequentialGroup()
                                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(jPanel7Layout.createSequentialGroup()
                                                .addComponent(jLabel24)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 579, Short.MAX_VALUE)
                                                .addComponent(jTextField22, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGap(81, 81, 81)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jCheckBox6)
                                    .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(17, 17, 17))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel7Layout.createSequentialGroup()
                                        .addComponent(jLabel25)
                                        .addGap(43, 43, 43)
                                        .addComponent(jTextField23, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextField24, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jCheckBox11))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 639, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jRadioButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 700, Short.MAX_VALUE)
                                .addComponent(jRadioButton2)
                                .addGap(54, 54, 54))
                            .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(93, 93, 93)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jCheckBox6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(jTextField23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel26)
                    .addComponent(jTextField24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel23)
                    .addComponent(jTextField21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 114, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton26)
                    .addComponent(jButton27))
                .addGap(83, 83, 83))
        );

        centerPanel.add(jPanel7, "jPanel7");

        correoPanel.setBackground(resourceMap.getColor("correoPanel.background")); // NOI18N
        correoPanel.setName("correoPanel"); // NOI18N
        correoPanel.setPreferredSize(new java.awt.Dimension(579, 371));
        correoPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable2.setName("jTable2"); // NOI18N
        jScrollPane2.setViewportView(jTable2);

        correoPanel.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 460, 85));

        jButton18.setAction(actionMap.get("setMailFilename")); // NOI18N
        jButton18.setName("jButton18"); // NOI18N
        correoPanel.add(jButton18, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 140, -1, -1));

        jTextField8.setName("jTextField8"); // NOI18N
        correoPanel.add(jTextField8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 140, 376, -1));

        jButton19.setAction(actionMap.get("okGenerarMail")); // NOI18N
        jButton19.setName("jButton19"); // NOI18N
        correoPanel.add(jButton19, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 30, -1, -1));

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N
        correoPanel.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, -1, -1));

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N
        correoPanel.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, 452, -1));

        jScrollPane5.setName("jScrollPane5"); // NOI18N

        correosTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        correosTable.setName("correosTable"); // NOI18N
        correosTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane5.setViewportView(correosTable);

        correoPanel.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 200, 460, 100));

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        correoPanel.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, 190, -1));

        eliminarCorreo.setAction(actionMap.get("okEliminarCorreo")); // NOI18N
        eliminarCorreo.setText(resourceMap.getString("eliminarCorreo.text")); // NOI18N
        eliminarCorreo.setName("eliminarCorreo"); // NOI18N
        correoPanel.add(eliminarCorreo, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 200, 100, 20));

        jButton20.setAction(actionMap.get("okNuevoCorreo")); // NOI18N
        jButton20.setText(resourceMap.getString("jButton20.text")); // NOI18N
        jButton20.setName("jButton20"); // NOI18N
        correoPanel.add(jButton20, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 230, 100, 20));

        centerPanel.add(correoPanel, "correoPanel");

        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setPreferredSize(new java.awt.Dimension(579, 371));

        jButton14.setAction(actionMap.get("okNovedades")); // NOI18N
        jButton14.setName("jButton14"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jButton17.setAction(actionMap.get("cancelarNovedades")); // NOI18N
        jButton17.setName("jButton17"); // NOI18N

        jCheckBox3.setAction(actionMap.get("okReprocesoNovedades")); // NOI18N
        jCheckBox3.setName("jCheckBox3"); // NOI18N

        jLabel19.setText(resourceMap.getString("jLabel19.text")); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N

        jTextField17.setName("jTextField17"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jTextField6.setName("jTextField6"); // NOI18N

        jLabel20.setText(resourceMap.getString("jLabel20.text")); // NOI18N
        jLabel20.setName("jLabel20"); // NOI18N

        jTextField18.setName("jTextField18"); // NOI18N

        jCheckBox4.setAction(actionMap.get("hastaNovedadChange")); // NOI18N
        jCheckBox4.setName("jCheckBox4"); // NOI18N

        jTextField7.setName("jTextField7"); // NOI18N

        jLabel21.setText(resourceMap.getString("jLabel21.text")); // NOI18N
        jLabel21.setName("jLabel21"); // NOI18N

        jTextField19.setName("jTextField19"); // NOI18N

        jLabel22.setText(resourceMap.getString("jLabel22.text")); // NOI18N
        jLabel22.setName("jLabel22"); // NOI18N

        jTextField20.setName("jTextField20"); // NOI18N

        jCheckBox10.setAction(actionMap.get("okRangoNovedades")); // NOI18N
        jCheckBox10.setName("jCheckBox10"); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField17, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel5Layout.createSequentialGroup()
                                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel5Layout.createSequentialGroup()
                                            .addComponent(jCheckBox3)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 404, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel5Layout.createSequentialGroup()
                                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGroup(jPanel5Layout.createSequentialGroup()
                                                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(jPanel5Layout.createSequentialGroup()
                                                            .addComponent(jLabel20)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 175, Short.MAX_VALUE)
                                                            .addComponent(jTextField18, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                    .addGap(81, 81, 81)))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jCheckBox4)
                                                .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGap(17, 17, 17))
                                        .addGroup(jPanel5Layout.createSequentialGroup()
                                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(jPanel5Layout.createSequentialGroup()
                                                    .addComponent(jLabel21)
                                                    .addGap(43, 43, 43)
                                                    .addComponent(jTextField19, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(jTextField20, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addComponent(jCheckBox10))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 235, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGap(271, 271, 271))
                                .addGroup(jPanel5Layout.createSequentialGroup()
                                    .addGap(49, 49, 49)
                                    .addComponent(jButton14)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jButton17)))
                            .addGap(225, 225, 225)))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addGap(9, 9, 9)
                .addComponent(jCheckBox3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jCheckBox4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(jTextField19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22)
                    .addComponent(jTextField20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel19)
                    .addComponent(jTextField17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 148, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton14)
                    .addComponent(jButton17))
                .addGap(83, 83, 83))
        );

        centerPanel.add(jPanel5, "jPanel5");

        jPanel4.setBackground(resourceMap.getColor("jPanel4.background")); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setPreferredSize(new java.awt.Dimension(579, 371));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.setName("jTable1"); // NOI18N
        jScrollPane1.setViewportView(jTable1);

        jPanel4.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 463, 207));

        jButton7.setAction(actionMap.get("setFileName")); // NOI18N
        jButton7.setName("jButton7"); // NOI18N
        jPanel4.add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 30, -1, -1));

        jTextField3.setText(resourceMap.getString("jTextField3.text")); // NOI18N
        jTextField3.setName("jTextField3"); // NOI18N
        jPanel4.add(jTextField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 32, 380, -1));

        jButton9.setAction(actionMap.get("okGenerarArchivo")); // NOI18N
        jButton9.setName("jButton9"); // NOI18N
        jPanel4.add(jButton9, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 273, -1, -1));

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N
        jPanel4.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, -1, -1));

        centerPanel.add(jPanel4, "jPanel4");

        jPanel1.setBackground(resourceMap.getColor("jPanel1.background")); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(579, 371));

        jButton3.setAction(actionMap.get("showMainPanel")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(799, Short.MAX_VALUE)
                .addComponent(jButton3)
                .addGap(120, 120, 120))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(98, 98, 98)
                .addComponent(jButton3)
                .addContainerGap(349, Short.MAX_VALUE))
        );

        centerPanel.add(jPanel1, "jPanel1");

        jLabel17.setText(resourceMap.getString("jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N
        centerPanel.add(jLabel17, "card8");

        jPanel3.setBackground(resourceMap.getColor("jPanel3.background")); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setPreferredSize(new java.awt.Dimension(579, 371));

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jTextField4.setText(resourceMap.getString("jTextField4.text")); // NOI18N
        jTextField4.setName("jTextField4"); // NOI18N

        jTextField5.setText(resourceMap.getString("jTextField5.text")); // NOI18N
        jTextField5.setName("jTextField5"); // NOI18N

        jButton11.setAction(actionMap.get("okMarcaciones")); // NOI18N
        jButton11.setName("jButton11"); // NOI18N

        jCheckBox1.setAction(actionMap.get("jCheckbox2Change")); // NOI18N
        jCheckBox1.setText(resourceMap.getString("jCheckBox1.text")); // NOI18N
        jCheckBox1.setName("jCheckBox1"); // NOI18N

        jCheckBox2.setAction(actionMap.get("okReprocesoMarcaciones")); // NOI18N
        jCheckBox2.setText(resourceMap.getString("jCheckBox2.text")); // NOI18N
        jCheckBox2.setName("jCheckBox2"); // NOI18N

        jButton16.setAction(actionMap.get("cancelarMarcaciones")); // NOI18N
        jButton16.setName("jButton16"); // NOI18N

        jTextField9.setText(resourceMap.getString("jTextField9.text")); // NOI18N
        jTextField9.setName("jTextField9"); // NOI18N

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        jTextField10.setText(resourceMap.getString("jTextField10.text")); // NOI18N
        jTextField10.setName("jTextField10"); // NOI18N

        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox2)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(8, 8, 8)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox1)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField10)
                            .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButton11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton16)))
                .addContainerGap(674, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jCheckBox1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addGap(65, 65, 65)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton11)
                    .addComponent(jButton16))
                .addContainerGap(228, Short.MAX_VALUE))
        );

        centerPanel.add(jPanel3, "jPanel3");

        mainMenu.setBackground(resourceMap.getColor("mainMenu.background")); // NOI18N
        mainMenu.setName("mainMenu"); // NOI18N
        mainMenu.setPreferredSize(new java.awt.Dimension(579, 371));
        mainMenu.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        buttonSincronizarMaestros.setAction(actionMap.get("synchMaestro")); // NOI18N
        buttonSincronizarMaestros.setText(resourceMap.getString("buttonSincronizarMaestros.text")); // NOI18N
        buttonSincronizarMaestros.setName("buttonSincronizarMaestros"); // NOI18N
        mainMenu.add(buttonSincronizarMaestros, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 160, 219, -1));

        buttonGenerarArchivo.setAction(actionMap.get("generateProps")); // NOI18N
        buttonGenerarArchivo.setText(resourceMap.getString("buttonGenerarArchivo.text")); // NOI18N
        buttonGenerarArchivo.setName("buttonGenerarArchivo"); // NOI18N
        mainMenu.add(buttonGenerarArchivo, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 220, 219, -1));

        buttonSincronizarCentrosCosto.setAction(actionMap.get("synchCentroCosto")); // NOI18N
        buttonSincronizarCentrosCosto.setName("buttonSincronizarCentrosCosto"); // NOI18N
        mainMenu.add(buttonSincronizarCentrosCosto, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 190, 219, -1));

        buttonCorreo.setAction(actionMap.get("pantConfigMail")); // NOI18N
        buttonCorreo.setText(resourceMap.getString("buttonCorreo.text")); // NOI18N
        buttonCorreo.setName("buttonCorreo"); // NOI18N
        mainMenu.add(buttonCorreo, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 250, 220, -1));

        jButton25.setAction(actionMap.get("paintMarcacionesMenu")); // NOI18N
        jButton25.setText(resourceMap.getString("jButton25.text")); // NOI18N
        jButton25.setName("jButton25"); // NOI18N
        mainMenu.add(jButton25, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 70, 220, -1));

        buttonCambiarPassword.setAction(actionMap.get("showCambiarContrasena")); // NOI18N
        buttonCambiarPassword.setText(resourceMap.getString("buttonCambiarPassword.text")); // NOI18N
        buttonCambiarPassword.setName("buttonCambiarPassword"); // NOI18N
        mainMenu.add(buttonCambiarPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 130, 220, -1));

        buttonHistorial.setAction(actionMap.get("paintHistorial")); // NOI18N
        buttonHistorial.setText(resourceMap.getString("buttonHistorial.text")); // NOI18N
        buttonHistorial.setName("buttonHistorial"); // NOI18N
        mainMenu.add(buttonHistorial, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 280, 220, -1));

        buttonAdminUsuarios.setAction(actionMap.get("showAdminUsuarios")); // NOI18N
        buttonAdminUsuarios.setText(resourceMap.getString("buttonAdminUsuarios.text")); // NOI18N
        buttonAdminUsuarios.setName("buttonAdminUsuarios"); // NOI18N
        mainMenu.add(buttonAdminUsuarios, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 310, 220, -1));

        buttonEditarPerfil.setAction(actionMap.get("showEditarPerfil")); // NOI18N
        buttonEditarPerfil.setText(resourceMap.getString("buttonEditarPerfil.text")); // NOI18N
        buttonEditarPerfil.setName("buttonEditarPerfil"); // NOI18N
        mainMenu.add(buttonEditarPerfil, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 100, 220, -1));

        centerPanel.add(mainMenu, "mainMenu");

        jPanel15.setBackground(resourceMap.getColor("jPanel15.background")); // NOI18N
        jPanel15.setName("jPanel15"); // NOI18N
        jPanel15.setPreferredSize(new java.awt.Dimension(579, 371));
        jPanel15.setLayout(new javax.swing.BoxLayout(jPanel15, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jTextPane1.setEditable(false);
        jTextPane1.setName("jTextPane1"); // NOI18N
        jScrollPane3.setViewportView(jTextPane1);

        jPanel15.add(jScrollPane3);

        centerPanel.add(jPanel15, "jPanel15");

        panelLogMarcaciones.setBackground(resourceMap.getColor("panelLogMarcaciones.background")); // NOI18N
        panelLogMarcaciones.setName("panelLogMarcaciones"); // NOI18N
        panelLogMarcaciones.setPreferredSize(new java.awt.Dimension(579, 371));
        panelLogMarcaciones.setLayout(new java.awt.BorderLayout());

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        tablaMarcaciones.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tablaMarcaciones.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tablaMarcaciones.setName("tablaMarcaciones"); // NOI18N
        jScrollPane4.setViewportView(tablaMarcaciones);

        panelLogMarcaciones.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        centerPanel.add(panelLogMarcaciones, "panelLogMarcaciones");

        logMarcsForm.setBackground(resourceMap.getColor("logMarcsForm.background")); // NOI18N
        logMarcsForm.setName("logMarcsForm"); // NOI18N
        logMarcsForm.setPreferredSize(new java.awt.Dimension(579, 371));
        logMarcsForm.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel18.setText(resourceMap.getString("jLabel18.text")); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N
        logMarcsForm.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(201, 11, 197, -1));

        desdeSiboMarcs.setName("desdeSiboMarcs"); // NOI18N
        logMarcsForm.add(desdeSiboMarcs, new org.netbeans.lib.awtextra.AbsoluteConstraints(201, 32, 124, -1));

        jTextField16.setName("jTextField16"); // NOI18N
        jTextField16.setNextFocusableComponent(jTextField27);
        logMarcsForm.add(jTextField16, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 30, 131, -1));

        jButton13.setAction(actionMap.get("pantLogMarcaciones")); // NOI18N
        jButton13.setText(resourceMap.getString("jButton13.text")); // NOI18N
        jButton13.setName("jButton13"); // NOI18N
        jButton13.setNextFocusableComponent(desdeSiboMarcs);
        logMarcsForm.add(jButton13, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 420, -1, -1));

        jTextField25.setName("jTextField25"); // NOI18N
        jTextField25.setNextFocusableComponent(jTextField26);
        logMarcsForm.add(jTextField25, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 130, 179, -1));

        jLabel27.setText(resourceMap.getString("jLabel27.text")); // NOI18N
        jLabel27.setName("jLabel27"); // NOI18N
        logMarcsForm.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, -1, -1));

        jTextField26.setName("jTextField26"); // NOI18N
        jTextField26.setNextFocusableComponent(jButton13);
        logMarcsForm.add(jTextField26, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 170, 179, -1));

        jLabel28.setText(resourceMap.getString("jLabel28.text")); // NOI18N
        jLabel28.setName("jLabel28"); // NOI18N
        logMarcsForm.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, -1, -1));

        jLabel29.setText(resourceMap.getString("jLabel29.text")); // NOI18N
        jLabel29.setName("jLabel29"); // NOI18N
        logMarcsForm.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, -1, -1));

        jLabel30.setText(resourceMap.getString("jLabel30.text")); // NOI18N
        jLabel30.setName("jLabel30"); // NOI18N
        logMarcsForm.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 70, 197, -1));

        jTextField27.setName("jTextField27"); // NOI18N
        logMarcsForm.add(jTextField27, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 90, 124, -1));

        jTextField28.setName("jTextField28"); // NOI18N
        jTextField28.setNextFocusableComponent(jTextField25);
        logMarcsForm.add(jTextField28, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 90, 131, -1));

        jLabel31.setText(resourceMap.getString("jLabel31.text")); // NOI18N
        jLabel31.setName("jLabel31"); // NOI18N
        logMarcsForm.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));

        jLabel32.setText(resourceMap.getString("jLabel32.text")); // NOI18N
        jLabel32.setName("jLabel32"); // NOI18N
        logMarcsForm.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 290, -1, -1));

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.setName("jComboBox1"); // NOI18N
        logMarcsForm.add(jComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 250, 180, -1));

        jRadioButton3.setAction(actionMap.get("descSelected")); // NOI18N
        buttonGroup2.add(jRadioButton3);
        jRadioButton3.setName("jRadioButton3"); // NOI18N
        logMarcsForm.add(jRadioButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 250, -1, -1));

        jRadioButton4.setAction(actionMap.get("ascSelected")); // NOI18N
        buttonGroup2.add(jRadioButton4);
        jRadioButton4.setName("jRadioButton4"); // NOI18N
        logMarcsForm.add(jRadioButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 250, -1, -1));

        jLabel33.setText(resourceMap.getString("jLabel33.text")); // NOI18N
        jLabel33.setName("jLabel33"); // NOI18N
        logMarcsForm.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, -1, -1));

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox2.setAction(actionMap.get("estadoMarcsSelected")); // NOI18N
        jComboBox2.setName("jComboBox2"); // NOI18N
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });
        logMarcsForm.add(jComboBox2, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 210, 180, -1));

        jLabel74.setText(resourceMap.getString("jLabel74.text")); // NOI18N
        jLabel74.setName("jLabel74"); // NOI18N
        logMarcsForm.add(jLabel74, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 70, -1, -1));

        jLabel75.setText(resourceMap.getString("jLabel75.text")); // NOI18N
        jLabel75.setName("jLabel75"); // NOI18N
        logMarcsForm.add(jLabel75, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 10, -1, -1));

        descripcionesMarcacion.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        descripcionesMarcacion.setName("descripcionesMarcacion"); // NOI18N
        logMarcsForm.add(descripcionesMarcacion, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 290, 290, -1));

        jLabel85.setText(resourceMap.getString("jLabel85.text")); // NOI18N
        jLabel85.setName("jLabel85"); // NOI18N
        logMarcsForm.add(jLabel85, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 250, -1, -1));

        jLabel87.setText(resourceMap.getString("jLabel87.text")); // NOI18N
        jLabel87.setName("jLabel87"); // NOI18N
        logMarcsForm.add(jLabel87, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 370, -1, 20));

        botonExportarExcel.setAction(actionMap.get("exportarMarcacionesExcel")); // NOI18N
        botonExportarExcel.setText(resourceMap.getString("botonExportarExcel.text")); // NOI18N
        botonExportarExcel.setName("botonExportarExcel"); // NOI18N
        logMarcsForm.add(botonExportarExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 420, 180, -1));

        jLabel91.setText(resourceMap.getString("jLabel91.text")); // NOI18N
        jLabel91.setName("jLabel91"); // NOI18N
        logMarcsForm.add(jLabel91, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 330, -1, -1));

        comboDivsMarcacion.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboDivsMarcacion.setName("comboDivsMarcacion"); // NOI18N
        comboDivsMarcacion.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                comboDivsMarcacionPropertyChange(evt);
            }
        });
        logMarcsForm.add(comboDivsMarcacion, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 330, 290, -1));

        comboSubsMarcacion.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboSubsMarcacion.setName("comboSubsMarcacion"); // NOI18N
        logMarcsForm.add(comboSubsMarcacion, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 370, 290, -1));

        centerPanel.add(logMarcsForm, "logMarcsForm");

        marcacionesMenu.setBackground(resourceMap.getColor("marcacionesMenu.background")); // NOI18N
        marcacionesMenu.setName("marcacionesMenu"); // NOI18N
        marcacionesMenu.setPreferredSize(new java.awt.Dimension(579, 371));
        marcacionesMenu.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton36.setAction(actionMap.get("paintNovedadesPaqueteForm")); // NOI18N
        jButton36.setText(resourceMap.getString("jButton36.text")); // NOI18N
        jButton36.setName("jButton36"); // NOI18N
        marcacionesMenu.add(jButton36, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 110, 220, -1));

        jButton37.setAction(actionMap.get("paintLogMarcsForm")); // NOI18N
        jButton37.setText(resourceMap.getString("jButton37.text")); // NOI18N
        jButton37.setName("jButton37"); // NOI18N
        marcacionesMenu.add(jButton37, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 70, 220, -1));

        centerPanel.add(marcacionesMenu, "marcacionesMenu");

        panelLogNovedades.setBackground(resourceMap.getColor("panelLogNovedades.background")); // NOI18N
        panelLogNovedades.setName("panelLogNovedades"); // NOI18N
        panelLogNovedades.setPreferredSize(new java.awt.Dimension(579, 371));
        panelLogNovedades.setLayout(new java.awt.BorderLayout());

        jScrollPane6.setName("jScrollPane6"); // NOI18N

        tablaNovedades.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tablaNovedades.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tablaNovedades.setName("tablaNovedades"); // NOI18N
        jScrollPane6.setViewportView(tablaNovedades);

        panelLogNovedades.add(jScrollPane6, java.awt.BorderLayout.CENTER);

        centerPanel.add(panelLogNovedades, "panelLogNovedades");

        logNovedadesForm.setBackground(resourceMap.getColor("logNovedadesForm.background")); // NOI18N
        logNovedadesForm.setName("logNovedadesForm"); // NOI18N
        logNovedadesForm.setPreferredSize(new java.awt.Dimension(579, 371));
        logNovedadesForm.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel37.setText(resourceMap.getString("jLabel37.text")); // NOI18N
        jLabel37.setName("jLabel37"); // NOI18N
        logNovedadesForm.add(jLabel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(201, 11, 197, -1));

        desdeSiboNovedades.setName("desdeSiboNovedades"); // NOI18N
        logNovedadesForm.add(desdeSiboNovedades, new org.netbeans.lib.awtextra.AbsoluteConstraints(201, 32, 124, -1));

        hastaSiboNovedades.setName("hastaSiboNovedades"); // NOI18N
        hastaSiboNovedades.setNextFocusableComponent(jTextField27);
        logNovedadesForm.add(hastaSiboNovedades, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 30, 131, -1));

        jButton24.setAction(actionMap.get("paintNovedades")); // NOI18N
        jButton24.setText(resourceMap.getString("jButton24.text")); // NOI18N
        jButton24.setName("jButton24"); // NOI18N
        jButton24.setNextFocusableComponent(desdeSiboMarcs);
        logNovedadesForm.add(jButton24, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 430, -1, -1));

        jTextField31.setName("jTextField31"); // NOI18N
        jTextField31.setNextFocusableComponent(jTextField26);
        logNovedadesForm.add(jTextField31, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 130, 179, -1));

        jLabel38.setText(resourceMap.getString("jLabel38.text")); // NOI18N
        jLabel38.setName("jLabel38"); // NOI18N
        logNovedadesForm.add(jLabel38, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, -1, -1));

        jTextField32.setName("jTextField32"); // NOI18N
        jTextField32.setNextFocusableComponent(jButton13);
        logNovedadesForm.add(jTextField32, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 170, 179, -1));

        jLabel39.setText(resourceMap.getString("jLabel39.text")); // NOI18N
        jLabel39.setName("jLabel39"); // NOI18N
        logNovedadesForm.add(jLabel39, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, -1, -1));

        jLabel40.setText(resourceMap.getString("jLabel40.text")); // NOI18N
        jLabel40.setName("jLabel40"); // NOI18N
        logNovedadesForm.add(jLabel40, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, -1, -1));

        jLabel41.setText(resourceMap.getString("jLabel41.text")); // NOI18N
        jLabel41.setName("jLabel41"); // NOI18N
        logNovedadesForm.add(jLabel41, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 70, 197, -1));

        desdeSapNovedades.setName("desdeSapNovedades"); // NOI18N
        logNovedadesForm.add(desdeSapNovedades, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 90, 124, -1));

        hastaSapNovedades.setName("hastaSapNovedades"); // NOI18N
        hastaSapNovedades.setNextFocusableComponent(jTextField25);
        logNovedadesForm.add(hastaSapNovedades, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 90, 131, -1));

        jLabel42.setText(resourceMap.getString("jLabel42.text")); // NOI18N
        jLabel42.setName("jLabel42"); // NOI18N
        logNovedadesForm.add(jLabel42, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));

        jLabel43.setText(resourceMap.getString("jLabel43.text")); // NOI18N
        jLabel43.setName("jLabel43"); // NOI18N
        logNovedadesForm.add(jLabel43, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 250, -1, -1));

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox3.setName("jComboBox3"); // NOI18N
        logNovedadesForm.add(jComboBox3, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 250, 180, -1));

        jRadioButton5.setAction(actionMap.get("descSelected")); // NOI18N
        buttonGroup2.add(jRadioButton5);
        jRadioButton5.setName("jRadioButton5"); // NOI18N
        logNovedadesForm.add(jRadioButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 250, -1, -1));

        jRadioButton6.setAction(actionMap.get("ascSelected")); // NOI18N
        buttonGroup2.add(jRadioButton6);
        jRadioButton6.setName("jRadioButton6"); // NOI18N
        logNovedadesForm.add(jRadioButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 250, -1, -1));

        jLabel44.setText(resourceMap.getString("jLabel44.text")); // NOI18N
        jLabel44.setName("jLabel44"); // NOI18N
        logNovedadesForm.add(jLabel44, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, -1, -1));

        comboEstadoNovedades.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboEstadoNovedades.setAction(actionMap.get("estadoNovedadesSelected")); // NOI18N
        comboEstadoNovedades.setName("comboEstadoNovedades"); // NOI18N
        logNovedadesForm.add(comboEstadoNovedades, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 210, 180, -1));

        jLabel72.setText(resourceMap.getString("jLabel72.text")); // NOI18N
        jLabel72.setName("jLabel72"); // NOI18N
        logNovedadesForm.add(jLabel72, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 10, -1, -1));

        jLabel73.setText(resourceMap.getString("jLabel73.text")); // NOI18N
        jLabel73.setName("jLabel73"); // NOI18N
        logNovedadesForm.add(jLabel73, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 70, -1, -1));

        jLabel84.setText(resourceMap.getString("jLabel84.text")); // NOI18N
        jLabel84.setName("jLabel84"); // NOI18N
        logNovedadesForm.add(jLabel84, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 290, -1, -1));

        descripcionesNovedad.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        descripcionesNovedad.setName("descripcionesNovedad"); // NOI18N
        logNovedadesForm.add(descripcionesNovedad, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 290, 290, -1));

        jButton15.setAction(actionMap.get("exportarNovedadesExcel")); // NOI18N
        jButton15.setText(resourceMap.getString("jButton15.text")); // NOI18N
        jButton15.setName("jButton15"); // NOI18N
        logNovedadesForm.add(jButton15, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 430, -1, -1));

        jLabel88.setText(resourceMap.getString("jLabel88.text")); // NOI18N
        jLabel88.setName("jLabel88"); // NOI18N
        logNovedadesForm.add(jLabel88, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 370, -1, 20));

        jLabel92.setText(resourceMap.getString("jLabel92.text")); // NOI18N
        jLabel92.setName("jLabel92"); // NOI18N
        logNovedadesForm.add(jLabel92, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 330, -1, -1));

        comboDivsNovedad.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboDivsNovedad.setName("comboDivsNovedad"); // NOI18N
        comboDivsNovedad.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                comboDivsNovedadPropertyChange(evt);
            }
        });
        logNovedadesForm.add(comboDivsNovedad, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 330, 290, -1));

        comboSubsNovedad.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboSubsNovedad.setName("comboSubsNovedad"); // NOI18N
        logNovedadesForm.add(comboSubsNovedad, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 370, 290, -1));

        centerPanel.add(logNovedadesForm, "logNovedadesForm");

        novedadesPaqueteForm.setBackground(resourceMap.getColor("novedadesPaqueteForm.background")); // NOI18N
        novedadesPaqueteForm.setName("novedadesPaqueteForm"); // NOI18N
        novedadesPaqueteForm.setPreferredSize(new java.awt.Dimension(579, 371));
        novedadesPaqueteForm.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel45.setText(resourceMap.getString("jLabel45.text")); // NOI18N
        jLabel45.setName("jLabel45"); // NOI18N
        novedadesPaqueteForm.add(jLabel45, new org.netbeans.lib.awtextra.AbsoluteConstraints(201, 11, 197, -1));

        desdeSiboPaquete.setName("desdeSiboPaquete"); // NOI18N
        novedadesPaqueteForm.add(desdeSiboPaquete, new org.netbeans.lib.awtextra.AbsoluteConstraints(201, 32, 124, -1));

        hastaSiboPaquete.setName("hastaSiboPaquete"); // NOI18N
        hastaSiboPaquete.setNextFocusableComponent(jTextField27);
        novedadesPaqueteForm.add(hastaSiboPaquete, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 30, 131, -1));

        jButton41.setAction(actionMap.get("paintNovedadesPaquete")); // NOI18N
        jButton41.setText(resourceMap.getString("jButton41.text")); // NOI18N
        jButton41.setName("jButton41"); // NOI18N
        jButton41.setNextFocusableComponent(desdeSiboMarcs);
        novedadesPaqueteForm.add(jButton41, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 460, -1, -1));

        jTextField37.setName("jTextField37"); // NOI18N
        jTextField37.setNextFocusableComponent(jTextField26);
        novedadesPaqueteForm.add(jTextField37, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 130, 179, -1));

        jLabel46.setText(resourceMap.getString("jLabel46.text")); // NOI18N
        jLabel46.setName("jLabel46"); // NOI18N
        novedadesPaqueteForm.add(jLabel46, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, -1, -1));

        jTextField38.setName("jTextField38"); // NOI18N
        jTextField38.setNextFocusableComponent(jButton13);
        novedadesPaqueteForm.add(jTextField38, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 170, 179, -1));

        jLabel47.setText(resourceMap.getString("jLabel47.text")); // NOI18N
        jLabel47.setName("jLabel47"); // NOI18N
        novedadesPaqueteForm.add(jLabel47, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, -1, -1));

        jLabel48.setText(resourceMap.getString("jLabel48.text")); // NOI18N
        jLabel48.setName("jLabel48"); // NOI18N
        novedadesPaqueteForm.add(jLabel48, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, -1, -1));

        jLabel49.setText(resourceMap.getString("jLabel49.text")); // NOI18N
        jLabel49.setName("jLabel49"); // NOI18N
        novedadesPaqueteForm.add(jLabel49, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 70, 197, -1));

        desdeSapPaquete.setName("desdeSapPaquete"); // NOI18N
        novedadesPaqueteForm.add(desdeSapPaquete, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 90, 124, -1));

        hastaSapPaquete.setName("hastaSapPaquete"); // NOI18N
        hastaSapPaquete.setNextFocusableComponent(jTextField25);
        novedadesPaqueteForm.add(hastaSapPaquete, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 90, 131, -1));

        jLabel50.setText(resourceMap.getString("jLabel50.text")); // NOI18N
        jLabel50.setName("jLabel50"); // NOI18N
        novedadesPaqueteForm.add(jLabel50, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));

        jLabel51.setText(resourceMap.getString("jLabel51.text")); // NOI18N
        jLabel51.setName("jLabel51"); // NOI18N
        novedadesPaqueteForm.add(jLabel51, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 250, -1, -1));

        jComboBox5.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox5.setName("jComboBox5"); // NOI18N
        novedadesPaqueteForm.add(jComboBox5, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 250, 180, -1));

        jRadioButton7.setAction(actionMap.get("descSelected")); // NOI18N
        buttonGroup2.add(jRadioButton7);
        jRadioButton7.setName("jRadioButton7"); // NOI18N
        novedadesPaqueteForm.add(jRadioButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 250, -1, -1));

        jRadioButton8.setAction(actionMap.get("ascSelected")); // NOI18N
        buttonGroup2.add(jRadioButton8);
        jRadioButton8.setName("jRadioButton8"); // NOI18N
        novedadesPaqueteForm.add(jRadioButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 250, -1, -1));

        jLabel52.setText(resourceMap.getString("jLabel52.text")); // NOI18N
        jLabel52.setName("jLabel52"); // NOI18N
        novedadesPaqueteForm.add(jLabel52, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, -1, -1));

        comboEstadoPaquete.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboEstadoPaquete.setAction(actionMap.get("estadoPaqueteSelected")); // NOI18N
        comboEstadoPaquete.setName("comboEstadoPaquete"); // NOI18N
        novedadesPaqueteForm.add(comboEstadoPaquete, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 210, 180, -1));

        jComboBox7.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox7.setName("tipoNovedadCombo"); // NOI18N
        novedadesPaqueteForm.add(jComboBox7, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 290, 180, -1));

        jLabel53.setText(resourceMap.getString("jLabel53.text")); // NOI18N
        jLabel53.setName("jLabel53"); // NOI18N
        novedadesPaqueteForm.add(jLabel53, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 290, 110, -1));

        jLabel70.setText(resourceMap.getString("jLabel70.text")); // NOI18N
        jLabel70.setName("jLabel70"); // NOI18N
        novedadesPaqueteForm.add(jLabel70, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 70, -1, -1));

        jLabel71.setText(resourceMap.getString("jLabel71.text")); // NOI18N
        jLabel71.setName("jLabel71"); // NOI18N
        novedadesPaqueteForm.add(jLabel71, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 10, -1, -1));

        jLabel86.setText(resourceMap.getString("jLabel86.text")); // NOI18N
        jLabel86.setName("jLabel86"); // NOI18N
        novedadesPaqueteForm.add(jLabel86, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 330, -1, -1));

        descripcionesPaquete.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        descripcionesPaquete.setName("descripcionesPaquete"); // NOI18N
        novedadesPaqueteForm.add(descripcionesPaquete, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 330, 290, -1));

        jButton21.setAction(actionMap.get("exportarPaqueteExcel")); // NOI18N
        jButton21.setText(resourceMap.getString("jButton21.text")); // NOI18N
        jButton21.setName("jButton21"); // NOI18N
        novedadesPaqueteForm.add(jButton21, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 460, -1, -1));

        jLabel89.setText(resourceMap.getString("jLabel89.text")); // NOI18N
        jLabel89.setName("jLabel89"); // NOI18N
        novedadesPaqueteForm.add(jLabel89, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 410, -1, 20));

        jLabel93.setText(resourceMap.getString("jLabel93.text")); // NOI18N
        jLabel93.setName("jLabel93"); // NOI18N
        novedadesPaqueteForm.add(jLabel93, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 370, -1, -1));

        comboDivsPaquete.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboDivsPaquete.setName("comboDivsPaquete"); // NOI18N
        comboDivsPaquete.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                comboDivsPaquetePropertyChange(evt);
            }
        });
        novedadesPaqueteForm.add(comboDivsPaquete, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 370, 290, -1));

        comboSubsPaquete.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboSubsPaquete.setName("comboSubsPaquete"); // NOI18N
        novedadesPaqueteForm.add(comboSubsPaquete, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 410, 290, -1));

        centerPanel.add(novedadesPaqueteForm, "novedadesPaqueteForm");

        panelLogNovedadesPaquete.setBackground(resourceMap.getColor("panelLogNovedadesPaquete.background")); // NOI18N
        panelLogNovedadesPaquete.setName("panelLogNovedadesPaquete"); // NOI18N
        panelLogNovedadesPaquete.setPreferredSize(new java.awt.Dimension(579, 371));
        panelLogNovedadesPaquete.setLayout(new java.awt.BorderLayout());

        jScrollPane7.setName("jScrollPane7"); // NOI18N

        tablaNovedadesPaquete.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tablaNovedadesPaquete.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tablaNovedadesPaquete.setName("tablaNovedadesPaquete"); // NOI18N
        jScrollPane7.setViewportView(tablaNovedadesPaquete);

        panelLogNovedadesPaquete.add(jScrollPane7, java.awt.BorderLayout.CENTER);

        centerPanel.add(panelLogNovedadesPaquete, "panelLogNovedadesPaquete");

        panelHistorial.setBackground(resourceMap.getColor("panelHistorial.background")); // NOI18N
        panelHistorial.setName("panelHistorial"); // NOI18N
        panelHistorial.setPreferredSize(new java.awt.Dimension(579, 371));
        panelHistorial.setLayout(new java.awt.BorderLayout());

        jScrollPane8.setName("jScrollPane8"); // NOI18N

        tablaHistorial.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tablaHistorial.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tablaHistorial.setName("tablaHistorial"); // NOI18N
        jScrollPane8.setViewportView(tablaHistorial);

        panelHistorial.add(jScrollPane8, java.awt.BorderLayout.CENTER);

        centerPanel.add(panelHistorial, "panelHistorial");

        historialForm.setBackground(resourceMap.getColor("historialForm.background")); // NOI18N
        historialForm.setName("historialForm"); // NOI18N
        historialForm.setPreferredSize(new java.awt.Dimension(579, 371));
        historialForm.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel54.setText(resourceMap.getString("jLabel54.text")); // NOI18N
        jLabel54.setName("jLabel54"); // NOI18N
        historialForm.add(jLabel54, new org.netbeans.lib.awtextra.AbsoluteConstraints(201, 11, 197, -1));

        jTextField41.setName("jTextField41"); // NOI18N
        historialForm.add(jTextField41, new org.netbeans.lib.awtextra.AbsoluteConstraints(201, 32, 124, -1));

        jTextField42.setName("jTextField42"); // NOI18N
        jTextField42.setNextFocusableComponent(jTextField27);
        historialForm.add(jTextField42, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 30, 131, -1));

        jButton46.setAction(actionMap.get("paintHistorialPanel")); // NOI18N
        jButton46.setText(resourceMap.getString("jButton46.text")); // NOI18N
        jButton46.setName("jButton46"); // NOI18N
        jButton46.setNextFocusableComponent(desdeSiboMarcs);
        historialForm.add(jButton46, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 320, -1, -1));

        jLabel55.setText(resourceMap.getString("jLabel55.text")); // NOI18N
        jLabel55.setName("jLabel55"); // NOI18N
        historialForm.add(jLabel55, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, -1, -1));

        jLabel56.setText(resourceMap.getString("jLabel56.text")); // NOI18N
        jLabel56.setName("jLabel56"); // NOI18N
        historialForm.add(jLabel56, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, -1, -1));

        jLabel57.setText(resourceMap.getString("jLabel57.text")); // NOI18N
        jLabel57.setName("jLabel57"); // NOI18N
        historialForm.add(jLabel57, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, -1, -1));

        jLabel58.setText(resourceMap.getString("jLabel58.text")); // NOI18N
        jLabel58.setName("jLabel58"); // NOI18N
        historialForm.add(jLabel58, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 70, 197, -1));

        jTextField45.setName("jTextField45"); // NOI18N
        historialForm.add(jTextField45, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 90, 124, -1));

        jTextField46.setName("jTextField46"); // NOI18N
        jTextField46.setNextFocusableComponent(jTextField25);
        historialForm.add(jTextField46, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 90, 131, -1));

        jLabel59.setText(resourceMap.getString("jLabel59.text")); // NOI18N
        jLabel59.setName("jLabel59"); // NOI18N
        historialForm.add(jLabel59, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));

        jLabel60.setText(resourceMap.getString("jLabel60.text")); // NOI18N
        jLabel60.setName("jLabel60"); // NOI18N
        historialForm.add(jLabel60, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 250, -1, -1));

        comboOrdenIntento.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboOrdenIntento.setName("comboOrdenIntento"); // NOI18N
        historialForm.add(comboOrdenIntento, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 250, 180, -1));

        jRadioButton9.setAction(actionMap.get("descSelected")); // NOI18N
        buttonGroup2.add(jRadioButton9);
        jRadioButton9.setName("jRadioButton9"); // NOI18N
        historialForm.add(jRadioButton9, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 250, -1, -1));

        jRadioButton10.setAction(actionMap.get("ascSelected")); // NOI18N
        buttonGroup2.add(jRadioButton10);
        jRadioButton10.setName("jRadioButton10"); // NOI18N
        historialForm.add(jRadioButton10, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 250, -1, -1));

        jLabel61.setText(resourceMap.getString("jLabel61.text")); // NOI18N
        jLabel61.setName("jLabel61"); // NOI18N
        historialForm.add(jLabel61, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, -1, -1));

        comboEstadoIntento.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboEstadoIntento.setAction(actionMap.get("estadoMarcsSelected")); // NOI18N
        comboEstadoIntento.setName("comboEstadoIntento"); // NOI18N
        historialForm.add(comboEstadoIntento, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 210, 180, -1));

        comboUsuario.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboUsuario.setName("comboUsuario"); // NOI18N
        historialForm.add(comboUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 130, 180, -1));

        comboTarea.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboTarea.setName("comboTarea"); // NOI18N
        historialForm.add(comboTarea, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 170, 180, -1));

        jLabel62.setText(resourceMap.getString("jLabel62.text")); // NOI18N
        jLabel62.setName("jLabel62"); // NOI18N
        historialForm.add(jLabel62, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 70, -1, -1));

        jLabel63.setText(resourceMap.getString("jLabel63.text")); // NOI18N
        jLabel63.setName("jLabel63"); // NOI18N
        historialForm.add(jLabel63, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 10, -1, -1));

        centerPanel.add(historialForm, "historialForm");

        loginPanel.setBackground(resourceMap.getColor("loginPanel.background")); // NOI18N
        loginPanel.setName("loginPanel"); // NOI18N
        loginPanel.setPreferredSize(new java.awt.Dimension(579, 371));
        loginPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel64.setText(resourceMap.getString("jLabel64.text")); // NOI18N
        jLabel64.setName("jLabel64"); // NOI18N
        loginPanel.add(jLabel64, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 120, -1, -1));

        jLabel65.setText(resourceMap.getString("jLabel65.text")); // NOI18N
        jLabel65.setName("jLabel65"); // NOI18N
        loginPanel.add(jLabel65, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 160, -1, 10));

        jButton12.setAction(actionMap.get("validarUsuario")); // NOI18N
        jButton12.setText(resourceMap.getString("jButton12.text")); // NOI18N
        jButton12.setName("jButton12"); // NOI18N
        loginPanel.add(jButton12, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 200, -1, -1));

        usuarioField.setText(resourceMap.getString("usuarioField.text")); // NOI18N
        usuarioField.setName("usuarioField"); // NOI18N
        loginPanel.add(usuarioField, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 120, 140, -1));

        jButton47.setAction(actionMap.get("quit")); // NOI18N
        jButton47.setText(resourceMap.getString("jButton47.text")); // NOI18N
        jButton47.setName("jButton47"); // NOI18N
        loginPanel.add(jButton47, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 200, -1, -1));

        passwordField.setText(resourceMap.getString("passwordField.text")); // NOI18N
        passwordField.setName("passwordField"); // NOI18N
        loginPanel.add(passwordField, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 150, 140, -1));

        centerPanel.add(loginPanel, "loginPanel");

        cambiarPasswordPanel.setBackground(resourceMap.getColor("cambiarPasswordPanel.background")); // NOI18N
        cambiarPasswordPanel.setName("cambiarPasswordPanel"); // NOI18N
        cambiarPasswordPanel.setPreferredSize(new java.awt.Dimension(579, 371));
        cambiarPasswordPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel66.setText(resourceMap.getString("jLabel66.text")); // NOI18N
        jLabel66.setName("jLabel66"); // NOI18N
        cambiarPasswordPanel.add(jLabel66, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 130, -1, -1));

        jLabel67.setText(resourceMap.getString("jLabel67.text")); // NOI18N
        jLabel67.setName("jLabel67"); // NOI18N
        cambiarPasswordPanel.add(jLabel67, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 160, -1, 10));

        jButton49.setAction(actionMap.get("cambiarContrasena")); // NOI18N
        jButton49.setText(resourceMap.getString("jButton49.text")); // NOI18N
        jButton49.setName("jButton49"); // NOI18N
        cambiarPasswordPanel.add(jButton49, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 240, -1, -1));

        jButton50.setAction(actionMap.get("showMainPanel")); // NOI18N
        jButton50.setText(resourceMap.getString("jButton50.text")); // NOI18N
        jButton50.setName("jButton50"); // NOI18N
        cambiarPasswordPanel.add(jButton50, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 240, -1, -1));

        nuevaContrasena1.setName("nuevaContrasena1"); // NOI18N
        cambiarPasswordPanel.add(nuevaContrasena1, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 150, 140, -1));

        nuevaContrasena2.setName("nuevaContrasena2"); // NOI18N
        cambiarPasswordPanel.add(nuevaContrasena2, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 180, 140, -1));

        jLabel68.setText(resourceMap.getString("jLabel68.text")); // NOI18N
        jLabel68.setName("jLabel68"); // NOI18N
        cambiarPasswordPanel.add(jLabel68, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 190, -1, 10));

        contrasenaErrorMessage.setFont(resourceMap.getFont("contrasenaErrorMessage.font")); // NOI18N
        contrasenaErrorMessage.setForeground(resourceMap.getColor("contrasenaErrorMessage.foreground")); // NOI18N
        contrasenaErrorMessage.setText(resourceMap.getString("contrasenaErrorMessage.text")); // NOI18N
        contrasenaErrorMessage.setName("contrasenaErrorMessage"); // NOI18N
        cambiarPasswordPanel.add(contrasenaErrorMessage, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 80, 410, 20));

        viejaContrasena.setText(resourceMap.getString("viejaContrasena.text")); // NOI18N
        viejaContrasena.setName("viejaContrasena"); // NOI18N
        cambiarPasswordPanel.add(viejaContrasena, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 120, 140, -1));

        centerPanel.add(cambiarPasswordPanel, "cambiarPasswordPanel");

        adminUsuariosPanel.setBackground(resourceMap.getColor("adminUsuariosPanel.background")); // NOI18N
        adminUsuariosPanel.setName("adminUsuariosPanel"); // NOI18N
        adminUsuariosPanel.setPreferredSize(new java.awt.Dimension(579, 371));
        adminUsuariosPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane9.setName("jScrollPane9"); // NOI18N

        listUsuarios.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        listUsuarios.setName("listUsuarios"); // NOI18N
        listUsuarios.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listUsuariosValueChanged(evt);
            }
        });
        jScrollPane9.setViewportView(listUsuarios);

        adminUsuariosPanel.add(jScrollPane9, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 30, 250, 220));

        buttonNuevoUsuario.setAction(actionMap.get("showNuevoUsuario")); // NOI18N
        buttonNuevoUsuario.setText(resourceMap.getString("buttonNuevoUsuario.text")); // NOI18N
        buttonNuevoUsuario.setName("buttonNuevoUsuario"); // NOI18N
        adminUsuariosPanel.add(buttonNuevoUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 30, 80, -1));

        buttonEliminarUsuario.setAction(actionMap.get("eliminarUsuario")); // NOI18N
        buttonEliminarUsuario.setText(resourceMap.getString("buttonEliminarUsuario.text")); // NOI18N
        buttonEliminarUsuario.setName("buttonEliminarUsuario"); // NOI18N
        adminUsuariosPanel.add(buttonEliminarUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 90, 80, -1));

        buttonEditarUsuario.setAction(actionMap.get("editarUsuario")); // NOI18N
        buttonEditarUsuario.setText(resourceMap.getString("buttonEditarUsuario.text")); // NOI18N
        buttonEditarUsuario.setName("buttonEditarUsuario"); // NOI18N
        adminUsuariosPanel.add(buttonEditarUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 60, 80, -1));

        centerPanel.add(adminUsuariosPanel, "adminUsuariosPanel");

        nuevoUsuarioPanel.setBackground(resourceMap.getColor("nuevoUsuarioPanel.background")); // NOI18N
        nuevoUsuarioPanel.setName("nuevoUsuarioPanel"); // NOI18N
        nuevoUsuarioPanel.setPreferredSize(new java.awt.Dimension(579, 371));
        nuevoUsuarioPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        nuevoUsuarioLogin.setText(resourceMap.getString("nuevoUsuarioLogin.text")); // NOI18N
        nuevoUsuarioLogin.setName("nuevoUsuarioLogin"); // NOI18N
        nuevoUsuarioPanel.add(nuevoUsuarioLogin, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 70, 170, -1));

        jButton2.setAction(actionMap.get("okNuevoUsuario")); // NOI18N
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        nuevoUsuarioPanel.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 170, -1, -1));

        jLabel69.setText(resourceMap.getString("jLabel69.text")); // NOI18N
        jLabel69.setName("jLabel69"); // NOI18N
        nuevoUsuarioPanel.add(jLabel69, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 70, -1, -1));

        nuevoErrorMessage.setFont(resourceMap.getFont("nuevoErrorMessage.font")); // NOI18N
        nuevoErrorMessage.setForeground(resourceMap.getColor("nuevoErrorMessage.foreground")); // NOI18N
        nuevoErrorMessage.setText(resourceMap.getString("nuevoErrorMessage.text")); // NOI18N
        nuevoErrorMessage.setName("nuevoErrorMessage"); // NOI18N
        nuevoUsuarioPanel.add(nuevoErrorMessage, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, 410, 20));

        radioAdmin.setAction(actionMap.get("checkTipoUsuario")); // NOI18N
        radioAdmin.setText(resourceMap.getString("radioAdmin.text")); // NOI18N
        radioAdmin.setName("radioAdmin"); // NOI18N
        nuevoUsuarioPanel.add(radioAdmin, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 110, -1, -1));

        radioUser.setAction(actionMap.get("checkTipoUsuario2")); // NOI18N
        radioUser.setText(resourceMap.getString("radioUser.text")); // NOI18N
        radioUser.setName("radioUser"); // NOI18N
        nuevoUsuarioPanel.add(radioUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 110, -1, -1));

        jButton4.setAction(actionMap.get("showAdminUsuarios")); // NOI18N
        jButton4.setText(resourceMap.getString("jButton4.text")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        nuevoUsuarioPanel.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 170, -1, -1));

        contrasenaErrorMessage2.setFont(resourceMap.getFont("contrasenaErrorMessage2.font")); // NOI18N
        contrasenaErrorMessage2.setForeground(resourceMap.getColor("contrasenaErrorMessage2.foreground")); // NOI18N
        contrasenaErrorMessage2.setText(resourceMap.getString("contrasenaErrorMessage2.text")); // NOI18N
        contrasenaErrorMessage2.setName("contrasenaErrorMessage2"); // NOI18N
        nuevoUsuarioPanel.add(contrasenaErrorMessage2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 20, 410, 20));

        centerPanel.add(nuevoUsuarioPanel, "nuevoUsuarioPanel");

        editarPerfilPanel.setBackground(resourceMap.getColor("editarPerfilPanel.background")); // NOI18N
        editarPerfilPanel.setName("editarPerfilPanel"); // NOI18N
        editarPerfilPanel.setPreferredSize(new java.awt.Dimension(579, 371));
        editarPerfilPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel76.setText(resourceMap.getString("jLabel76.text")); // NOI18N
        jLabel76.setName("jLabel76"); // NOI18N
        editarPerfilPanel.add(jLabel76, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 130, -1, -1));

        jButton51.setAction(actionMap.get("okEditarPerfil")); // NOI18N
        jButton51.setText(resourceMap.getString("jButton51.text")); // NOI18N
        jButton51.setName("jButton51"); // NOI18N
        editarPerfilPanel.add(jButton51, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 240, -1, -1));

        jButton52.setAction(actionMap.get("showMainPanel")); // NOI18N
        jButton52.setText(resourceMap.getString("jButton52.text")); // NOI18N
        jButton52.setName("jButton52"); // NOI18N
        editarPerfilPanel.add(jButton52, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 240, -1, -1));

        editarPerfilErrorMessage.setFont(resourceMap.getFont("editarPerfilErrorMessage.font")); // NOI18N
        editarPerfilErrorMessage.setForeground(resourceMap.getColor("editarPerfilErrorMessage.foreground")); // NOI18N
        editarPerfilErrorMessage.setName("editarPerfilErrorMessage"); // NOI18N
        editarPerfilPanel.add(editarPerfilErrorMessage, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 80, 410, 20));

        nombreUsuario.setText(resourceMap.getString("nombreUsuario.text")); // NOI18N
        nombreUsuario.setName("nombreUsuario"); // NOI18N
        editarPerfilPanel.add(nombreUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 120, 130, -1));

        centerPanel.add(editarPerfilPanel, "editarPerfilPanel");

        editarUsuarioPanel.setBackground(resourceMap.getColor("editarUsuarioPanel.background")); // NOI18N
        editarUsuarioPanel.setName("editarUsuarioPanel"); // NOI18N
        editarUsuarioPanel.setPreferredSize(new java.awt.Dimension(579, 371));
        editarUsuarioPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        listasPlantasScrollPanel.setName("listasPlantasScrollPanel"); // NOI18N

        listPlantasUsuario.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        listPlantasUsuario.setName("listPlantasUsuario"); // NOI18N
        listPlantasUsuario.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listPlantasUsuarioValueChanged(evt);
            }
        });
        listasPlantasScrollPanel.setViewportView(listPlantasUsuario);

        editarUsuarioPanel.add(listasPlantasScrollPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 90, 250, 220));

        botonAgregarPlanta.setAction(actionMap.get("showAsignarPlantaDialog")); // NOI18N
        botonAgregarPlanta.setText(resourceMap.getString("botonAgregarPlanta.text")); // NOI18N
        botonAgregarPlanta.setName("botonAgregarPlanta"); // NOI18N
        editarUsuarioPanel.add(botonAgregarPlanta, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 90, 130, -1));

        botonEliminarPlanta.setAction(actionMap.get("eliminarPlanta")); // NOI18N
        botonEliminarPlanta.setText(resourceMap.getString("botonEliminarPlanta.text")); // NOI18N
        botonEliminarPlanta.setName("botonEliminarPlanta"); // NOI18N
        editarUsuarioPanel.add(botonEliminarPlanta, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 120, 130, -1));

        jLabel78.setText(resourceMap.getString("jLabel78.text")); // NOI18N
        jLabel78.setName("jLabel78"); // NOI18N
        editarUsuarioPanel.add(jLabel78, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, -1, -1));

        labelNombreUsuarioEditar.setText(resourceMap.getString("labelNombreUsuarioEditar.text")); // NOI18N
        labelNombreUsuarioEditar.setName("labelNombreUsuarioEditar"); // NOI18N
        editarUsuarioPanel.add(labelNombreUsuarioEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 20, 200, -1));

        labelRolUsuarioEditar.setText(resourceMap.getString("labelRolUsuarioEditar.text")); // NOI18N
        labelRolUsuarioEditar.setName("labelRolUsuarioEditar"); // NOI18N
        editarUsuarioPanel.add(labelRolUsuarioEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 40, 200, -1));

        jLabel81.setText(resourceMap.getString("jLabel81.text")); // NOI18N
        jLabel81.setName("jLabel81"); // NOI18N
        editarUsuarioPanel.add(jLabel81, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, 100, -1));

        jLabel82.setText(resourceMap.getString("jLabel82.text")); // NOI18N
        jLabel82.setName("jLabel82"); // NOI18N
        editarUsuarioPanel.add(jLabel82, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, 40, -1));

        labelPlantasTodas.setText(resourceMap.getString("labelPlantasTodas.text")); // NOI18N
        labelPlantasTodas.setName("labelPlantasTodas"); // NOI18N
        editarUsuarioPanel.add(labelPlantasTodas, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 60, 70, -1));

        regresarEditarUsuario.setAction(actionMap.get("showAdminUsuarios")); // NOI18N
        regresarEditarUsuario.setText(resourceMap.getString("regresarEditarUsuario.text")); // NOI18N
        regresarEditarUsuario.setName("regresarEditarUsuario"); // NOI18N
        editarUsuarioPanel.add(regresarEditarUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 320, 120, -1));

        jSeparator1.setName("jSeparator1"); // NOI18N
        editarUsuarioPanel.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, 380, -1));

        asignarContrasena.setAction(actionMap.get("asignarContrasena")); // NOI18N
        asignarContrasena.setText(resourceMap.getString("asignarContrasena.text")); // NOI18N
        asignarContrasena.setName("asignarContrasena"); // NOI18N
        editarUsuarioPanel.add(asignarContrasena, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 150, 130, -1));

        centerPanel.add(editarUsuarioPanel, "editarUsuarioPanel");

        listaMaestrosPanel.setBackground(resourceMap.getColor("listaMaestrosPanel.background")); // NOI18N
        listaMaestrosPanel.setName("listaMaestrosPanel"); // NOI18N
        listaMaestrosPanel.setPreferredSize(new java.awt.Dimension(579, 371));
        listaMaestrosPanel.setLayout(new java.awt.BorderLayout());

        jScrollPane10.setName("jScrollPane10"); // NOI18N

        tablaMaestros.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tablaMaestros.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tablaMaestros.setName("tablaMaestros"); // NOI18N
        jScrollPane10.setViewportView(tablaMaestros);

        listaMaestrosPanel.add(jScrollPane10, java.awt.BorderLayout.CENTER);

        centerPanel.add(listaMaestrosPanel, "listaMaestrosPanel");

        contenedor.add(centerPanel, java.awt.BorderLayout.CENTER);

        headerPanel.setBackground(resourceMap.getColor("headerPanel.background")); // NOI18N
        headerPanel.setName("headerPanel"); // NOI18N
        headerPanel.setLayout(new java.awt.BorderLayout(30, 0));

        jLabel34.setIcon(resourceMap.getIcon("jLabel34.icon")); // NOI18N
        jLabel34.setText(resourceMap.getString("jLabel34.text")); // NOI18N
        jLabel34.setName("jLabel34"); // NOI18N
        headerPanel.add(jLabel34, java.awt.BorderLayout.WEST);

        welcomeLabel.setText(resourceMap.getString("welcomeLabel.text")); // NOI18N
        welcomeLabel.setName("welcomeLabel"); // NOI18N
        headerPanel.add(welcomeLabel, java.awt.BorderLayout.CENTER);

        versionLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        versionLabel.setText(resourceMap.getString("versionLabel.text")); // NOI18N
        versionLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        versionLabel.setName("versionLabel"); // NOI18N
        headerPanel.add(versionLabel, java.awt.BorderLayout.EAST);

        contenedor.add(headerPanel, java.awt.BorderLayout.PAGE_START);

        footerPanel.setBackground(resourceMap.getColor("footerPanel.background")); // NOI18N
        footerPanel.setName("footerPanel"); // NOI18N
        footerPanel.setLayout(new java.awt.CardLayout());

        jPanel13.setBackground(resourceMap.getColor("jPanel13.background")); // NOI18N
        jPanel13.setName("jPanel13"); // NOI18N
        jPanel13.setLayout(new java.awt.BorderLayout());

        jLabel4.setBackground(resourceMap.getColor("jLabel4.background")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel4.setName("jLabel4"); // NOI18N
        jPanel13.add(jLabel4, java.awt.BorderLayout.CENTER);

        footerPanel.add(jPanel13, "jPanel13");

        jPanel14.setBackground(resourceMap.getColor("jPanel14.background")); // NOI18N
        jPanel14.setName("jPanel14"); // NOI18N

        jButton23.setAction(actionMap.get("showMainPanel")); // NOI18N
        jButton23.setName("jButton23"); // NOI18N

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                .addContainerGap(919, Short.MAX_VALUE)
                .addComponent(jButton23))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButton23)
        );

        footerPanel.add(jPanel14, "jPanel14");

        footerMarcaciones.setBackground(resourceMap.getColor("footerMarcaciones.background")); // NOI18N
        footerMarcaciones.setName("footerMarcaciones"); // NOI18N
        footerMarcaciones.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        botonSubirSeleccionados.setAction(actionMap.get("subirMarcacionesSelected")); // NOI18N
        botonSubirSeleccionados.setText(resourceMap.getString("botonSubirSeleccionados.text")); // NOI18N
        botonSubirSeleccionados.setName("botonSubirSeleccionados"); // NOI18N
        footerMarcaciones.add(botonSubirSeleccionados, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 150, 30));

        botonRegresarFormulario.setAction(actionMap.get("paintLogMarcsForm")); // NOI18N
        botonRegresarFormulario.setText(resourceMap.getString("botonRegresarFormulario.text")); // NOI18N
        botonRegresarFormulario.setName("botonRegresarFormulario"); // NOI18N
        footerMarcaciones.add(botonRegresarFormulario, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 0, 140, 30));

        botonActualizar.setAction(actionMap.get("pantLogMarcaciones")); // NOI18N
        botonActualizar.setText(resourceMap.getString("botonActualizar.text")); // NOI18N
        botonActualizar.setName("botonActualizar"); // NOI18N
        footerMarcaciones.add(botonActualizar, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 0, 150, 30));

        botonDetener.setAction(actionMap.get("detener")); // NOI18N
        botonDetener.setText(resourceMap.getString("botonDetener.text")); // NOI18N
        botonDetener.setName("botonDetener"); // NOI18N
        footerMarcaciones.add(botonDetener, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 0, 150, 30));

        botonSubirTodos.setAction(actionMap.get("subirMarcacionesTodas")); // NOI18N
        botonSubirTodos.setText(resourceMap.getString("botonSubirTodos.text")); // NOI18N
        botonSubirTodos.setName("botonSubirTodos"); // NOI18N
        footerMarcaciones.add(botonSubirTodos, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 0, 150, 30));

        avPagMarcaciones.setAction(actionMap.get("avanzarPaginaMarcaciones")); // NOI18N
        avPagMarcaciones.setText(resourceMap.getString("avPagMarcaciones.text")); // NOI18N
        avPagMarcaciones.setName("avPagMarcaciones"); // NOI18N
        footerMarcaciones.add(avPagMarcaciones, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 0, -1, 30));

        rePagMarcaciones.setAction(actionMap.get("retrocederPaginaMarcaciones")); // NOI18N
        rePagMarcaciones.setText(resourceMap.getString("rePagMarcaciones.text")); // NOI18N
        rePagMarcaciones.setName("rePagMarcaciones"); // NOI18N
        footerMarcaciones.add(rePagMarcaciones, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 0, -1, 30));

        irapagMarcaciones.setAction(actionMap.get("irapagMarcaciones")); // NOI18N
        irapagMarcaciones.setText(resourceMap.getString("irapagMarcaciones.text")); // NOI18N
        irapagMarcaciones.setName("irapagMarcaciones"); // NOI18N
        footerMarcaciones.add(irapagMarcaciones, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 0, 90, 30));

        footerPanel.add(footerMarcaciones, "footerMarcaciones");

        footerNovedades.setBackground(resourceMap.getColor("footerNovedades.background")); // NOI18N
        footerNovedades.setName("footerNovedades"); // NOI18N
        footerNovedades.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        botonActualizarNovedades.setAction(actionMap.get("paintNovedades")); // NOI18N
        botonActualizarNovedades.setText(resourceMap.getString("botonActualizarNovedades.text")); // NOI18N
        botonActualizarNovedades.setName("botonActualizarNovedades"); // NOI18N
        footerNovedades.add(botonActualizarNovedades, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 0, 150, 30));

        botonDetenerNovedades.setAction(actionMap.get("detener")); // NOI18N
        botonDetenerNovedades.setText(resourceMap.getString("botonDetenerNovedades.text")); // NOI18N
        botonDetenerNovedades.setName("botonDetenerNovedades"); // NOI18N
        footerNovedades.add(botonDetenerNovedades, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 0, 150, 30));

        botonSubirNovedadesSeleccionadas.setAction(actionMap.get("subirNovedadesSelected")); // NOI18N
        botonSubirNovedadesSeleccionadas.setText(resourceMap.getString("botonSubirNovedadesSeleccionadas.text")); // NOI18N
        botonSubirNovedadesSeleccionadas.setName("botonSubirNovedadesSeleccionadas"); // NOI18N
        footerNovedades.add(botonSubirNovedadesSeleccionadas, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 150, 30));

        botonSubirTodasNovedades.setAction(actionMap.get("subirNovedadesTodas")); // NOI18N
        botonSubirTodasNovedades.setText(resourceMap.getString("botonSubirTodasNovedades.text")); // NOI18N
        botonSubirTodasNovedades.setName("botonSubirTodasNovedades"); // NOI18N
        footerNovedades.add(botonSubirTodasNovedades, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 0, 150, 30));

        botonRegresarNovedades.setAction(actionMap.get("paintNovedadesForm")); // NOI18N
        botonRegresarNovedades.setText(resourceMap.getString("botonRegresarNovedades.text")); // NOI18N
        botonRegresarNovedades.setName("botonRegresarNovedades"); // NOI18N
        footerNovedades.add(botonRegresarNovedades, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 0, 150, 30));

        rePagNovedades.setAction(actionMap.get("retrocederPaginaNovedades")); // NOI18N
        rePagNovedades.setText(resourceMap.getString("rePagNovedades.text")); // NOI18N
        rePagNovedades.setName("rePagNovedades"); // NOI18N
        footerNovedades.add(rePagNovedades, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 0, -1, 30));

        avPagNovedades.setAction(actionMap.get("avanzarPaginaNovedades")); // NOI18N
        avPagNovedades.setText(resourceMap.getString("avPagNovedades.text")); // NOI18N
        avPagNovedades.setName("avPagNovedades"); // NOI18N
        footerNovedades.add(avPagNovedades, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 0, -1, 30));

        irapagNovedades.setAction(actionMap.get("irapagNovedades")); // NOI18N
        irapagNovedades.setText(resourceMap.getString("irapagNovedades.text")); // NOI18N
        irapagNovedades.setName("irapagNovedades"); // NOI18N
        footerNovedades.add(irapagNovedades, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 0, 90, 30));

        footerPanel.add(footerNovedades, "footerNovedades");

        footerNovedadesPaquete.setBackground(resourceMap.getColor("footerNovedadesPaquete.background")); // NOI18N
        footerNovedadesPaquete.setName("footerNovedadesPaquete"); // NOI18N
        footerNovedadesPaquete.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        botonActualizarPaquete.setAction(actionMap.get("paintNovedadesPaquete")); // NOI18N
        botonActualizarPaquete.setText(resourceMap.getString("botonActualizarPaquete.text")); // NOI18N
        botonActualizarPaquete.setName("botonActualizarPaquete"); // NOI18N
        footerNovedadesPaquete.add(botonActualizarPaquete, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 0, 150, 30));

        botonDetenerPaquete.setAction(actionMap.get("detener")); // NOI18N
        botonDetenerPaquete.setText(resourceMap.getString("botonDetenerPaquete.text")); // NOI18N
        botonDetenerPaquete.setName("botonDetenerPaquete"); // NOI18N
        footerNovedadesPaquete.add(botonDetenerPaquete, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 0, 150, 30));

        botonSubirPaqueteSeleccionados.setAction(actionMap.get("subirNovedadesPaqueteSelected")); // NOI18N
        botonSubirPaqueteSeleccionados.setText(resourceMap.getString("botonSubirPaqueteSeleccionados.text")); // NOI18N
        botonSubirPaqueteSeleccionados.setName("botonSubirPaqueteSeleccionados"); // NOI18N
        footerNovedadesPaquete.add(botonSubirPaqueteSeleccionados, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 150, 30));

        botonSubirTodasPaquete.setAction(actionMap.get("subirNovedadesPaqueteTodas")); // NOI18N
        botonSubirTodasPaquete.setText(resourceMap.getString("botonSubirTodasPaquete.text")); // NOI18N
        botonSubirTodasPaquete.setName("botonSubirTodasPaquete"); // NOI18N
        footerNovedadesPaquete.add(botonSubirTodasPaquete, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 0, 150, 30));

        botonRegresarPaquete.setAction(actionMap.get("paintNovedadesPaqueteForm")); // NOI18N
        botonRegresarPaquete.setText(resourceMap.getString("botonRegresarPaquete.text")); // NOI18N
        botonRegresarPaquete.setName("botonRegresarPaquete"); // NOI18N
        footerNovedadesPaquete.add(botonRegresarPaquete, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 0, 150, 30));

        rePagPaquete.setAction(actionMap.get("retrocederPaginaPaquete")); // NOI18N
        rePagPaquete.setText(resourceMap.getString("rePagPaquete.text")); // NOI18N
        rePagPaquete.setName("rePagPaquete"); // NOI18N
        footerNovedadesPaquete.add(rePagPaquete, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 0, -1, 30));

        avPagPaquete.setAction(actionMap.get("avanzarPaginaPaquete")); // NOI18N
        avPagPaquete.setText(resourceMap.getString("avPagPaquete.text")); // NOI18N
        avPagPaquete.setName("avPagPaquete"); // NOI18N
        footerNovedadesPaquete.add(avPagPaquete, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 0, -1, 30));

        irapagPaquete.setAction(actionMap.get("irapagPaquete")); // NOI18N
        irapagPaquete.setText(resourceMap.getString("irapagPaquete.text")); // NOI18N
        irapagPaquete.setName("irapagPaquete"); // NOI18N
        footerNovedadesPaquete.add(irapagPaquete, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 0, 90, 30));

        footerPanel.add(footerNovedadesPaquete, "footerNovedadesPaquete");

        footerHistorial.setBackground(resourceMap.getColor("footerHistorial.background")); // NOI18N
        footerHistorial.setName("footerHistorial"); // NOI18N
        footerHistorial.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        regresarHistorial.setAction(actionMap.get("paintHistorial")); // NOI18N
        regresarHistorial.setText(resourceMap.getString("regresarHistorial.text")); // NOI18N
        regresarHistorial.setName("regresarHistorial"); // NOI18N
        footerHistorial.add(regresarHistorial, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 0, 150, 30));

        rePagHistorial.setAction(actionMap.get("retrocederPaginaHistorial")); // NOI18N
        rePagHistorial.setText(resourceMap.getString("rePagHistorial.text")); // NOI18N
        rePagHistorial.setName("rePagHistorial"); // NOI18N
        footerHistorial.add(rePagHistorial, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 0, -1, 30));

        avPagHistorial.setAction(actionMap.get("avanzarPaginaHistorial")); // NOI18N
        avPagHistorial.setText(resourceMap.getString("avPagHistorial.text")); // NOI18N
        avPagHistorial.setName("avPagHistorial"); // NOI18N
        footerHistorial.add(avPagHistorial, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 0, -1, 30));

        irapagHistorial.setAction(actionMap.get("irapagHistorial")); // NOI18N
        irapagHistorial.setText(resourceMap.getString("irapagHistorial.text")); // NOI18N
        irapagHistorial.setName("irapagHistorial"); // NOI18N
        footerHistorial.add(irapagHistorial, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 0, 90, 30));

        footerPanel.add(footerHistorial, "footerHistorial");

        footerMaestros.setBackground(resourceMap.getColor("footerMaestros.background")); // NOI18N
        footerMaestros.setName("footerMaestros"); // NOI18N
        footerMaestros.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        regresarMaestros.setAction(actionMap.get("showMainPanel")); // NOI18N
        regresarMaestros.setText(resourceMap.getString("regresarMaestros.text")); // NOI18N
        regresarMaestros.setName("regresarMaestros"); // NOI18N
        footerMaestros.add(regresarMaestros, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 0, 150, 30));

        descargarMaestro.setAction(actionMap.get("descargarMaestro")); // NOI18N
        descargarMaestro.setText(resourceMap.getString("descargarMaestro.text")); // NOI18N
        descargarMaestro.setName("descargarMaestro"); // NOI18N
        footerMaestros.add(descargarMaestro, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 150, 30));

        footerPanel.add(footerMaestros, "footerMaestros");

        footerVerConexiones.setBackground(resourceMap.getColor("footerVerConexiones.background")); // NOI18N
        footerVerConexiones.setName("footerVerConexiones"); // NOI18N

        jButton28.setAction(actionMap.get("showLoginPanel")); // NOI18N
        jButton28.setName("jButton28"); // NOI18N

        javax.swing.GroupLayout footerVerConexionesLayout = new javax.swing.GroupLayout(footerVerConexiones);
        footerVerConexiones.setLayout(footerVerConexionesLayout);
        footerVerConexionesLayout.setHorizontalGroup(
            footerVerConexionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, footerVerConexionesLayout.createSequentialGroup()
                .addContainerGap(957, Short.MAX_VALUE)
                .addComponent(jButton28))
        );
        footerVerConexionesLayout.setVerticalGroup(
            footerVerConexionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButton28)
        );

        footerPanel.add(footerVerConexiones, "footerVerConexiones");

        contenedor.add(footerPanel, java.awt.BorderLayout.PAGE_END);

        mainPanel.add(contenedor);

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setAction(actionMap.get("pantVerConexiones")); // NOI18N
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N
        fileMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileMenuActionPerformed(evt);
            }
        });

        jMenuItem1.setAction(actionMap.get("pantVerConexiones")); // NOI18N
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        fileMenu.add(jMenuItem1);

        exitMenuItem.setAction(actionMap.get("myQuit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setText(resourceMap.getString("aboutMenuItem.text")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 1020, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 850, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        jDialog1.setTitle(resourceMap.getString("jDialog1.title")); // NOI18N
        jDialog1.setModal(true);
        jDialog1.setName("jDialog1"); // NOI18N
        jDialog1.setResizable(false);

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jTextField1.setText(resourceMap.getString("jTextField1.text")); // NOI18N
        jTextField1.setName("jTextField1"); // NOI18N
        jTextField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField1FocusGained(evt);
            }
        });

        jButton5.setAction(actionMap.get("okEncriptar")); // NOI18N
        jButton5.setName("jButton5"); // NOI18N

        jButton6.setAction(actionMap.get("showMainPanel")); // NOI18N
        jButton6.setName("jButton6"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jTextField2.setBackground(resourceMap.getColor("jTextField2.background")); // NOI18N
        jTextField2.setEditable(false);
        jTextField2.setText(resourceMap.getString("jTextField2.text")); // NOI18N
        jTextField2.setName("jTextField2"); // NOI18N
        jTextField2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField2FocusGained(evt);
            }
        });

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jDialog1Layout.createSequentialGroup()
                        .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton5)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE))))
                .addGap(54, 54, 54))
            .addGroup(jDialog1Layout.createSequentialGroup()
                .addGap(159, 159, 159)
                .addComponent(jButton6)
                .addContainerGap(214, Short.MAX_VALUE))
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog1Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton6)
                .addContainerGap(41, Short.MAX_VALUE))
        );

        jFileChooser1.setName("jFileChooser1"); // NOI18N

        jButton22.setText(resourceMap.getString("jButton22.text")); // NOI18N
        jButton22.setName("jButton22"); // NOI18N

        jOptionPane1.setName("jOptionPane1"); // NOI18N

        nuevoCorreoDialog.setTitle(resourceMap.getString("nuevoCorreoDialog.title")); // NOI18N
        nuevoCorreoDialog.setLocationByPlatform(true);
        nuevoCorreoDialog.setModal(true);
        nuevoCorreoDialog.setName("nuevoCorreoDialog"); // NOI18N
        nuevoCorreoDialog.setResizable(false);
        nuevoCorreoDialog.getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel35.setText(resourceMap.getString("jLabel35.text")); // NOI18N
        jLabel35.setName("jLabel35"); // NOI18N
        nuevoCorreoDialog.getContentPane().add(jLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 70, -1));

        subdivisionesCombo.setEditable(true);
        subdivisionesCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        subdivisionesCombo.setName("subdivisionesCombo"); // NOI18N
        nuevoCorreoDialog.getContentPane().add(subdivisionesCombo, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 20, -1, -1));

        jLabel36.setText(resourceMap.getString("jLabel36.text")); // NOI18N
        jLabel36.setName("jLabel36"); // NOI18N
        nuevoCorreoDialog.getContentPane().add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, 60, -1));

        nuevoCorreoText.setText(resourceMap.getString("nuevoCorreoText.text")); // NOI18N
        nuevoCorreoText.setName("nuevoCorreoText"); // NOI18N
        nuevoCorreoDialog.getContentPane().add(nuevoCorreoText, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 60, 220, -1));

        jButton8.setAction(actionMap.get("saveNuevoCorreo")); // NOI18N
        jButton8.setText(resourceMap.getString("jButton8.text")); // NOI18N
        jButton8.setName("jButton8"); // NOI18N
        nuevoCorreoDialog.getContentPane().add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 110, 100, -1));

        jButton10.setAction(actionMap.get("cancelNuevoCorreo")); // NOI18N
        jButton10.setText(resourceMap.getString("jButton10.text")); // NOI18N
        jButton10.setName("jButton10"); // NOI18N
        nuevoCorreoDialog.getContentPane().add(jButton10, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 110, 100, -1));

        nuevoCorreoDialog.getAccessibleContext().setAccessibleParent(correoPanel);

        eliminarUsuarioPane.setName("eliminarUsuarioPane"); // NOI18N

        jDialog2.setName("jDialog2"); // NOI18N

        javax.swing.GroupLayout jDialog2Layout = new javax.swing.GroupLayout(jDialog2.getContentPane());
        jDialog2.getContentPane().setLayout(jDialog2Layout);
        jDialog2Layout.setHorizontalGroup(
            jDialog2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jDialog2Layout.setVerticalGroup(
            jDialog2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        jOptionPane2.setMessageType(JOptionPane.YES_NO_OPTION);
        jOptionPane2.setWantsInput(true);
        jOptionPane2.setName("jOptionPane2"); // NOI18N

        jButton40.setAction(actionMap.get("subirMarcacionesTodas")); // NOI18N
        jButton40.setText(resourceMap.getString("jButton40.text")); // NOI18N
        jButton40.setName("jButton40"); // NOI18N

        irapag.setTitle(resourceMap.getString("irapag.title")); // NOI18N
        irapag.setModal(true);
        irapag.setName("irapag"); // NOI18N
        irapag.setResizable(false);

        botonIrapagOK.setAction(actionMap.get("goToPage")); // NOI18N
        botonIrapagOK.setText(resourceMap.getString("botonIrapagOK.text")); // NOI18N
        botonIrapagOK.setName("botonIrapagOK"); // NOI18N

        irapagTextField.setText(resourceMap.getString("irapagTextField.text")); // NOI18N
        irapagTextField.setName("irapagTextField"); // NOI18N

        jLabel77.setText(resourceMap.getString("jLabel77.text")); // NOI18N
        jLabel77.setName("jLabel77"); // NOI18N

        botonIrapagCancelar.setAction(actionMap.get("cerrarIrapag")); // NOI18N
        botonIrapagCancelar.setText(resourceMap.getString("botonIrapagCancelar.text")); // NOI18N
        botonIrapagCancelar.setName("botonIrapagCancelar"); // NOI18N

        javax.swing.GroupLayout irapagLayout = new javax.swing.GroupLayout(irapag.getContentPane());
        irapag.getContentPane().setLayout(irapagLayout);
        irapagLayout.setHorizontalGroup(
            irapagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(irapagLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(irapagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel77)
                    .addComponent(botonIrapagOK, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(irapagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(irapagTextField)
                    .addComponent(botonIrapagCancelar, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        irapagLayout.setVerticalGroup(
            irapagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(irapagLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(irapagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel77)
                    .addComponent(irapagTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(irapagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botonIrapagOK)
                    .addComponent(botonIrapagCancelar))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        irapag.getAccessibleContext().setAccessibleParent(centerPanel);

        asignarPlanta.setTitle(resourceMap.getString("asignarPlanta.title")); // NOI18N
        asignarPlanta.setModal(true);
        asignarPlanta.setName("asignarPlanta"); // NOI18N
        asignarPlanta.setResizable(false);

        comboPlantasAsignar.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboPlantasAsignar.setName("comboPlantasAsignar"); // NOI18N

        jLabel80.setText(resourceMap.getString("jLabel80.text")); // NOI18N
        jLabel80.setName("jLabel80"); // NOI18N

        botonAsignarPlantaOk.setAction(actionMap.get("asignarPlantaAUsuario")); // NOI18N
        botonAsignarPlantaOk.setText(resourceMap.getString("botonAsignarPlantaOk.text")); // NOI18N
        botonAsignarPlantaOk.setName("botonAsignarPlantaOk"); // NOI18N

        botonCerrarAsignarPlanta.setAction(actionMap.get("cerrarAsignarPlanta")); // NOI18N
        botonCerrarAsignarPlanta.setText(resourceMap.getString("botonCerrarAsignarPlanta.text")); // NOI18N
        botonCerrarAsignarPlanta.setName("botonCerrarAsignarPlanta"); // NOI18N

        javax.swing.GroupLayout asignarPlantaLayout = new javax.swing.GroupLayout(asignarPlanta.getContentPane());
        asignarPlanta.getContentPane().setLayout(asignarPlantaLayout);
        asignarPlantaLayout.setHorizontalGroup(
            asignarPlantaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(asignarPlantaLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(asignarPlantaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(asignarPlantaLayout.createSequentialGroup()
                        .addComponent(jLabel80, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboPlantasAsignar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, asignarPlantaLayout.createSequentialGroup()
                        .addComponent(botonAsignarPlantaOk, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(botonCerrarAsignarPlanta, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        asignarPlantaLayout.setVerticalGroup(
            asignarPlantaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(asignarPlantaLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(asignarPlantaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel80)
                    .addComponent(comboPlantasAsignar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(asignarPlantaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botonAsignarPlantaOk)
                    .addComponent(botonCerrarAsignarPlanta))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        jLabel79.setText(resourceMap.getString("jLabel79.text")); // NOI18N
        jLabel79.setName("jLabel79"); // NOI18N

        errorDetallado.setModal(true);
        errorDetallado.setName("errorDetallado"); // NOI18N
        errorDetallado.setResizable(false);

        scrollPane1.setName("scrollPane1"); // NOI18N

        jScrollPane11.setName("jScrollPane11"); // NOI18N

        areaErrorDetallado.setColumns(20);
        areaErrorDetallado.setLineWrap(true);
        areaErrorDetallado.setRows(5);
        areaErrorDetallado.setName("areaErrorDetallado"); // NOI18N
        jScrollPane11.setViewportView(areaErrorDetallado);

        scrollPane1.add(jScrollPane11);

        jLabel83.setText(resourceMap.getString("jLabel83.text")); // NOI18N
        jLabel83.setName("jLabel83"); // NOI18N

        jButton1.setAction(actionMap.get("cerrarErrorDetallado")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        javax.swing.GroupLayout errorDetalladoLayout = new javax.swing.GroupLayout(errorDetallado.getContentPane());
        errorDetallado.getContentPane().setLayout(errorDetalladoLayout);
        errorDetalladoLayout.setHorizontalGroup(
            errorDetalladoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(errorDetalladoLayout.createSequentialGroup()
                .addGroup(errorDetalladoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(errorDetalladoLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(errorDetalladoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel83, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(scrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(errorDetalladoLayout.createSequentialGroup()
                        .addGap(148, 148, 148)
                        .addComponent(jButton1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        errorDetalladoLayout.setVerticalGroup(
            errorDetalladoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(errorDetalladoLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel83)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        nuevaContrasena.setTitle(resourceMap.getString("nuevaContrasena.title")); // NOI18N
        nuevaContrasena.setModal(true);
        nuevaContrasena.setName("nuevaContrasena"); // NOI18N
        nuevaContrasena.setResizable(false);

        jLabel90.setText(resourceMap.getString("jLabel90.text")); // NOI18N
        jLabel90.setName("jLabel90"); // NOI18N

        nuevaContrasenaText.setText(resourceMap.getString("nuevaContrasenaText.text")); // NOI18N
        nuevaContrasenaText.setName("nuevaContrasenaText"); // NOI18N

        okNuevaContrasena.setAction(actionMap.get("okNuevaContrasena")); // NOI18N
        okNuevaContrasena.setText(resourceMap.getString("okNuevaContrasena.text")); // NOI18N
        okNuevaContrasena.setName("okNuevaContrasena"); // NOI18N

        cancelarNuevaContrasena.setAction(actionMap.get("cerrarNuevaContrasena")); // NOI18N
        cancelarNuevaContrasena.setText(resourceMap.getString("cancelarNuevaContrasena.text")); // NOI18N
        cancelarNuevaContrasena.setName("cancelarNuevaContrasena"); // NOI18N

        javax.swing.GroupLayout nuevaContrasenaLayout = new javax.swing.GroupLayout(nuevaContrasena.getContentPane());
        nuevaContrasena.getContentPane().setLayout(nuevaContrasenaLayout);
        nuevaContrasenaLayout.setHorizontalGroup(
            nuevaContrasenaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nuevaContrasenaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(nuevaContrasenaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(okNuevaContrasena, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel90, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addGroup(nuevaContrasenaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(nuevaContrasenaLayout.createSequentialGroup()
                        .addComponent(nuevaContrasenaText, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                        .addContainerGap(20, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(nuevaContrasenaLayout.createSequentialGroup()
                        .addComponent(cancelarNuevaContrasena)
                        .addContainerGap())))
        );
        nuevaContrasenaLayout.setVerticalGroup(
            nuevaContrasenaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nuevaContrasenaLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(nuevaContrasenaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel90)
                    .addComponent(nuevaContrasenaText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(nuevaContrasenaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okNuevaContrasena)
                    .addComponent(cancelarNuevaContrasena))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        jFileChooser2.setName("jFileChooser2"); // NOI18N

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
        addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                propCh(evt);
            }
        });
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField2FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField2FocusGained
        // TODO add your handling code here:
        jTextField2.selectAll();
    }//GEN-LAST:event_jTextField2FocusGained

    private void jTextField1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField1FocusGained
        // TODO add your handling code here:
        jTextField1.selectAll();
    }//GEN-LAST:event_jTextField1FocusGained

    private void mainPanelAncestorRemoved(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_mainPanelAncestorRemoved
        // TODO add your handling code here:

    }//GEN-LAST:event_mainPanelAncestorRemoved

    private void propCh(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_propCh
        // TODO add your handling code here:\
        System.out.println( evt.getNewValue() );
    }//GEN-LAST:event_propCh

    private void mainPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainPanelMouseClicked
        // TODO add your handling code here:

    }//GEN-LAST:event_mainPanelMouseClicked

    private void fileMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileMenuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fileMenuActionPerformed

    private void listUsuariosValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listUsuariosValueChanged
        // TODO add your handling code here:
        this.checkAdminUsuariosPanel();
    }//GEN-LAST:event_listUsuariosValueChanged

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void listPlantasUsuarioValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listPlantasUsuarioValueChanged
        // TODO add your handling code here:
        this.checkListaPlantas();
    }//GEN-LAST:event_listPlantasUsuarioValueChanged

    private void comboDivsMarcacionPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_comboDivsMarcacionPropertyChange
        // TODO add your handling code here:
        DefaultComboBoxModel subsModel = new DefaultComboBoxModel();
        subsModel.addElement(new StringItem("Cualquiera", null));
        JComboBox comboDivs = comboDivsMarcacion;
        JComboBox comboSubs = comboSubsMarcacion;
        try
        {
            StringItem div = (StringItem) 
                    divisionesModel.getElementAt(comboDivs.getSelectedIndex());

            if ( div.getValue() != null )
            {
                List<StringItem> subs = tm.getSubdivisiones(div);
                for ( StringItem sub : subs )
                    subsModel.addElement(sub);
            }
        }
        catch ( Exception ex)
        {
            
        }
        comboSubs.setModel(subsModel);
    }//GEN-LAST:event_comboDivsMarcacionPropertyChange

    private void comboDivsPaquetePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_comboDivsPaquetePropertyChange
        // TODO add your handling code here:
        DefaultComboBoxModel subsModel = new DefaultComboBoxModel();
        subsModel.addElement(new StringItem("Cualquiera", null));
        JComboBox comboDivs = comboDivsPaquete;
        JComboBox comboSubs = comboSubsPaquete;
        try
        {
            StringItem div = (StringItem) 
                    divisionesModel.getElementAt(comboDivs.getSelectedIndex());

            if ( div.getValue() != null )
            {
                List<StringItem> subs = tm.getSubdivisiones(div);
                for ( StringItem sub : subs )
                    subsModel.addElement(sub);
            }
        }
        catch ( Exception ex)
        {
            
        }
        comboSubs.setModel(subsModel);
    }//GEN-LAST:event_comboDivsPaquetePropertyChange

    private void comboDivsNovedadPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_comboDivsNovedadPropertyChange
        // TODO add your handling code here:
        DefaultComboBoxModel subsModel = new DefaultComboBoxModel();
        subsModel.addElement(new StringItem("Cualquiera", null));
        JComboBox comboDivs = comboDivsNovedad;
        JComboBox comboSubs = comboSubsNovedad;
        try
        {
            StringItem div = (StringItem) 
                    divisionesModel.getElementAt(comboDivs.getSelectedIndex());

            if ( div.getValue() != null )
            {
                List<StringItem> subs = tm.getSubdivisiones(div);
                for ( StringItem sub : subs )
                    subsModel.addElement(sub);
            }
        }
        catch ( Exception ex)
        {
            
        }
        comboSubs.setModel(subsModel);
        
    }//GEN-LAST:event_comboDivsNovedadPropertyChange

    public void exit() {

        try
        {
            //tm.kill();
        }
        catch ( Exception ex )
        {

        }

    }

        @Action(block = Task.BlockingScope.COMPONENT)
    public Task synchMaestro() {
        return new SynchMaestroTask(getApplication());
    }


    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        exit();
    }

    public void windowClosed(WindowEvent e) {

    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {

    }



    private class SynchMaestroTask extends org.jdesktop.application.Task<Object, Void> {
        
        List<Maestro> maestros ;
        
        SynchMaestroTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to SynchMaestroTask fields, here.
            super(app);
            
            tm.showMessage("Cargando los registros maestros...");
            showPanel(listaMaestrosPanel);
            showFooterPanel(footerMaestros);
            desactivarAcciones();
            
            
            MaestroTableModel maestroTableModel = new MaestroTableModel( new ArrayList<Maestro>() );
            tablaMaestros.setModel( maestroTableModel );
            tablaMaestros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        }
        
        @Override protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            try
            {
                maestros = tm.loadMaestrosFromSap();
                List<String> maestrosEnSibo = tm.listPernrs();
                for ( Maestro maestro : maestros )
                {
                    if ( maestrosEnSibo.contains(maestro.getPernr()))
                        maestro.setEstado(Constantes.EXITO);
                    else
                        maestro.setEstado(Constantes.ERROR);
                }
                Collections.sort(maestros,new ComparadorMaestro());
                Collections.reverse(maestros);
                
                
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
            }

            return null;  // return your result
        }
        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            statusMessageLabel.setText( "Se encontraron " + maestros.size() + " registro(s) en el servidor SAP");

            maestrosEnSap = maestros;
            MaestroTableModel maestroTableModel = new MaestroTableModel( maestrosEnSap );
            tablaMaestros.setModel( maestroTableModel );            
            tablaMaestros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            activarAcciones();
        }
    }

    @Action
    public void loadMarcs() {
        //tm.showMessage("Cargar Marcaciones de SIBO a SAP");
        jCheckBox1.setSelected(false);
        jTextField5.setEnabled(false);
        jButton16.setEnabled(false);
        jCheckBox2.setSelected(true);
        this.okRangeMarcaciones();
        showPanel( jPanel3 );
        showFooterPanel( jPanel14 );

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel adminUsuariosPanel;
    private javax.swing.JTextArea areaErrorDetallado;
    private javax.swing.JButton asignarContrasena;
    private javax.swing.JDialog asignarPlanta;
    private javax.swing.JButton avPagHistorial;
    private javax.swing.JButton avPagMarcaciones;
    private javax.swing.JButton avPagNovedades;
    private javax.swing.JButton avPagPaquete;
    private javax.swing.ButtonGroup bgTipoUsuario;
    private javax.swing.JButton botonActualizar;
    private javax.swing.JButton botonActualizarNovedades;
    private javax.swing.JButton botonActualizarPaquete;
    private javax.swing.JButton botonAgregarPlanta;
    private javax.swing.JButton botonAsignarPlantaOk;
    private javax.swing.JButton botonCerrarAsignarPlanta;
    private javax.swing.JButton botonDetener;
    private javax.swing.JButton botonDetenerNovedades;
    private javax.swing.JButton botonDetenerPaquete;
    private javax.swing.JButton botonEliminarPlanta;
    private javax.swing.JButton botonExportarExcel;
    private javax.swing.JButton botonIrapagCancelar;
    private javax.swing.JButton botonIrapagOK;
    private javax.swing.JButton botonRegresarFormulario;
    private javax.swing.JButton botonRegresarNovedades;
    private javax.swing.JButton botonRegresarPaquete;
    private javax.swing.JButton botonSubirNovedadesSeleccionadas;
    private javax.swing.JButton botonSubirPaqueteSeleccionados;
    private javax.swing.JButton botonSubirSeleccionados;
    private javax.swing.JButton botonSubirTodasNovedades;
    private javax.swing.JButton botonSubirTodasPaquete;
    private javax.swing.JButton botonSubirTodos;
    private javax.swing.JButton buttonAdminUsuarios;
    private javax.swing.JButton buttonCambiarPassword;
    private javax.swing.JButton buttonCorreo;
    private javax.swing.JButton buttonEditarPerfil;
    private javax.swing.JButton buttonEditarUsuario;
    private javax.swing.JButton buttonEliminarUsuario;
    private javax.swing.JButton buttonGenerarArchivo;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton buttonHistorial;
    private javax.swing.JButton buttonNuevoUsuario;
    private javax.swing.JButton buttonSincronizarCentrosCosto;
    private javax.swing.JButton buttonSincronizarMaestros;
    private javax.swing.JPanel cambiarPasswordPanel;
    private javax.swing.JButton cancelarNuevaContrasena;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JComboBox comboDivsMarcacion;
    private javax.swing.JComboBox comboDivsNovedad;
    private javax.swing.JComboBox comboDivsPaquete;
    private javax.swing.JComboBox comboEstadoIntento;
    private javax.swing.JComboBox comboEstadoNovedades;
    private javax.swing.JComboBox comboEstadoPaquete;
    private javax.swing.JComboBox comboOrdenIntento;
    private javax.swing.JComboBox comboPlantasAsignar;
    private javax.swing.JComboBox comboSubsMarcacion;
    private javax.swing.JComboBox comboSubsNovedad;
    private javax.swing.JComboBox comboSubsPaquete;
    private javax.swing.JComboBox comboTarea;
    private javax.swing.JComboBox comboUsuario;
    private javax.swing.JPanel contenedor;
    private javax.swing.JLabel contrasenaErrorMessage;
    private javax.swing.JLabel contrasenaErrorMessage2;
    private javax.swing.JPanel correoPanel;
    private javax.swing.JTable correosTable;
    private javax.swing.JButton descargarMaestro;
    private javax.swing.JComboBox descripcionesMarcacion;
    private javax.swing.JComboBox descripcionesNovedad;
    private javax.swing.JComboBox descripcionesPaquete;
    private javax.swing.JTextField desdeSapNovedades;
    private javax.swing.JTextField desdeSapPaquete;
    private javax.swing.JTextField desdeSiboMarcs;
    private javax.swing.JTextField desdeSiboNovedades;
    private javax.swing.JTextField desdeSiboPaquete;
    private javax.swing.JLabel editarPerfilErrorMessage;
    private javax.swing.JPanel editarPerfilPanel;
    private javax.swing.JPanel editarUsuarioPanel;
    private javax.swing.JButton eliminarCorreo;
    private javax.swing.JOptionPane eliminarUsuarioPane;
    private javax.swing.JDialog errorDetallado;
    private javax.swing.JPanel footerHistorial;
    private javax.swing.JPanel footerMaestros;
    private javax.swing.JPanel footerMarcaciones;
    private javax.swing.JPanel footerNovedades;
    private javax.swing.JPanel footerNovedadesPaquete;
    private javax.swing.JPanel footerPanel;
    private javax.swing.JPanel footerVerConexiones;
    private javax.swing.JTextField hastaSapNovedades;
    private javax.swing.JTextField hastaSapPaquete;
    private javax.swing.JTextField hastaSiboNovedades;
    private javax.swing.JTextField hastaSiboPaquete;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JPanel historialForm;
    private javax.swing.JDialog irapag;
    private javax.swing.JButton irapagHistorial;
    private javax.swing.JButton irapagMarcaciones;
    private javax.swing.JButton irapagNovedades;
    private javax.swing.JButton irapagPaquete;
    private javax.swing.JTextField irapagTextField;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton27;
    private javax.swing.JButton jButton28;
    private javax.swing.JButton jButton29;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton30;
    private javax.swing.JButton jButton36;
    private javax.swing.JButton jButton37;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton40;
    private javax.swing.JButton jButton41;
    private javax.swing.JButton jButton46;
    private javax.swing.JButton jButton47;
    private javax.swing.JButton jButton49;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton50;
    private javax.swing.JButton jButton51;
    private javax.swing.JButton jButton52;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox10;
    private javax.swing.JCheckBox jCheckBox11;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JCheckBox jCheckBox7;
    private javax.swing.JCheckBox jCheckBox8;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JComboBox jComboBox5;
    private javax.swing.JComboBox jComboBox7;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JDialog jDialog2;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JFileChooser jFileChooser2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JLabel jLabel79;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel80;
    private javax.swing.JLabel jLabel81;
    private javax.swing.JLabel jLabel82;
    private javax.swing.JLabel jLabel83;
    private javax.swing.JLabel jLabel84;
    private javax.swing.JLabel jLabel85;
    private javax.swing.JLabel jLabel86;
    private javax.swing.JLabel jLabel87;
    private javax.swing.JLabel jLabel88;
    private javax.swing.JLabel jLabel89;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel90;
    private javax.swing.JLabel jLabel91;
    private javax.swing.JLabel jLabel92;
    private javax.swing.JLabel jLabel93;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JOptionPane jOptionPane1;
    private javax.swing.JOptionPane jOptionPane2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton10;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JRadioButton jRadioButton6;
    private javax.swing.JRadioButton jRadioButton7;
    private javax.swing.JRadioButton jRadioButton8;
    private javax.swing.JRadioButton jRadioButton9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField13;
    private javax.swing.JTextField jTextField14;
    private javax.swing.JTextField jTextField16;
    private javax.swing.JTextField jTextField17;
    private javax.swing.JTextField jTextField18;
    private javax.swing.JTextField jTextField19;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField20;
    private javax.swing.JTextField jTextField21;
    private javax.swing.JTextField jTextField22;
    private javax.swing.JTextField jTextField23;
    private javax.swing.JTextField jTextField24;
    private javax.swing.JTextField jTextField25;
    private javax.swing.JTextField jTextField26;
    private javax.swing.JTextField jTextField27;
    private javax.swing.JTextField jTextField28;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField31;
    private javax.swing.JTextField jTextField32;
    private javax.swing.JTextField jTextField37;
    private javax.swing.JTextField jTextField38;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField41;
    private javax.swing.JTextField jTextField42;
    private javax.swing.JTextField jTextField45;
    private javax.swing.JTextField jTextField46;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JLabel labelNombreUsuarioEditar;
    private javax.swing.JLabel labelPlantasTodas;
    private javax.swing.JLabel labelRolUsuarioEditar;
    private javax.swing.JList listPlantasUsuario;
    private javax.swing.JList listUsuarios;
    private javax.swing.JPanel listaMaestrosPanel;
    private javax.swing.JScrollPane listasPlantasScrollPanel;
    private javax.swing.JPanel logMarcsForm;
    private javax.swing.JPanel logNovedadesForm;
    private javax.swing.JPanel loginPanel;
    private javax.swing.JPanel mainMenu;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel marcacionesMenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JTextField nombreUsuario;
    private javax.swing.JPanel novedadesPaqueteForm;
    private javax.swing.JDialog nuevaContrasena;
    private javax.swing.JPasswordField nuevaContrasena1;
    private javax.swing.JPasswordField nuevaContrasena2;
    private javax.swing.JTextField nuevaContrasenaText;
    private javax.swing.JDialog nuevoCorreoDialog;
    private javax.swing.JTextField nuevoCorreoText;
    private javax.swing.JLabel nuevoErrorMessage;
    private javax.swing.JTextField nuevoUsuarioLogin;
    private javax.swing.JPanel nuevoUsuarioPanel;
    private javax.swing.JButton okNuevaContrasena;
    private javax.swing.JPanel panelHistorial;
    private javax.swing.JPanel panelLogMarcaciones;
    private javax.swing.JPanel panelLogNovedades;
    private javax.swing.JPanel panelLogNovedadesPaquete;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JRadioButton radioAdmin;
    private javax.swing.JRadioButton radioUser;
    private javax.swing.JButton rePagHistorial;
    private javax.swing.JButton rePagMarcaciones;
    private javax.swing.JButton rePagNovedades;
    private javax.swing.JButton rePagPaquete;
    private javax.swing.JButton regresarEditarUsuario;
    private javax.swing.JButton regresarHistorial;
    private javax.swing.JButton regresarMaestros;
    private java.awt.ScrollPane scrollPane1;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JComboBox subdivisionesCombo;
    private javax.swing.JTable tablaHistorial;
    private javax.swing.JTable tablaMaestros;
    private javax.swing.JTable tablaMarcaciones;
    private javax.swing.JTable tablaNovedades;
    private javax.swing.JTable tablaNovedadesPaquete;
    private javax.swing.JTextField usuarioField;
    private javax.swing.JLabel versionLabel;
    private javax.swing.JPasswordField viejaContrasena;
    private javax.swing.JLabel welcomeLabel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;


    public void showPanel( JPanel p ) {
        //this.getComponent().setVisible(false);
        //p.setVisible(true);
        //setComponent( p );

        CardLayout cl = (CardLayout) centerPanel.getLayout();
        cl.show(centerPanel, p.getName() );
        

        //for ( Component c : jPanel12.getComponents() )
        //    c.setVisible(false);
        //p.setVisible(true);


    }

    public void showFooterPanel(JPanel p) {
        CardLayout cl = (CardLayout) footerPanel.getLayout();
        cl.show( footerPanel, p.getName() );
    }


    @Action
    public void showMainPanel() {
        showPanel(mainMenu);
        showFooterPanel( jPanel13 );
    }

    @Action
    public void encriptar() {

        this.showPanel(jPanel3);
    }

    @Action
    public void okEncriptar() {

    }

        @Action(block = Task.BlockingScope.COMPONENT)
    public Task conectarASap() {
        return new ConectarASapTask(getApplication());
    }

    private class ConectarASapTask extends org.jdesktop.application.Task<Object, Void> {
        ConectarASapTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to ConectarASapTask fields, here.
            super(app);

            statusMessageLabel.setText( "Conectando a SAP ... ");

        }
        @Override protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            try
            {
                //connect();
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                statusMessageLabel.setText( ex.getMessage() ) ;
            }
            return null;  // return your result
        }
        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }

    @Action
    public void pantVerConexiones() {
        showPanel( jPanel15 );
        showFooterPanel( this.footerVerConexiones );
        try
        {
            DefaultStyledDocument doc = new DefaultStyledDocument();
            doc.insertString(0, getConexionesString(), null );
            //jLabel18.setText( getConexionesString() );
            jTextPane1.setDocument(doc);
        }
        catch ( Exception ex )
        {

        }
    }

public String getConexionesString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("Time HR - Version 7");
    sb.append("\n");
    sb.append("Fecha de lanzamiento: Nov 26, 2010");
    sb.append("\n");

    if (this.conexionSAP) {
      sb.append("SAP OK");
    }
    else {
      sb.append("ERROR: Sin conexión a SAP. " + this.mensajeErrorSAP);
    }
    sb.append("\n");


    if (this.props != null) {
      sb.append("SIBO: " + this.props.getProperty("url"));
      sb.append("\n");
      sb.append("ASHOST: " + this.props.getProperty("jco.client.ashost"));
      sb.append("\n");
      sb.append("CLIENT: " + this.props.getProperty("jco.client.client"));
      sb.append("\n");
      sb.append("SYSNR: " + this.props.getProperty("jco.client.sysnr"));
      sb.append("\n");
      sb.append("Usario base de datos: " + this.props.getProperty("user"));
      sb.append("\n");
      
    }
    else {
      sb.append("Error al iniciar el controlador");
      sb.append("\n");
    }
    
    if (this.bdOK) {
      sb.append("Base de datos OK");
    }
    else {
      sb.append("ERROR: Sin conexión a la base de datos. " + bdErrorMessage);
    }
    sb.append("\n");
    

    return sb.toString();
  }


    @Action
    public void myQuit() {
        exit();
        HRTimesoftApp.getApplication().exit();
    }

    @Action
    public void generateProps() {
        showPanel( jPanel4 );
        showFooterPanel( jPanel14 );
    }

    @Action
    public void okGenerarArchivo() {
        try
        {
            Properties props2 = model.getProps();
            //TimesoftManager.encriptarArchivo(props2, jTextField3.getText() + File.separator + "timesoft.properties" );
            Cryptowerk.cryptFile(props2, jTextField3.getText() + File.separator + "timesoft.properties" ,Constantes.KEY );

          
            statusMessageLabel.setText("Archivo creado");
            showMainPanel();
            
        }
        catch ( Exception ex )
        {
            statusMessageLabel.setText( ex.getMessage() );
        }
    }


    @Action
    public void setFileName() {
        jFileChooser1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = jFileChooser1.showOpenDialog( getComponent() );
        if ( returnVal == JFileChooser.APPROVE_OPTION)
        {
           String folder = jFileChooser1.getSelectedFile().getAbsolutePath();
           jTextField3.setText( folder );
        }
    }


    @Action
    public void jCheckbox2Change() {
        if ( jCheckBox1.isSelected() )
        {
            jTextField5.setEnabled(true);
        }
        else
            jTextField5.setEnabled( false );
    }

    @Action
    public void pantNovedades() {
        //tm.showMessage("Cargar Novedades de SIBO a SAP");
        jCheckBox3.setSelected(true);
        jTextField6.setEnabled(true);
        jTextField7.setEnabled(false);
        jCheckBox10.setSelected(false);
        this.okRangoNovedades();
        jButton14.setEnabled(true);
        jButton23.setEnabled(true);
        jButton17.setEnabled(false);
        showPanel( jPanel5 );
        showFooterPanel( jPanel14 );

    }


    @Action
    public void jCheckBox3Change() {
        if ( jCheckBox3.isSelected() )
        {
            jTextField7.setEnabled(true);
        }
        else
            jTextField7.setEnabled( false );

    }


    public void connect() throws Exception {

    }

    @Action
    public Task synchCentroCosto() {
        return new SynchCentroCostoTask(getApplication());
    }

    private class SynchCentroCostoTask extends org.jdesktop.application.Task<Object, Void> {
        SynchCentroCostoTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to SynchCentroCostoTask fields, here.
            super(app);
            tm.showMessage("Sincronizar Centro Costo desde SAP");
            jButton3.setVisible(false);
            showPanel( jPanel1 );
        }
        @Override protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            try
            {
                tm.syncCentrosCostoFromSAP();
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
            }
            return null;  // return your result
        }
        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            jButton3.setVisible(true);
        }
    }

    @Action
    public void cancelarMarcaciones() {
        tm.showMessage("Cancelando...");
        //tm.setRunning(false);
        //Borrar
    }

    @Action
    public void cancelarNovedades() {
        tm.showMessage( "Cancelando...");
        //tm.setRunning(false);
        //Borrar
    }

    @Action
    public void okGenerarMail() {
        try
        {
            mailProps = mailModel.getProps();
            Cryptowerk.cryptFile(mailProps, jTextField8.getText() + File.separator + "mail.properties", Constantes.KEY  );

            statusMessageLabel.setText("Archivo creado");
            showMainPanel();

        }
        catch ( Exception ex )
        {
            statusMessageLabel.setText( ex.getMessage() );
        }
    }

    @Action
    public void pantConfigMail() {

        try
        {

            showPanel( correoPanel );
            showFooterPanel( jPanel14 );

        }
        catch ( Exception ex )
        {
            statusMessageLabel.setText( ex.getMessage() );
        }
        
    }

    @Action
    public void setMailFilename() {
        jFileChooser1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = jFileChooser1.showOpenDialog( getComponent() );
        if ( returnVal == JFileChooser.APPROVE_OPTION)
        {
           String folder = jFileChooser1.getSelectedFile().getAbsolutePath();
           jTextField8.setText( folder );
        }
    }

    @Action
    public void showCasino() {

        jCheckBox5.setSelected(false);
        jTextField11.setEnabled(false);
        jTextField12.setEnabled(false);
        jButton26.setEnabled(true);
        jButton23.setEnabled(true);
        jButton27.setEnabled(false);
        jCheckBox11.setSelected(false);
        this.okRangoPaquete();
        jRadioButton1.setSelected(true);
        this.selCasino();

        showPanel( jPanel7 );
        showFooterPanel( jPanel14 );
    }

    @Action
    public void showBolsa() {
        jCheckBox7.setSelected(false);
        jTextField14.setEnabled(false);
        jButton29.setEnabled(true);
        jButton23.setEnabled(true);
        jButton30.setEnabled(false);
        showPanel( jPanel8 );

    }


    @Action
    public void jCheckBox5Change() {
        if ( jCheckBox5.isSelected() )
        {
            jTextField12.setEnabled(true);
        }
        else
            jTextField12.setEnabled( false );
    }

    @Action
    public void jCheckBox7Change() {
        if ( jCheckBox7.isSelected() )
        {
            jTextField14.setEnabled(true);
        }
        else
            jTextField14.setEnabled( false );

    }

    @Action
    public void okRangeMarcaciones() {
    }

    @Action
    public void okReprocesoMarcaciones() {
        if ( jCheckBox2.isSelected() )
        {
            jTextField4.setEnabled(true);
            jTextField5.setEnabled(true);
            jCheckBox1.setEnabled(true);
            this.jCheckbox2Change();
        }
        else
        {
            jTextField4.setEnabled(false);
            jTextField5.setEnabled(false);
            jCheckBox1.setEnabled(false);
        }
    }

    @Action
    public void okReprocesoNovedades() {
        if ( jCheckBox3.isSelected() )
        {
            jTextField6.setEnabled(true);
            jTextField7.setEnabled(true);
            jCheckBox4.setEnabled(true);
            this.hastaNovedadChange();
        }
        else
        {
            jTextField6.setEnabled(false);
            jTextField7.setEnabled(false);
            jCheckBox4.setEnabled(false);
        }
    }

    @Action
    public void hastaNovedadChange() {
        if ( jCheckBox4.isSelected() )
        {
            jTextField7.setEnabled(true);
        }
        else
            jTextField7.setEnabled( false );
    }

    @Action
    public void okRangoNovedades() {
        if ( jCheckBox10.isSelected())
        {
            jTextField18.setEnabled(false);
            jTextField19.setEnabled(true);
            jTextField20.setEnabled(true);
        }
        else
        {
            jTextField18.setEnabled(true);
            jTextField19.setEnabled(false);
            jTextField20.setEnabled(false);
        }
    }

    @Action
    public void okReprocesoPaquete() {
        if ( jCheckBox5.isSelected() )
        {
            jTextField11.setEnabled(true);
            jTextField12.setEnabled(true);
            jCheckBox6.setEnabled(true);
            this.okHastaPaquete();
        }
        else
        {
            jTextField11.setEnabled(false);
            jTextField12.setEnabled(false);
            jCheckBox6.setEnabled(false);
        }
    }

    @Action
    public void okHastaPaquete() {
        if ( jCheckBox6.isSelected() )
        {
            jTextField12.setEnabled(true);
        }
        else
            jTextField12.setEnabled( false );
    }

    @Action
    public void okRangoPaquete() {
        if ( jCheckBox11.isSelected())
        {
            jTextField22.setEnabled(false);
            jTextField23.setEnabled(true);
            jTextField24.setEnabled(true);
        }
        else
        {
            jTextField22.setEnabled(true);
            jTextField23.setEnabled(false);
            jTextField24.setEnabled(false);
        }
    }

    @Action
    public void selCasino() {
        jRadioButton2.setSelected( ! jRadioButton1.isSelected() );
    }

    @Action
    public void selBolsaPapel() {
        jRadioButton1.setSelected( ! jRadioButton2.isSelected() );
    }

    @Action
    public void rangoPernr() {
    }

    @Action
    public Task pantLogMarcaciones() {
        paginaMarcaciones = 0;
        return buscarMarcaciones();
    }
    
    private int verificarPagina(int totalRegistros, int pagina) {
        int totalPaginas = getTotalPaginas(totalRegistros);

        if ( pagina >= totalPaginas )
            pagina = totalPaginas - 1;

        if ( pagina < 0 )
            pagina = 0;
        
        return pagina;
    }
    
    private int getTotalPaginas(int totalRegistros) {
        return totalRegistros / PAGE_SIZE + 1;
    }
    
    private String getNavigationMessage(int pagina, int totalRegistros, int regEnPagina) {
        String msg = "Mostrando página " 
                 + (pagina+1)
                 + " de " + getTotalPaginas(totalRegistros) 
                 + ", con " + regEnPagina + " de " + totalRegistros + " registro(s) encontrados.";    
        return msg;
    }

    private class PantLogMarcacionesTask extends org.jdesktop.application.Task<Object, Void> {
        
        Criterios findMarcaciones;
        int totalRegistros;
        int pagina;
        
        PantLogMarcacionesTask(org.jdesktop.application.Application app, Criterios criterios, int pagina) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to PantLogMarcacionesTask fields, here.
            super(app);
            this.pagina = pagina;
      
            
            try
            {
                findMarcaciones = criterios;
                        
                showPanel( panelLogMarcaciones );
                showFooterPanel( footerMarcaciones );

                MarcacionesTableModel marcTable = new MarcacionesTableModel( new ArrayList<Registro>() );
                tablaMarcaciones.setModel( marcTable );
                tablaMarcaciones.setDefaultRenderer(Icon.class,new MarcacionesTableRenderer());
                TableColumnModel columns = tablaMarcaciones.getColumnModel();
                columns.getColumn(0).setPreferredWidth(50);
                columns.getColumn(1).setPreferredWidth(55);
                columns.getColumn(2).setPreferredWidth(50);
                columns.getColumn(3).setPreferredWidth(25);
                columns.getColumn(4).setPreferredWidth(115);
                columns.getColumn(5).setPreferredWidth(115);
                columns.getColumn(6).setPreferredWidth(10);
                columns.getColumn(7).setPreferredWidth(50);
                columns.getColumn(8).setPreferredWidth(50);
                columns.getColumn(9).setPreferredWidth(400);
                
                statusMessageLabel.setText( "Buscando en la base de datos, espere por favor..." );
                desactivarAcciones();
                botonDetener.setEnabled(false);
            }
            catch ( Exception ex )
            {
                log.error( ex.getMessage() );
                statusMessageLabel.setText( ex.getMessage() );
            }

        }
        @Override protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            
            try
            {
                List<Registro> marcs = null;
                try
                {
                    totalRegistros = tm.count(findMarcaciones, timesoft.Constantes.MARCACIONES);
                    pagina = verificarPagina(totalRegistros,pagina);
                    marcs = tm.getMarcaciones( findMarcaciones, pagina*PAGE_SIZE, PAGE_SIZE );
                    String msg = getNavigationMessage(pagina,totalRegistros,marcs.size());
                    statusMessageLabel.setText( msg );
                }
                catch ( Exception ex )
                {
                    log.error( ex.getMessage() );
                    marcs = new ArrayList<Registro>();
                }
                
                MarcacionesTableModel marcTable = new MarcacionesTableModel( marcs );
                tablaMarcaciones.setModel( marcTable );
                tablaMarcaciones.setDefaultRenderer(Icon.class,new MarcacionesTableRenderer());
                TableColumnModel columns = tablaMarcaciones.getColumnModel();
                columns.getColumn(0).setPreferredWidth(50);
                columns.getColumn(1).setPreferredWidth(55);
                columns.getColumn(2).setPreferredWidth(50);
                columns.getColumn(3).setPreferredWidth(25);
                columns.getColumn(4).setPreferredWidth(115);
                columns.getColumn(5).setPreferredWidth(115);
                columns.getColumn(6).setPreferredWidth(10);
                columns.getColumn(7).setPreferredWidth(50);
                columns.getColumn(8).setPreferredWidth(50);
                columns.getColumn(9).setPreferredWidth(400);

                activarAcciones();
                
            }
            catch ( Exception ex )
            {
                log.error( ex.getMessage() );
                statusMessageLabel.setText( ex.getMessage() );
            }
            
            return null;  // return your result
        }
        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            tablaMarcaciones.repaint();
            paginaMarcaciones = pagina;

        }
    }

    @Action
    public void paintLogMarcsForm() {
        desdeSiboMarcs.requestFocus();

        jRadioButton3.setSelected(true);
        jRadioButton4.setSelected(false);

        statusMessageLabel.setText( "" );

        showPanel( logMarcsForm );
        showFooterPanel( jPanel14 );
    }


    @Action
    public void descSelected() {
        jRadioButton4.setSelected(false);
    }

    @Action
    public void ascSelected() {
        jRadioButton3.setSelected(false);
    }

    @Action
    public void estadoMarcsSelected() {

        int index = jComboBox2.getSelectedIndex();
        boolean viejasMarcaciones = index != 2  ;
        jTextField27.setEnabled( viejasMarcaciones );
        jTextField28.setEnabled( viejasMarcaciones  );
        
    }
    
    @Action
    public void estadoNovedadesSelected() {
        int index = this.comboEstadoNovedades.getSelectedIndex();
        boolean viejasMarcaciones = index != 2  ;
        desdeSapNovedades.setEnabled( viejasMarcaciones );
        hastaSapNovedades.setEnabled( viejasMarcaciones  );
    }
    
    @Action
    public void estadoPaqueteSelected() {
        int index = this.comboEstadoPaquete.getSelectedIndex();
        boolean viejasMarcaciones = index != 2  ;
        desdeSapPaquete.setEnabled( viejasMarcaciones );
        hastaSapPaquete.setEnabled( viejasMarcaciones  );
    }
    
    
    @Action
    public void okEliminarCorreo() {
        int row = correosTable.getSelectedRow();
        if ( row < 0 )
        {
            JOptionPane.showMessageDialog(
            correoPanel,
            "Ningún correo seleccionado",
            "Familia",
            JOptionPane.WARNING_MESSAGE );
                }
        else
        {
           int answer = JOptionPane.showConfirmDialog(
            correoPanel,
            "¿Desea eliminar el correo seleccionado?",
            "Familia",
            JOptionPane.YES_NO_OPTION);

           if ( answer == 0 )
           {
               try
               {
                    CorreosTableModel correosModel = (CorreosTableModel) correosTable.getModel();
                    SubdivisionCorreo sc = correosModel.get(row);
                    tm.deleteSubdivisionCorreo( sc.pk.getSubdivision(), sc.pk.getCorreo() );
                    correosModel.delete(row);
                    //correosModel.removeRow(row);
                    //correosTable.repaint();
               }
               catch ( Exception ex )
               {
                   ex.printStackTrace();
                    JOptionPane.showMessageDialog(
                    correoPanel,
                    "Error al remover el registro: " + ex.getMessage(),
                    "Familia",
                    JOptionPane.ERROR_MESSAGE );
               }
           }

        }

    }

    @Action
    public void okNuevoCorreo() {
        nuevoCorreoDialog.setSize(400,300);
        nuevoCorreoDialog.setLocationRelativeTo( correoPanel );
        nuevoCorreoDialog.setVisible(true);
        nuevoCorreoText.requestFocus();
    }

    @Action
    public void cancelNuevoCorreo() {
        nuevoCorreoDialog.setVisible( false );
    }

    @Action
    public void saveNuevoCorreo() {
        
        String nuevoCorreo = nuevoCorreoText.getText();
        int subIndex = subdivisionesCombo.getSelectedIndex();

        SubdivisionCorreo nc = new SubdivisionCorreo();
        nc.pk.setCorreo(nuevoCorreo);
        nc.pk.setSubdivision( ((StringItem)subdivisiones.getElementAt(subIndex)).getValue());

        try
        {
            CorreosTableModel correosModel = (CorreosTableModel)correosTable.getModel();
            correosModel.add(nc);
        }
        catch ( Exception ex )
        {
            //ex.printStackTrace();
            JOptionPane.showMessageDialog(
            correoPanel,
            "Error en la creación del registro: " + ex.getMessage(),
            "Familia",
            JOptionPane.ERROR_MESSAGE );

        }

        nuevoCorreoDialog.setVisible(false);


    }

    @Action
    public void paintMarcacionesMenu() {
        showPanel( marcacionesMenu );
        showFooterPanel( jPanel14 );    }

    @Action
    public void paintNovedadesForm() {

        desdeSiboNovedades.requestFocus();

        jRadioButton5.setSelected(true);
        jRadioButton6.setSelected(false);

        statusMessageLabel.setText( "" );

        showPanel( logNovedadesForm );
        showFooterPanel( jPanel14 );
    }

    @Action
    public Task paintNovedades() throws ParseException {
        paginaNovedades = 0;
        return buscarNovedades();
    }
    
    @Action
    private Task buscarNovedades() {
        try
        {
            Criterios criterios = this.obtenerCriteriosNovedades();
            return new PaintNovedadesTask(getApplication(), criterios, paginaNovedades);
        }
        catch ( Exception ex )
        {
            log.error("Error buscando novedades.", ex );
            statusMessageLabel.setText(ex.getMessage());
            return null;
        }  
    }

    private class PaintNovedadesTask extends org.jdesktop.application.Task<Object, Void> {
        
        List<Registro> regs;
        Criterios criterios;
        int totalRegistros = 0;
        int pagina = 0;
        
        PaintNovedadesTask(org.jdesktop.application.Application app, Criterios criterios, int pagina) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to PaintNovedadesTask fields, here.
            super(app);
            
            this.pagina = pagina;
            this.criterios = criterios ;
            this.regs = new ArrayList<Registro>();

            NovedadesTableModel novedadesModel = new NovedadesTableModel( regs );
            tablaNovedades.setModel( novedadesModel );
            TableColumnModel columns = tablaNovedades.getColumnModel();
            columns.getColumn(0).setPreferredWidth(50);
            columns.getColumn(1).setPreferredWidth(55);
            columns.getColumn(2).setPreferredWidth(50);
            columns.getColumn(3).setPreferredWidth(25);
            columns.getColumn(4).setPreferredWidth(70);
            columns.getColumn(5).setPreferredWidth(70);
            columns.getColumn(6).setPreferredWidth(50);
            columns.getColumn(7).setPreferredWidth(50);
            columns.getColumn(10).setPreferredWidth(115);
            columns.getColumn(11).setPreferredWidth(40);
            columns.getColumn(12).setPreferredWidth(50);
            columns.getColumn(13).setPreferredWidth(50);
            columns.getColumn(14).setPreferredWidth(300);


            showPanel( panelLogNovedades );
            showFooterPanel( footerNovedades );
            statusMessageLabel.setText( "Buscando en la base de datos, espere por favor..." );
            desactivarAcciones();
            botonDetenerNovedades.setEnabled(false);
            
        }
        
        @Override protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.

            try
            {
                totalRegistros = tm.count(criterios, timesoft.Constantes.NOVEDADES );
                pagina = verificarPagina(totalRegistros,pagina);
                regs = tm.getNovedades( this.criterios, pagina*PAGE_SIZE , PAGE_SIZE );
                return "";
            }
            catch ( Exception ex )
            {
                log.error( ex.getMessage() );
                return ex.getMessage();
            }
            
        }
        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            String mensaje = (String) result;
            paginaNovedades = pagina;
            if ( mensaje.isEmpty() )
            {

                String msg = getNavigationMessage(pagina,totalRegistros,regs.size());
                    
                statusMessageLabel.setText( msg );
                
                NovedadesTableModel novedadesModel = new NovedadesTableModel( regs );
                tablaNovedades.setModel( novedadesModel );
                TableColumnModel columns = tablaNovedades.getColumnModel();
                columns.getColumn(0).setPreferredWidth(50);
                columns.getColumn(1).setPreferredWidth(55);
                columns.getColumn(2).setPreferredWidth(50);
                columns.getColumn(3).setPreferredWidth(25);
                columns.getColumn(4).setPreferredWidth(70);
                columns.getColumn(5).setPreferredWidth(70);
                columns.getColumn(6).setPreferredWidth(50);
                columns.getColumn(7).setPreferredWidth(50);
                columns.getColumn(10).setPreferredWidth(115);
                columns.getColumn(11).setPreferredWidth(40);
                columns.getColumn(12).setPreferredWidth(50);
                columns.getColumn(13).setPreferredWidth(50);
                columns.getColumn(14).setPreferredWidth(300);
                
                
                
            }
            else
            {
                statusMessageLabel.setText( mensaje );
            }
            
            activarAcciones();
        }
    }
    
    private void activarAcciones() {
        activarAcciones(true);
    }
    
    private void desactivarAcciones() {
        activarAcciones(false);
    }
    
    private void activarAcciones(boolean valor) {
        this.botonActualizar.setEnabled(valor);
        this.botonRegresarFormulario.setEnabled(valor);
        this.botonSubirTodos.setEnabled(valor);
        this.botonSubirSeleccionados.setEnabled(valor);
        this.botonDetener.setEnabled(!valor);
        this.avPagMarcaciones.setEnabled(valor);
        this.rePagMarcaciones.setEnabled(valor);
        
        this.botonSubirNovedadesSeleccionadas.setEnabled(valor);
        this.botonSubirTodasNovedades.setEnabled(valor);
        this.botonActualizarNovedades.setEnabled(valor);
        this.botonRegresarNovedades.setEnabled(valor);
        this.botonDetenerNovedades.setEnabled(!valor);
        this.avPagNovedades.setEnabled(valor);
        this.rePagNovedades.setEnabled(valor);
        
        this.botonSubirPaqueteSeleccionados.setEnabled(valor);
        this.botonSubirTodasPaquete.setEnabled(valor);
        this.botonActualizarPaquete.setEnabled(valor);
        this.botonRegresarPaquete.setEnabled(valor);
        this.botonDetenerNovedades.setEnabled(!valor);
        this.avPagPaquete.setEnabled(valor);
        this.rePagPaquete.setEnabled(valor);
        
        this.avPagHistorial.setEnabled(valor);
        this.rePagHistorial.setEnabled(valor);
        this.regresarHistorial.setEnabled(valor);
        
        this.irapagMarcaciones.setEnabled(valor);
        this.irapagNovedades.setEnabled(valor);
        this.irapagPaquete.setEnabled(valor);
        this.irapagHistorial.setEnabled(valor);
        
        this.regresarMaestros.setEnabled(valor);
        this.descargarMaestro.setEnabled(valor);
        
    }
    

    @Action
    public Task subirMarcacionesSelected() {
        int rows[] = this.tablaMarcaciones.getSelectedRows();
        MarcacionesTableModel tableModel = (MarcacionesTableModel) tablaMarcaciones.getModel();
        return new SubirMarcacionesSelectedTask(getApplication(),rows,tableModel,timesoft.Constantes.MARCACIONES);
    }

    private class SubirMarcacionesSelectedTask extends org.jdesktop.application.Task<Object, Void> {
        
        List<Integer> updatedRows;
        List<Registro> regs;
        int tipo;
        Reporte reporte;
        
        SubirMarcacionesSelectedTask(org.jdesktop.application.Application app, int rows[],DefaultTableModel tableModel,int tipo) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to SubirMarcacionesSelectedTask fields, here.
            super(app);
            
            updatedRows = new ArrayList<Integer>();
            regs = new ArrayList<Registro>();
            this.tipo = tipo;
            
            for ( int row : rows )
            {
                Registro reg = (Registro) tableModel.getDataVector().get(row) ;
                regs.add(reg);
                updatedRows.add( row );
            }
            
            desactivarAcciones();
        }
        
        @Override protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            try
            {
                reporte = tm.subirRegistrosSAP(regs, tipo );
                return "";
            }
            catch ( Exception ex )
            {
                return ex.getMessage();
            }
        }
        
        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            
            String mensaje = "";
            
            if (result != null) {
                mensaje = (String) result;
            }
            else {
                mensaje = "Error al procesar los registros";
            }
            
            if (mensaje.isEmpty() )
            {
                JOptionPane.showMessageDialog(
                centerPanel,
                reporte.toString(),
                "Familia",
                JOptionPane.INFORMATION_MESSAGE );
            }
            else
            {
                JOptionPane.showMessageDialog(
                centerPanel,
                mensaje,
                "Familia",
                JOptionPane.ERROR_MESSAGE );
            }

            activarAcciones();
               
           tablaMarcaciones.repaint();
           tablaNovedades.repaint();
           tablaNovedadesPaquete.repaint();
            
        }
    }
    
    @Action
    public Task subirMarcacionesTodas() throws Exception {
        statusMessageLabel.setText("Procesando todos los registros...");
        desactivarAcciones();
        Criterios criterios = this.obtenerCriteriosMarcaciones();
        int tipo = timesoft.Constantes.MARCACIONES;
        return new SubirMarcacionesTodasTask(getApplication(), criterios, tipo);
    }

    private class SubirMarcacionesTodasTask extends org.jdesktop.application.Task<Object, Void> {
        
        Criterios findMarcaciones;
        int tipo;
        
        SubirMarcacionesTodasTask(org.jdesktop.application.Application app, Criterios criterios, int tipo) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to SubirMarcacionesTodasTask fields, here.
            super(app);
            findMarcaciones = criterios;
            this.tipo = tipo;
        }
        
        
        @Override protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            
            try {
                 tm.procesarEnBufer( findMarcaciones, 
                                     timesoft.Constantes.BATCH_SIZE_SMALL,
                                     tipo, 
                                     tm.startTry(usr.getLogin(), tipo)
                                    );
                 return "";
            }
            catch ( Exception ex ) {
                return ex.getMessage();
            }
            
        }
        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            

            
            String mensaje = "";
            
            if (result != null) {
                mensaje = (String) result;
            }
            else {
                mensaje = "Error al procesar los registros";
            }
            
            if (mensaje.isEmpty() )
            {
                JOptionPane.showMessageDialog(
                centerPanel,
                "Se realizó el envío de los registros a SAP",
                "Familia",
                JOptionPane.INFORMATION_MESSAGE );
            }
            else
            {
                JOptionPane.showMessageDialog(
                centerPanel,
                mensaje,
                "Familia",
                JOptionPane.ERROR_MESSAGE );
            }
            
            
           activarAcciones();
            
           tablaMarcaciones.repaint();
           tablaNovedades.repaint();
           tablaNovedadesPaquete.repaint();
            
        }
    }

    @Action
    public Task subirNovedadesPaqueteSelected() {
        int rows[] = this.tablaNovedadesPaquete.getSelectedRows();
        NovedadesPaqueteTableModel tableModel = (NovedadesPaqueteTableModel) tablaNovedadesPaquete.getModel();
        return new SubirMarcacionesSelectedTask(getApplication(),rows,tableModel,timesoft.Constantes.NOVEDADES_PAQUETE);
    }

    @Action
    public Task subirNovedadesPaqueteTodas() throws Exception {
        statusMessageLabel.setText("Procesando todos los registros...");
        desactivarAcciones();
        Criterios criterios = this.obtenerCriteriosPaquete();
        int tipo = timesoft.Constantes.NOVEDADES_PAQUETE;
        return new SubirMarcacionesTodasTask(getApplication(), criterios, tipo);
    }

    @Action
    public Task subirNovedadesSelected() {
        int rows[] = this.tablaNovedades.getSelectedRows();
        NovedadesTableModel tableModel = (NovedadesTableModel) tablaNovedades.getModel();
        return new SubirMarcacionesSelectedTask(getApplication(),rows,tableModel,timesoft.Constantes.NOVEDADES);
    }


    @Action
    public Task subirNovedadesTodas() throws Exception {
        statusMessageLabel.setText("Procesando todos los registros...");
        desactivarAcciones();
        Criterios criterios = this.obtenerCriteriosNovedades();
        int tipo = timesoft.Constantes.NOVEDADES;
        return new SubirMarcacionesTodasTask(getApplication(), criterios, tipo);
    }


    @Action
    public Task paintNovedadesPaquete() throws ParseException {
        paginaPaquete = 0;
        return buscarPaquete();
    }

    @Action
    private Task buscarPaquete() {
        try
        {
            Criterios criterios = this.obtenerCriteriosPaquete();
            return new PaintNovedadesPaqueteTask(getApplication(),criterios,paginaPaquete);
        }
        catch ( Exception ex )
        {
            statusMessageLabel.setText(ex.getMessage());
            return null;
        }      
    }

    private class PaintNovedadesPaqueteTask extends org.jdesktop.application.Task<Object, Void> {
        
        List<Registro> regs;
        Criterios criterios;
        int totalRegistros = 0;
        int pagina;
        
        PaintNovedadesPaqueteTask(org.jdesktop.application.Application app,Criterios criterios, int pagina) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to PaintNovedadesPaqueteTask fields, here.
            super(app);
            
            this.pagina = pagina;
            this.criterios = criterios ;

            this.regs = new ArrayList<Registro>();
            
            NovedadesPaqueteTableModel novedadesModel = new NovedadesPaqueteTableModel( regs );
            tablaNovedadesPaquete.setModel( novedadesModel );

            TableColumnModel columns = tablaNovedadesPaquete.getColumnModel();
            columns.getColumn(0).setPreferredWidth(50);
            columns.getColumn(1).setPreferredWidth(55);
            columns.getColumn(2).setPreferredWidth(50);
            columns.getColumn(3).setPreferredWidth(25);
            columns.getColumn(4).setPreferredWidth(120);
            columns.getColumn(5).setPreferredWidth(70);
            columns.getColumn(6).setPreferredWidth(70);
            columns.getColumn(7).setPreferredWidth(50);
            columns.getColumn(9).setPreferredWidth(115);
            columns.getColumn(10).setPreferredWidth(115);
            columns.getColumn(11).setPreferredWidth(40);
            columns.getColumn(12).setPreferredWidth(50);
            columns.getColumn(13).setPreferredWidth(50);
            columns.getColumn(14).setPreferredWidth(300);

            showPanel( panelLogNovedadesPaquete );
            showFooterPanel( footerNovedadesPaquete );
            statusMessageLabel.setText( "Buscando en la base de datos, espere por favor..." );
            desactivarAcciones();
            botonDetenerPaquete.setEnabled(false);
            
        }
        
        @Override protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            
            try
            {
                totalRegistros = tm.count(criterios, timesoft.Constantes.NOVEDADES_PAQUETE );
                pagina = verificarPagina(totalRegistros,pagina);
                regs = tm.getNovedadesPaquete( this.criterios, pagina*PAGE_SIZE, PAGE_SIZE );
                return "";
            }
            catch ( Exception ex )
            {
                return ex.getMessage();
            }
        }
        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            String mensaje = (String) result;
            
            paginaPaquete = pagina;
            
            if ( mensaje.isEmpty() )
            {
                
                String msg = getNavigationMessage(pagina,totalRegistros,regs.size());
                    
                statusMessageLabel.setText( msg );
                
                NovedadesPaqueteTableModel novedadesModel = new NovedadesPaqueteTableModel( regs );
                tablaNovedadesPaquete.setModel( novedadesModel );

                TableColumnModel columns = tablaNovedadesPaquete.getColumnModel();
                columns.getColumn(0).setPreferredWidth(50);
                columns.getColumn(1).setPreferredWidth(55);
                columns.getColumn(2).setPreferredWidth(50);
                columns.getColumn(3).setPreferredWidth(25);
                columns.getColumn(4).setPreferredWidth(120);
                columns.getColumn(5).setPreferredWidth(70);
                columns.getColumn(6).setPreferredWidth(70);
                columns.getColumn(7).setPreferredWidth(50);
                columns.getColumn(9).setPreferredWidth(115);
                columns.getColumn(10).setPreferredWidth(115);
                columns.getColumn(11).setPreferredWidth(40);
                columns.getColumn(12).setPreferredWidth(50);
                columns.getColumn(13).setPreferredWidth(50);
                columns.getColumn(14).setPreferredWidth(300);
                
            }
            else
            {
                statusMessageLabel.setText( mensaje );
            }
            
            activarAcciones();
            
        }
    }

    @Action
    public void paintNovedadesPaqueteForm() {
        
        desdeSiboPaquete.requestFocus();

        jRadioButton7.setSelected(true);
        jRadioButton8.setSelected(false);

        statusMessageLabel.setText( "" );

        showPanel( novedadesPaqueteForm );
        showFooterPanel( jPanel14 );

    }
    
    private CriteriosIntento obtenerCriteriosIntento() throws ParseException {
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

            DateRange creado = null;
            Date begda = null;
            if ( ! jTextField41.getText().trim().isEmpty() )
            begda = df.parse( jTextField41.getText() ) ;
            Date endda = null;
            if ( !jTextField42.getText().trim().isEmpty() )
            endda = df.parse( jTextField42.getText() );
            if ( endda != null )
            endda = new Timestamp( endda.getTime() + 1000*60*60*24 - 1);
            creado = new DateRange( begda, endda );

            DateRange modificado = null;
            Date begdaMod = null;
            if ( ! jTextField45.getText().trim().isEmpty() )
            begdaMod = df.parse( jTextField45.getText() ) ;
            Date enddaMod = null;
            if ( !jTextField46.getText().trim().isEmpty() )
            enddaMod = df.parse( jTextField46.getText() );
            if ( enddaMod != null )
            enddaMod = new Timestamp( enddaMod.getTime() + 1000*60*60*24 - 1);
            modificado = new DateRange( begdaMod, enddaMod );


            DefaultComboBoxModel estadoModel = (DefaultComboBoxModel) this.comboEstadoIntento.getModel();
            StringItem estadoItem = (StringItem) estadoModel.getElementAt( comboEstadoIntento.getSelectedIndex() );
            Character estado = null;
            if ( estadoItem.getValue() != null )
            estado = new Character( estadoItem.getValue().charAt(0) );

            DefaultComboBoxModel usuarioModel = (DefaultComboBoxModel) this.comboUsuario.getModel();
            StringItem usuarioItem = (StringItem) usuarioModel.getElementAt( comboUsuario.getSelectedIndex() );
            String usuario = usuarioItem.getValue();

            DefaultComboBoxModel actividadModel = (DefaultComboBoxModel) this.comboTarea.getModel();
            StringItem actividadItem = (StringItem) actividadModel.getElementAt( comboTarea.getSelectedIndex() );
            Integer tipoActividad = null;
            if ( actividadItem.getValue() != null )
            tipoActividad = Integer.parseInt( actividadItem.getValue() );


            DefaultComboBoxModel orderModel = (DefaultComboBoxModel) this.comboOrdenIntento.getModel();
            StringItem order = (StringItem) orderModel.getElementAt( comboOrdenIntento.getSelectedIndex() );
            String orderBy = order.getValue();
            String orderDirection = "desc";
            if ( jRadioButton10.isSelected() )
               orderDirection = "asc";

            CriteriosIntento criteria = new CriteriosIntento();

            criteria.setCreado(creado);
            criteria.setModificado(modificado);
            criteria.setFirma(usuario);
            criteria.setEstado(estado);
            criteria.setTipoActividad(tipoActividad);
            criteria.setOrderBy(orderBy);
            criteria.setOrderDirection(orderDirection);
            
            return criteria;
    }

    @Action
    public Task paintHistorialPanel() {
        paginaHistorial = 0;
        return buscarIntentos();
    }
    
    @Action
    private Task buscarIntentos() {
        try
        {
            CriteriosIntento criterios = this.obtenerCriteriosIntento();
            return new PaintHistorialPanelTask(getApplication(),criterios,paginaHistorial);
        }
        catch ( Exception ex )
        {
            log.error("Error al buscar eventos de historial", ex);
            statusMessageLabel.setText(ex.getMessage());
            return null;
        }        
    }

    private class PaintHistorialPanelTask extends org.jdesktop.application.Task<Object, Void> {
        
        private CriteriosIntento criterios;
        int totalRegistros;
        int pagina;
        List<Intento> regs;
        
        PaintHistorialPanelTask(org.jdesktop.application.Application app, CriteriosIntento criterios, int pagina) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to PaintHistorialPanelTask fields, here.
            super(app);
            
            this.criterios = criterios;
            this.pagina = pagina;
            
            try
            {
                regs = new ArrayList<Intento>();
                HistorialTableModel historialModel = new HistorialTableModel(regs);
                tablaHistorial.setModel( historialModel );

                TableColumnModel columns = tablaHistorial.getColumnModel();
                columns.getColumn(0).setPreferredWidth(50);
                columns.getColumn(1).setPreferredWidth(120);
                columns.getColumn(2).setPreferredWidth(120);
                columns.getColumn(3).setPreferredWidth(50);
                columns.getColumn(4).setPreferredWidth(70);
                columns.getColumn(5).setPreferredWidth(90);
                columns.getColumn(6).setPreferredWidth(50);
                columns.getColumn(7).setPreferredWidth(50);
                columns.getColumn(8).setPreferredWidth(50);
                columns.getColumn(9).setPreferredWidth(250);

                showPanel(panelHistorial);
                showFooterPanel( footerHistorial );

                statusMessageLabel.setText( "Buscando en la base de datos, espere por favor..." );
                desactivarAcciones();

            }
            catch ( Exception ex )
            {
                log.error( ex.getMessage() );
                statusMessageLabel.setText( ex.getMessage() );
            }

        }
        @Override protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            try
            {
                totalRegistros = tm.countIntentos(criterios);
                pagina = verificarPagina(totalRegistros,pagina);
                regs = tm.findIntentos( this.criterios, pagina*PAGE_SIZE , PAGE_SIZE );
                return "";
            }
            catch ( Exception ex )
            {
                log.error( ex.getMessage() );
                return ex.getMessage();
            }
        }
        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            String mensaje = (String) result;
            paginaHistorial = pagina;
            if ( mensaje.isEmpty() )
            {

                String msg = getNavigationMessage(pagina,totalRegistros,regs.size());
                    
                statusMessageLabel.setText( msg );
                
                HistorialTableModel historialModel = new HistorialTableModel(regs);
                tablaHistorial.setModel( historialModel );

                TableColumnModel columns = tablaHistorial.getColumnModel();
                columns.getColumn(0).setPreferredWidth(50);
                columns.getColumn(1).setPreferredWidth(120);
                columns.getColumn(2).setPreferredWidth(120);
                columns.getColumn(3).setPreferredWidth(50);
                columns.getColumn(4).setPreferredWidth(70);
                columns.getColumn(5).setPreferredWidth(90);
                columns.getColumn(6).setPreferredWidth(50);
                columns.getColumn(7).setPreferredWidth(50);
                columns.getColumn(8).setPreferredWidth(50);
                columns.getColumn(9).setPreferredWidth(250);
                
            }
            else
            {
                statusMessageLabel.setText( mensaje );
            }
            
            activarAcciones();            
        }
    }

    @Action
    public void paintHistorial() {
        
        jTextField41.requestFocus();

        jRadioButton9.setSelected(true);
        jRadioButton10.setSelected(false);

        statusMessageLabel.setText( "" );
        
        this.showPanel(historialForm);
        this.showFooterPanel( jPanel14 );
    }

    @Action
    public void validarUsuario() {
        try
        {
            String login = usuarioField.getText();
            String pwd   = passwordField.getText();
            Usuario pUsr = tm.getUsuario(login);
            if ( pUsr.getPassword().equals( pwd ) )
            {
                usr = pUsr;
                configurePermissions();
                this.showPanel( mainMenu );
                User u = new User();
                u.setLogin( usr.getLogin() );
                tm.setUser( u );
                this.refreshUsuario();
            }
            else
            {
                String msg = "Password inválido";
                System.out.println(msg);
               statusMessageLabel.setText(msg);
                
            }
        }
        catch ( Exception ex )
        {
            statusMessageLabel.setText( "Usuario inválido" );
        }
    }
    
    public void refreshUsuario() {
        if ( usr.getNombre() != null && ! usr.getNombre().isEmpty() )
            welcomeLabel.setText( "Bienvenido " + usr.getNombre() );
        else
            welcomeLabel.setText( "Bienvenido " + usr.getLogin() );
        
        divisionesModel = new DefaultComboBoxModel();
        divisionesModel.addElement(new StringItem("Cualquiera",null));
        Usuario u = tm.getUsuario(usr.getLogin());
        
        if ( this.esAdministrador(usr) )
        {
            for ( StringItem div : tm.getDivisiones() )
                divisionesModel.addElement(div);
        }
        else
        {
            if( u.getPlantas() != null && ! u.getPlantas().isEmpty() )
            {
                for ( UsuarioPlanta up : u.getPlantas() )
                    divisionesModel.addElement(tm.getItem(up.getPk().getPlanta()));
            }
        }
        
        this.comboDivsMarcacion.setModel(divisionesModel);
        this.comboDivsNovedad.setModel(divisionesModel);
        this.comboDivsPaquete.setModel(divisionesModel);
        
    }
    
    private boolean esAdministrador(Usuario usr) {
        return usr.getRol() != null && usr.getRol().equals("admin");
    }
    
    public void configurePermissions() {
        if ( esAdministrador( usr ) )
        {
            this.buttonCambiarPassword.setVisible(true);
            this.buttonAdminUsuarios.setVisible(true);
            this.buttonHistorial.setVisible(true);
            this.buttonGenerarArchivo.setVisible(true);
            this.buttonCorreo.setVisible(true);
            this.buttonSincronizarMaestros.setVisible(true);
            this.buttonSincronizarCentrosCosto.setVisible(true);
        }
        else
        {
            this.buttonCambiarPassword.setVisible(true);
            this.buttonAdminUsuarios.setVisible(false);
            this.buttonHistorial.setVisible(false);
            this.buttonGenerarArchivo.setVisible(false);
            this.buttonCorreo.setVisible(false);
            this.buttonSincronizarMaestros.setVisible(false);
            this.buttonSincronizarCentrosCosto.setVisible(false);
        }
    }

    @Action
    public void cambiarContrasena() {
        String old = this.viejaContrasena.getText().trim();
        String nueva1 = this.nuevaContrasena1.getText().trim();
        String nueva2 = this.nuevaContrasena2.getText().trim();
        
        if ( this.usr.getPassword().equals( old ) )
        {
            if ( ! nueva1.isEmpty() && nueva1.equals(nueva2) )
            {
                if ( nueva1.length() >= 6 )
                {
                    if ( nueva1.matches("\\S+") )
                    {
                        usr.setPassword(nueva1);
                        tm.updateUsuario(usr);                   
                        this.showPanel( mainMenu );
                    }
                    else
                    {
                        contrasenaErrorMessage.setText("La contraseña nueva no debe tener espacios.");
                    }
                }
                else
                {
                    contrasenaErrorMessage.setText("La contraseña nueva debe tener mínimo 6 letras.");
                }
            }
            else
            {
                contrasenaErrorMessage.setText("La contraseña nueva no coincide");
            }
        }
        else
        {
            contrasenaErrorMessage.setText("La contraseña anterior no coincide");
        }
        
        
    }

    @Action
    public void showCambiarContrasena() {
        this.showPanel( cambiarPasswordPanel );
        this.showFooterPanel( jPanel14 );
    }
    
    private void checkAdminUsuariosPanel() {
        int selectedUser = listUsuarios.getSelectedIndex();
        boolean enabled = false;
        if ( selectedUser < 0 )
        {
            enabled = false;
        }
        else
        {
            Usuario selUsr = (Usuario) listUsuarios.getSelectedValue();
            if ( selUsr != null && ! "batch".equals(selUsr.getLogin()) )
            {
                enabled = true;
            }
            else 
            {
                enabled = false;
            }
        }

        this.buttonEliminarUsuario.setEnabled(enabled);
        this.buttonEditarUsuario.setEnabled(enabled);
        
    }
    
    private void checkListaPlantas() {
        int selectedUser = this.listPlantasUsuario.getSelectedIndex();
        boolean enabled = false;
        
        if ( selectedUser < 0 )
            enabled = false;
        else
                enabled = true;

        this.botonEliminarPlanta.setEnabled(enabled);
       
    }
    
    

    @Action
    public void showAdminUsuarios() {
        this.refreshUsuarios();
        this.checkAdminUsuariosPanel();
        showPanel(adminUsuariosPanel);
        
        this.showFooterPanel( jPanel14 );
    }
    
    @Action
    public void showEditarPerfil() {
        
        if ( usr.getNombre() != null )
            this.nombreUsuario.setText( usr.getNombre() );
        else
            this.nombreUsuario.setText( "" );

        this.editarPerfilErrorMessage.setText("");
        showPanel( this.editarPerfilPanel );
        this.showFooterPanel( jPanel14 );
    }
    
    @Action
    public void okEditarPerfil() {
        if ( !nombreUsuario.getText().isEmpty() )
        {
            usr.setNombre( nombreUsuario.getText() );
            tm.updateUsuario(usr);
            this.refreshUsuario();
            this.showMainPanel();
        }
        else
        {
            this.editarPerfilErrorMessage.setText("Inserte un nombre de usuario.");
        }
        
    }

    @Action
    public void eliminarUsuario() {

        
        int n = JOptionPane.showConfirmDialog(
                    mainPanel,
                    "¿Desea eliminar el usuario?",
                    "Eliminar Usuario",
                    JOptionPane.YES_NO_OPTION);
        
        if ( n == 0 )
        {
            Usuario selUsr = (Usuario) this.listUsuarios.getSelectedValue();
            tm.deleteUsuario( selUsr.getLogin() );
            this.refreshUsuarios();
        }
    }

    @Action
    public void checkTipoUsuario() {
        
        this.radioUser.setSelected( false );
        this.radioAdmin.setSelected(true);
        
    }

    @Action
    public void showNuevoUsuario() {
        this.radioAdmin.setSelected( false );
        this.radioUser.setSelected( true );
        this.showPanel( this.nuevoUsuarioPanel );
        this.showFooterPanel( jPanel14 );
        
    }

    @Action
    public void checkTipoUsuario2() {
        this.radioUser.setSelected( true );
        this.radioAdmin.setSelected( false );
        
    }

    @Action
    public void okNuevoUsuario() {
        String nuevo = this.nuevoUsuarioLogin.getText();
        
        
            if ( ! nuevo.isEmpty()  )
            {
                if ( nuevo.length() >= 6 )
                {
                    if ( nuevo.matches("\\S+") )
                    {
                        Usuario nuevoUsr = new Usuario();
                        
                        nuevoUsr.setLogin(nuevo);
                        nuevoUsr.setPassword(nuevo);
                        if ( this.radioAdmin.isSelected() )
                            nuevoUsr.setRol("admin");
                        else
                            nuevoUsr.setRol("user");
                        
                        tm.createUsuario( nuevoUsr );
                        
                        this.showAdminUsuarios();
                    }
                    else
                    {
                        nuevoErrorMessage.setText("El nombre de usuario no debe tener espacios.");
                    }
                }
                else
                {
                    nuevoErrorMessage.setText("El nombre de usuario debe tener mínimo 6 letras.");
                }
            }
            else
            {
                this.nuevoErrorMessage.setText("Ingrese el nombre de usuario.");
            }
     
        
        
        
    }

    @Action
    public Task detener() {
        statusMessageLabel.setText("Deteniendo...");
        this.botonDetener.setEnabled(false);
        this.botonDetenerNovedades.setEnabled(false);
        this.botonDetenerPaquete.setEnabled(false);
        return new DetenerTask(getApplication());
    }

    private class DetenerTask extends org.jdesktop.application.Task<Object, Void> {
        DetenerTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to DetenerTask fields, here.
            super(app);
        }
        @Override protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            tm.detener();
            //resetearTarea();     
            
            while( ! tm.isDone() ) 
            {
              try 
              {
                  Thread.sleep(1000*1L);
              }
              catch ( Exception ex )
              {
                  
              }
            }
   
            return null;  // return your result
        }
        @Override 
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
           activarAcciones();   
           statusMessageLabel.setText("");
           
        }
    }


    private void resetearTarea() {
            try 
            {
                tareaProcesarRegistros.cancel(true);
            }
            catch ( Exception ex )
            {

            }        
    }
    
    private Criterios obtenerCriteriosMarcaciones() throws ParseException, NoTienePlantasAsignadas {
            Criterios findMarcaciones = new Criterios();
            Usuario u = tm.getUsuario(usr.getLogin());
            
            if ( esAdministrador(this.usr) )
            {
                // Todas las plantas
            }
            else
            {
                if( u.getPlantas() != null && ! u.getPlantas().isEmpty() )
                {
                    //Según criterio
                }
                else
                    throw new NoTienePlantasAsignadas();
            }

            List<String> ids = new ArrayList<String>();
            ArrayList<IdRange> rangosId = new ArrayList<IdRange>();

            List<String> pernrs = new ArrayList<String>();
            ArrayList<IdRange> rangosPernr = new ArrayList<IdRange>();

            FamiliaUtilsDAO.parse(jTextField25.getText(), ids, rangosId );
            FamiliaUtilsDAO.parse(jTextField26.getText(), pernrs, rangosPernr );

            findMarcaciones.setIds( FamiliaUtilsDAO.stringListToInteger(ids) );
            findMarcaciones.setRangosId(rangosId);
            findMarcaciones.setPernrs(pernrs);
            findMarcaciones.setRangosPernr(rangosPernr);

            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

            DateRange ldateRange = null;
            Date begda = null;
            if ( ! desdeSiboMarcs.getText().trim().isEmpty() )
            begda = df.parse( desdeSiboMarcs.getText() ) ;
            Date endda = null;
            if ( !jTextField16.getText().trim().isEmpty() )
            endda = df.parse( jTextField16.getText() );
            ldateRange = new DateRange( begda, endda );

            DateRange sapRange = null;
            begda = null;
            if ( ! jTextField27.getText().trim().isEmpty() )
            begda = df.parse( jTextField27.getText() ) ;
            endda = null;
            if ( !jTextField28.getText().trim().isEmpty() )
            endda = df.parse( jTextField28.getText() );
            if ( endda != null )
                endda = new Timestamp( endda.getTime() + 1000*60*60*24 - 1);
            sapRange = new DateRange( begda, endda );


            StringItem order = (StringItem) orderItems.getElementAt( jComboBox1.getSelectedIndex() );
            String orderBy = order.getValue();
            String orderDirection = "desc";
            if ( jRadioButton4.isSelected() )
                orderDirection = "asc";



            StringItem estado = (StringItem) estados.getElementAt(jComboBox2.getSelectedIndex());
            findMarcaciones.setRetorno( estado.getValue());            
            if ( ! timesoft.Constantes.NUEVA.equals(estado.getValue()) )
                findMarcaciones.setCntrl(sapRange);

            findMarcaciones.setCapturaSibo(ldateRange);
            findMarcaciones.setOrderBy( orderBy );
            findMarcaciones.setOrderDirection( orderDirection );
            
            StringItem descripcion = (StringItem) descripcionesModel.getElementAt( descripcionesMarcacion.getSelectedIndex() );
            findMarcaciones.setMensaje(descripcion.getValue());
            
            StringItem div = (StringItem) this.divisionesModel.getElementAt(this.comboDivsMarcacion.getSelectedIndex());
            if ( div.getValue() != null )
            {
                Set<String> divs = new HashSet<String>();
                divs.add(div.getValue());
                findMarcaciones.setSubdivisiones(divs);
            }
            else
            {
                Set<String> plantas = new HashSet<String>();
                for ( UsuarioPlanta up : u.getPlantas() )
                    plantas.add(up.getPk().getPlanta());
                findMarcaciones.setSubdivisiones(plantas);
            }
            
            DefaultComboBoxModel subsModel = (DefaultComboBoxModel) this.comboSubsMarcacion.getModel();
            StringItem sub = (StringItem) subsModel.getElementAt(comboSubsMarcacion.getSelectedIndex());
            findMarcaciones.setBtrtl(sub.getValue());
            
            return findMarcaciones;
    }
    
    private Criterios obtenerCriteriosNovedades() throws ParseException, NoTienePlantasAsignadas {
        Criterios findNovedades = new Criterios();
        Usuario u = tm.getUsuario(usr.getLogin());
        
        if ( esAdministrador(this.usr) )
        {
            // Todas las plantas
        }
        else
        {
            if( u.getPlantas() != null && ! u.getPlantas().isEmpty() )
            {
                // Según criterios
            }
            else
                throw new NoTienePlantasAsignadas();            
        }        

        ArrayList<String> ids = new ArrayList<String>();
        ArrayList<IdRange> rangosId = new ArrayList<IdRange>();

        ArrayList<String> pernrs = new ArrayList<String>();
        ArrayList<IdRange> rangosPernr = new ArrayList<IdRange>();

        FamiliaUtilsDAO.parse(jTextField31.getText(), ids, rangosId );
        FamiliaUtilsDAO.parse(jTextField32.getText(), pernrs, rangosPernr );

        findNovedades.setIds( FamiliaUtilsDAO.stringListToInteger(ids) );
        findNovedades.setRangosId(rangosId);
        findNovedades.setPernrs(pernrs);
        findNovedades.setRangosPernr(rangosPernr);

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        DateRange ldateRange = null;
        Date begda = null;
        if ( ! desdeSiboNovedades.getText().trim().isEmpty() )
            begda = df.parse( desdeSiboNovedades.getText() ) ;
        Date endda = null;
        if ( !hastaSiboNovedades.getText().trim().isEmpty() )
            endda = df.parse( hastaSiboNovedades.getText() );
        ldateRange = new DateRange( begda, endda );

        DateRange sapRange = null;
        begda = null;
        if ( ! desdeSapNovedades.getText().trim().isEmpty() )
            begda = df.parse( desdeSapNovedades.getText() ) ;
        endda = null;
        if ( !hastaSapNovedades.getText().trim().isEmpty() )
            endda = df.parse( hastaSapNovedades.getText() );
        sapRange = new DateRange( begda, endda );

        DefaultComboBoxModel novedadesOrder = (DefaultComboBoxModel) jComboBox3.getModel();
        StringItem order = (StringItem) novedadesOrder.getElementAt( jComboBox3.getSelectedIndex() );
        String orderBy = order.getValue();
        String orderDirection = "desc";
        if ( jRadioButton6.isSelected() )
            orderDirection = "asc";


        StringItem estado = (StringItem) estados.getElementAt(comboEstadoNovedades.getSelectedIndex());
        findNovedades.setRetorno( estado.getValue());        
        if ( ! timesoft.Constantes.NUEVA.equals(estado.getValue()) )
            findNovedades.setCntrl(sapRange);

        findNovedades.setCapturaSibo(ldateRange);
        findNovedades.setOrderBy( orderBy );
        findNovedades.setOrderDirection( orderDirection );
        
        StringItem descripcion = (StringItem) descripcionesModel.getElementAt( descripcionesNovedad.getSelectedIndex() );
        findNovedades.setMensaje(descripcion.getValue());
        
        StringItem div = (StringItem) this.divisionesModel.getElementAt(this.comboDivsNovedad.getSelectedIndex());
        if ( div.getValue() != null )
        {
            Set<String> divs = new HashSet<String>();
            divs.add(div.getValue());
            findNovedades.setSubdivisiones(divs);
        }
        else
        {
            Set<String> plantas = new HashSet<String>();
            for ( UsuarioPlanta up : u.getPlantas() )
                plantas.add(up.getPk().getPlanta());
            findNovedades.setSubdivisiones(plantas);
        }

        DefaultComboBoxModel subsModel = (DefaultComboBoxModel) this.comboSubsNovedad.getModel();
        StringItem sub = (StringItem) subsModel.getElementAt(comboSubsNovedad.getSelectedIndex());
        findNovedades.setBtrtl(sub.getValue());

        return findNovedades;
    }
    
    public Criterios obtenerCriteriosPaquete() throws ParseException, NoTienePlantasAsignadas {
        
        Criterios criteria = new Criterios();
        Usuario u = tm.getUsuario(usr.getLogin());
        
        if ( esAdministrador(this.usr) )
        {
            // Todas las plantas
        }
        else
        {
            if( u.getPlantas() != null && ! u.getPlantas().isEmpty() )
            {
                // Según criterios
            }
            else
                throw new NoTienePlantasAsignadas();          
        }          

        List<String> ids = new ArrayList<String>();
        ArrayList<IdRange> rangosId = new ArrayList<IdRange>();

        ArrayList<String> pernrs = new ArrayList<String>();
        ArrayList<IdRange> rangosPernr = new ArrayList<IdRange>();

        FamiliaUtilsDAO.parse(jTextField37.getText(), ids, rangosId );
        FamiliaUtilsDAO.parse(jTextField38.getText(), pernrs, rangosPernr );

        criteria.setIds( FamiliaUtilsDAO.stringListToInteger(ids) );
        criteria.setRangosId(rangosId);
        criteria.setPernrs(pernrs);
        criteria.setRangosPernr(rangosPernr);

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        DateRange ldateRange = null;
        Date begda = null;
        if ( ! desdeSiboPaquete.getText().trim().isEmpty() )
        begda = df.parse( desdeSiboPaquete.getText() ) ;
        Date endda = null;
        if ( !hastaSiboPaquete.getText().trim().isEmpty() )
        endda = df.parse( hastaSiboPaquete.getText() );
        if ( endda != null )
        endda = new Timestamp( endda.getTime() + 1000*60*60*24 - 1);
        ldateRange = new DateRange( begda, endda );

        DateRange sapRange = null;
        begda = null;
        if ( ! desdeSapPaquete.getText().trim().isEmpty() )
        begda = df.parse( desdeSapPaquete.getText() ) ;
        endda = null;
        if ( !hastaSapPaquete.getText().trim().isEmpty() )
        endda = df.parse( hastaSapPaquete.getText() );
        sapRange = new DateRange( begda, endda );

        DefaultComboBoxModel orderModel = (DefaultComboBoxModel) jComboBox5.getModel();
        StringItem order = (StringItem) orderModel.getElementAt( jComboBox5.getSelectedIndex() );
        String orderBy = order.getValue();
        String orderDirection = "desc";
        if ( jRadioButton8.isSelected() )
        orderDirection = "asc";

        DefaultComboBoxModel tipoModel = (DefaultComboBoxModel) jComboBox7.getModel();
        StringItem tipo = (StringItem) tipoModel.getSelectedItem();
        if ( tipo.getValue() != null )
        criteria.setTipo( tipo.getValue() );

        StringItem estado = (StringItem) estados.getElementAt(comboEstadoPaquete.getSelectedIndex());
        criteria.setRetorno( estado.getValue());
        
        if ( ! timesoft.Constantes.NUEVA.equals(estado.getValue()) )
            criteria.setCntrl( sapRange );

        criteria.setCapturaSibo(ldateRange);
        criteria.setOrderBy( orderBy );
        criteria.setOrderDirection( orderDirection );
        
        StringItem descripcion = (StringItem) descripcionesModel.getElementAt( descripcionesPaquete.getSelectedIndex() );
        criteria.setMensaje(descripcion.getValue());
        
        StringItem div = (StringItem) this.divisionesModel.getElementAt(this.comboDivsPaquete.getSelectedIndex());
        if ( div.getValue() != null )
        {
            Set<String> divs = new HashSet<String>();
            divs.add(div.getValue());
            criteria.setSubdivisiones(divs);
        }
        else
        {
            Set<String> plantas = new HashSet<String>();
            for ( UsuarioPlanta up : u.getPlantas() )
                plantas.add(up.getPk().getPlanta());
            criteria.setSubdivisiones(plantas);
        }

        DefaultComboBoxModel subsModel = (DefaultComboBoxModel) this.comboSubsPaquete.getModel();
        StringItem sub = (StringItem) subsModel.getElementAt(comboSubsPaquete.getSelectedIndex());
        criteria.setBtrtl(sub.getValue());

        return criteria;
            
    }

    @Action
    public void exportarMarcacionesExcel() throws ParseException, NoTienePlantasAsignadas {
        
        System.out.println("Exportar...");
        
        jFileChooser2.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileFilter filter = new FileNameExtensionFilter("Archivos de Excel", "xls", "xlsx", "ods");
        jFileChooser2.setFileFilter(filter);
        
        String filename = null;
        int returnVal = jFileChooser2.showOpenDialog( getComponent() );
        if ( returnVal == JFileChooser.APPROVE_OPTION)
        {
           filename = jFileChooser2.getSelectedFile().getAbsolutePath();
           if ( ! filename.endsWith(".xls") &&
                ! filename.endsWith(".xlsx") &&
                ! filename.endsWith(".ods") )
               filename = filename + ".xls";
        }
        else
            return;
        
        File file = new File(filename);
        
        if ( file.exists() )
        {
           int answer = JOptionPane.showConfirmDialog(
            correoPanel,
            "¿Desea sobrescribir el archivo seleccionado?",
            "Familia",
            JOptionPane.YES_NO_OPTION);     
           if ( answer != 0 )
               return;
        }
        
        System.out.println("Exportando " + filename + "...");
        
        Criterios findMarcaciones = obtenerCriteriosMarcaciones();
        List<Registro> regs = tm.getMarcaciones( findMarcaciones, 0, Integer.MAX_VALUE );        
        
        FileOutputStream fileOutputStream = null;
        HSSFWorkbook sampleWorkbook = null;
        HSSFSheet sampleDataSheet = null;
        try {
            /**
             * Create a new instance for HSSFWorkBook class and create a
             * sample worksheet using HSSFSheet class to write data.
             */
            sampleWorkbook = new HSSFWorkbook();
            sampleDataSheet = sampleWorkbook.createSheet("Marcaciones");
            /**
             * Create two rows using HSSFRow class, where headerRow denotes the
             * header and the dataRow1 denotes the cell data.
             */
            
            int row = 0;
            
            HSSFCellStyle cellStyle = setHeaderStyle(sampleWorkbook);
            
            
            HSSFRow titulos = sampleDataSheet.createRow(row++);
            HSSFCell titulosCell = titulos.createCell(0);
            titulosCell.setCellStyle(cellStyle);
            titulosCell.setCellValue(new HSSFRichTextString("Marcaciones"));

            HSSFRow fechaSibo = sampleDataSheet.createRow(row++);
            HSSFCell fechaSiboCell = fechaSibo.createCell(0);
            fechaSiboCell.setCellStyle(cellStyle);
            fechaSiboCell.setCellValue(new HSSFRichTextString("Fecha"));
            HSSFCell fechaInicioSiboCell = fechaSibo.createCell(1);
            fechaInicioSiboCell.setCellValue(new HSSFRichTextString( desdeSiboMarcs.getText() ));
            HSSFCell fechaFinSiboCell = fechaSibo.createCell(2);
            fechaFinSiboCell.setCellValue(new HSSFRichTextString(jTextField16.getText()));

            HSSFRow procesadoSap = sampleDataSheet.createRow(row++);
            HSSFCell fechaSapCell = procesadoSap.createCell(0);
            fechaSapCell.setCellStyle(cellStyle);
            fechaSapCell.setCellValue(new HSSFRichTextString("Procesado SAP"));
            HSSFCell fechaInicioSapCell = procesadoSap.createCell(1);
            fechaInicioSapCell.setCellValue(new HSSFRichTextString( jTextField27.getText() ));
            HSSFCell fechaFinSapCell = procesadoSap.createCell(2);
            fechaFinSapCell.setCellValue(new HSSFRichTextString(jTextField28.getText()));

            HSSFRow idsRow = sampleDataSheet.createRow(row++);
            HSSFCell idsCell = idsRow.createCell(0);
            idsCell.setCellStyle(cellStyle);
            idsCell.setCellValue(new HSSFRichTextString("Ids"));
            HSSFCell idsValueCell = idsRow.createCell(1);
            idsValueCell.setCellValue(new HSSFRichTextString( jTextField25.getText() ));

            HSSFRow pernrRow = sampleDataSheet.createRow(row++);
            HSSFCell pernrCell = pernrRow.createCell(0);
            pernrCell.setCellStyle(cellStyle);
            pernrCell.setCellValue(new HSSFRichTextString("Pernrs"));
            HSSFCell pernrValueCell = pernrRow.createCell(1);
            pernrValueCell.setCellValue(new HSSFRichTextString( jTextField26.getText() ));
            
            HSSFRow estadoRow = sampleDataSheet.createRow(row++);
            HSSFCell estadoCell = estadoRow.createCell(0);
            estadoCell.setCellStyle(cellStyle);
            estadoCell.setCellValue(new HSSFRichTextString("Estado"));
            HSSFCell estadoValueCell = estadoRow.createCell(1);
            StringItem estado = (StringItem) estados.getElementAt(jComboBox2.getSelectedIndex());            
            estadoValueCell.setCellValue(new HSSFRichTextString( estado.getLabel() ));

            HSSFRow descripcionRow = sampleDataSheet.createRow(row++);
            HSSFCell descripcionCell = descripcionRow.createCell(0);
            descripcionCell.setCellStyle(cellStyle);
            descripcionCell.setCellValue(new HSSFRichTextString("Descripción del error"));
            HSSFCell descripcionValueCell = descripcionRow.createCell(1);
            StringItem descripcion = (StringItem) descripcionesModel.getElementAt( descripcionesMarcacion.getSelectedIndex() );
            descripcionValueCell.setCellValue(new HSSFRichTextString( descripcion.getLabel() ));
                        
            HSSFRow subdisivionRow = sampleDataSheet.createRow(row++);
            HSSFCell subdivisionCell = subdisivionRow.createCell(0);
            subdivisionCell.setCellStyle(cellStyle);
            subdivisionCell.setCellValue(new HSSFRichTextString("Subdivisión"));
            HSSFCell subdivisionValueCell = subdisivionRow.createCell(1);
            DefaultComboBoxModel subsModel = (DefaultComboBoxModel) this.comboSubsMarcacion.getModel();
            StringItem sub = (StringItem) subsModel.getElementAt(comboSubsMarcacion.getSelectedIndex());
            subdivisionValueCell.setCellValue(new HSSFRichTextString(sub.toString()));
            
            
            HSSFRow headerRow = sampleDataSheet.createRow(row++);
            /**
             * Call the setHeaderStyle method and set the styles for the
             * all the three header cells.
             */
            int column = 0;
            HSSFCell firstHeaderCell = headerRow.createCell(column++);
            firstHeaderCell.setCellStyle(cellStyle);
            firstHeaderCell.setCellValue(new HSSFRichTextString("ID"));
            HSSFCell secondHeaderCell = headerRow.createCell(column++);
            secondHeaderCell.setCellStyle(cellStyle);
            secondHeaderCell.setCellValue(new HSSFRichTextString("PERNR"));
            
            HSSFCell nombreCell = headerRow.createCell(column++);
            nombreCell.setCellStyle(cellStyle);
            nombreCell.setCellValue(new HSSFRichTextString("NOMBRE"));
            
            HSSFCell divCell = headerRow.createCell(column++);
            divCell.setCellStyle(cellStyle);
            divCell.setCellValue(new HSSFRichTextString("DIV"));
            HSSFCell thirdHeaderCell = headerRow.createCell(column++);
            thirdHeaderCell.setCellStyle(cellStyle);
            thirdHeaderCell.setCellValue(new HSSFRichTextString("ESTADO"));
            HSSFCell fourthHeaderCell = headerRow.createCell(column++);
            fourthHeaderCell.setCellStyle(cellStyle);
            fourthHeaderCell.setCellValue(new HSSFRichTextString("Captura SIBO"));
            HSSFCell fifthHeaderCell = headerRow.createCell(column++);
            fifthHeaderCell.setCellStyle(cellStyle);
            fifthHeaderCell.setCellValue(new HSSFRichTextString("Procesado TimeHR"));
            HSSFCell sixthHeaderCell = headerRow.createCell(column++);
            sixthHeaderCell.setCellStyle(cellStyle);
            sixthHeaderCell.setCellValue(new HSSFRichTextString("Intentos de proceso"));
            HSSFCell operadorCell = headerRow.createCell(column++);
            operadorCell.setCellStyle(cellStyle);
            operadorCell.setCellValue(new HSSFRichTextString("Operador"));
            HSSFCell subCell = headerRow.createCell(column++);
            subCell.setCellStyle(cellStyle);
            subCell.setCellValue(new HSSFRichTextString("Subdivisión"));
            HSSFCell seventhHeaderCell = headerRow.createCell(column++);
            seventhHeaderCell.setCellStyle(cellStyle);
            seventhHeaderCell.setCellValue(new HSSFRichTextString("Descripción"));
            
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");            
            
            // Set the cell value for all the data rows.
  
            for ( Registro reg : regs ) 
            {
                 int columnValue = 0;
                 HSSFRow dataRow = sampleDataSheet.createRow(row++);
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( String.valueOf(reg.getId())));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( reg.getPernr() ) );
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( TimesoftManager.getNombres(reg) ));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(reg.getSubdivision()));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( reg.getRetorno()));
                 
                 if ( reg.getFechaCaptura() != null )
                     dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( df.format(reg.getFechaCaptura())));
                 else
                     dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( ""));
                     
                 if ( reg.getCntrl() != null )
                     dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( df.format(reg.getCntrl())));
                 else 
                     dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( "" ));
                 
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( reg.getCntdr() ) );
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(reg.getOperador()));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(reg.getBtrtl()));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( reg.getMensaje()));
                
            }
            
            
            fileOutputStream = new FileOutputStream(filename);
            sampleWorkbook.write(fileOutputStream);
            
            JOptionPane.showMessageDialog(
            centerPanel,
            "Archivo " + filename + " guardado con éxito",
            "Familia",
            JOptionPane.INFORMATION_MESSAGE );       
            
        } 
        catch (Exception ex) 
        {
            statusMessageLabel.setText(ex.getMessage());
        } 
        finally 
        {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }        
    }
    
    
    @Action
    public void exportarNovedadesExcel() throws ParseException, NoTienePlantasAsignadas {
        
        System.out.println("Exportar...");
        
        jFileChooser2.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileFilter filter = new FileNameExtensionFilter("Archivos de Excel", "xls", "xlsx", "ods");
        jFileChooser2.setFileFilter(filter);
        
        String filename = null;
        int returnVal = jFileChooser2.showOpenDialog( getComponent() );
        if ( returnVal == JFileChooser.APPROVE_OPTION)
        {
           filename = jFileChooser2.getSelectedFile().getAbsolutePath();
           if ( ! filename.endsWith(".xls") &&
                ! filename.endsWith(".xlsx") &&
                ! filename.endsWith(".ods") )
               filename = filename + ".xls";
        }
        else
            return;
        
        File file = new File(filename);
        
        if ( file.exists() )
        {
           int answer = JOptionPane.showConfirmDialog(
            correoPanel,
            "¿Desea sobrescribir el archivo seleccionado?",
            "Familia",
            JOptionPane.YES_NO_OPTION);     
           if ( answer != 0 )
               return;
        }
        
        System.out.println("Exportando novedades " + filename + "...");
        
        Criterios findNovedades = obtenerCriteriosNovedades();
        List<Registro> regs = tm.getNovedades( findNovedades, 0, Integer.MAX_VALUE );
        
        FileOutputStream fileOutputStream = null;
        HSSFWorkbook sampleWorkbook = null;
        HSSFSheet sampleDataSheet = null;
        try {
            /**
             * Create a new instance for HSSFWorkBook class and create a
             * sample worksheet using HSSFSheet class to write data.
             */
            sampleWorkbook = new HSSFWorkbook();
            sampleDataSheet = sampleWorkbook.createSheet("Novedades");
            /**
             * Create two rows using HSSFRow class, where headerRow denotes the
             * header and the dataRow1 denotes the cell data.
             */
            
            int row = 0;
            
            HSSFCellStyle cellStyle = setHeaderStyle(sampleWorkbook);
            
            HSSFRow titulos = sampleDataSheet.createRow(row++);
            HSSFCell titulosCell = titulos.createCell(0);
            titulosCell.setCellStyle(cellStyle);
            titulosCell.setCellValue(new HSSFRichTextString("Novedades"));

            HSSFRow fechaSibo = sampleDataSheet.createRow(row++);
            HSSFCell fechaSiboCell = fechaSibo.createCell(0);
            fechaSiboCell.setCellStyle(cellStyle);
            fechaSiboCell.setCellValue(new HSSFRichTextString("Fecha"));
            HSSFCell fechaInicioSiboCell = fechaSibo.createCell(1);
            fechaInicioSiboCell.setCellValue(new HSSFRichTextString( desdeSiboNovedades.getText() ));
            HSSFCell fechaFinSiboCell = fechaSibo.createCell(2);
            fechaFinSiboCell.setCellValue(new HSSFRichTextString(hastaSiboNovedades.getText()));

            HSSFRow procesadoSap = sampleDataSheet.createRow(row++);
            HSSFCell fechaSapCell = procesadoSap.createCell(0);
            fechaSapCell.setCellStyle(cellStyle);
            fechaSapCell.setCellValue(new HSSFRichTextString("Procesado SAP"));
            HSSFCell fechaInicioSapCell = procesadoSap.createCell(1);
            fechaInicioSapCell.setCellValue(new HSSFRichTextString( desdeSapNovedades.getText() ));
            HSSFCell fechaFinSapCell = procesadoSap.createCell(2);
            fechaFinSapCell.setCellValue(new HSSFRichTextString(hastaSapNovedades.getText()));

            HSSFRow idsRow = sampleDataSheet.createRow(row++);
            HSSFCell idsCell = idsRow.createCell(0);
            idsCell.setCellStyle(cellStyle);
            idsCell.setCellValue(new HSSFRichTextString("Ids"));
            HSSFCell idsValueCell = idsRow.createCell(1);
            idsValueCell.setCellValue(new HSSFRichTextString( jTextField31.getText() ));

            HSSFRow pernrRow = sampleDataSheet.createRow(row++);
            HSSFCell pernrCell = pernrRow.createCell(0);
            pernrCell.setCellStyle(cellStyle);
            pernrCell.setCellValue(new HSSFRichTextString("Pernrs"));
            HSSFCell pernrValueCell = pernrRow.createCell(1);
            pernrValueCell.setCellValue(new HSSFRichTextString( jTextField32.getText() ));
            
            HSSFRow estadoRow = sampleDataSheet.createRow(row++);
            HSSFCell estadoCell = estadoRow.createCell(0);
            estadoCell.setCellStyle(cellStyle);
            estadoCell.setCellValue(new HSSFRichTextString("Estado"));
            HSSFCell estadoValueCell = estadoRow.createCell(1);
            StringItem estado = (StringItem) estados.getElementAt(comboEstadoNovedades.getSelectedIndex());
            estadoValueCell.setCellValue(new HSSFRichTextString( estado.getLabel() ));

            HSSFRow descripcionRow = sampleDataSheet.createRow(row++);
            HSSFCell descripcionCell = descripcionRow.createCell(0);
            descripcionCell.setCellStyle(cellStyle);
            descripcionCell.setCellValue(new HSSFRichTextString("Descripción del error"));
            HSSFCell descripcionValueCell = descripcionRow.createCell(1);
            StringItem descripcion = (StringItem) descripcionesModel.getElementAt( descripcionesNovedad.getSelectedIndex() );
            descripcionValueCell.setCellValue(new HSSFRichTextString( descripcion.getLabel() ));
                        
            HSSFRow subdisivionRow = sampleDataSheet.createRow(row++);
            HSSFCell subdivisionCell = subdisivionRow.createCell(0);
            subdivisionCell.setCellStyle(cellStyle);
            subdivisionCell.setCellValue(new HSSFRichTextString("Subdivisión"));
            HSSFCell subdivisionValueCell = subdisivionRow.createCell(1);
            DefaultComboBoxModel subsModel = (DefaultComboBoxModel) this.comboSubsNovedad.getModel();
            StringItem sub = (StringItem) subsModel.getElementAt(comboSubsNovedad.getSelectedIndex());
            subdivisionValueCell.setCellValue(new HSSFRichTextString(sub.toString()));
            

            int column = 0;
            HSSFRow headerRow = sampleDataSheet.createRow(row++);
            /**
             * Call the setHeaderStyle method and set the styles for the
             * all the three header cells.
             */
            HSSFCell firstHeaderCell = headerRow.createCell(column++);
            firstHeaderCell.setCellStyle(cellStyle);
            firstHeaderCell.setCellValue(new HSSFRichTextString("ID"));
            HSSFCell secondHeaderCell = headerRow.createCell(column++);
            secondHeaderCell.setCellStyle(cellStyle);
            secondHeaderCell.setCellValue(new HSSFRichTextString("PERNR"));
            
            HSSFCell nombreCell = headerRow.createCell(column++);
            nombreCell.setCellStyle(cellStyle);
            nombreCell.setCellValue(new HSSFRichTextString("NOMBRE"));
            
            HSSFCell divCell = headerRow.createCell(column++);
            divCell.setCellStyle(cellStyle);
            divCell.setCellValue(new HSSFRichTextString("DIV"));
            
            HSSFCell thirdHeaderCell = headerRow.createCell(column++);
            thirdHeaderCell.setCellStyle(cellStyle);
            thirdHeaderCell.setCellValue(new HSSFRichTextString("ESTADO"));
            HSSFCell fourthHeaderCell = headerRow.createCell(column++);
            fourthHeaderCell.setCellStyle(cellStyle);
            fourthHeaderCell.setCellValue(new HSSFRichTextString("Fecha Ini"));
            
            HSSFCell fechaFinCell = headerRow.createCell(column++);
            fechaFinCell.setCellStyle(cellStyle);
            fechaFinCell.setCellValue(new HSSFRichTextString("Fecha Fin"));
            HSSFCell horaIniCell = headerRow.createCell(column++);
            horaIniCell.setCellStyle(cellStyle);
            horaIniCell.setCellValue(new HSSFRichTextString("Hora Ini"));
            HSSFCell horaFinCell = headerRow.createCell(column++);
            horaFinCell.setCellStyle(cellStyle);
            horaFinCell.setCellValue(new HSSFRichTextString("Hora Fin"));
            HSSFCell ccCell = headerRow.createCell(column++);
            ccCell.setCellStyle(cellStyle);
            ccCell.setCellValue(new HSSFRichTextString("Centro de costo"));
            HSSFCell vtkenCell = headerRow.createCell(column++);
            vtkenCell.setCellStyle(cellStyle);
            vtkenCell.setCellValue(new HSSFRichTextString("VTKEN"));
            
            
            HSSFCell fifthHeaderCell = headerRow.createCell(column++);
            fifthHeaderCell.setCellStyle(cellStyle);
            fifthHeaderCell.setCellValue(new HSSFRichTextString("Procesado TimeHR"));
            HSSFCell sixthHeaderCell = headerRow.createCell(column++);
            sixthHeaderCell.setCellStyle(cellStyle);
            sixthHeaderCell.setCellValue(new HSSFRichTextString("Intentos de proceso"));
            HSSFCell operadorCell = headerRow.createCell(column++);
            operadorCell.setCellStyle(cellStyle);
            operadorCell.setCellValue(new HSSFRichTextString("Operador"));
            HSSFCell subCell = headerRow.createCell(column++);
            subCell.setCellStyle(cellStyle);
            subCell.setCellValue(new HSSFRichTextString("Subdivisión"));
            HSSFCell seventhHeaderCell = headerRow.createCell(column++);
            seventhHeaderCell.setCellStyle(cellStyle);
            seventhHeaderCell.setCellValue(new HSSFRichTextString("Descripción"));
            
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");            
            
            // Set the cell value for all the data rows.
  
            for ( Registro registro : regs ) 
            {
                 int columnValue = 0;
                 Novedad reg = (Novedad) registro;
                 HSSFRow dataRow = sampleDataSheet.createRow(row++);
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( String.valueOf(reg.getId())));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( reg.getPernr() ) );
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( TimesoftManager.getNombres(reg)));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(reg.getSubdivision()));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( reg.getRetorno()));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(reg.getBegda()));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(reg.getEndda()));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(reg.getBeguz()));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(reg.getEnduz()));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(reg.getKostl()));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(reg.getVtken()));
  
                 if ( reg.getCntrl() != null )
                     dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( df.format(reg.getCntrl())));
                 else 
                     dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( "" ));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( reg.getCntdr() ) );
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(reg.getOperador()));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(reg.getBtrtl()));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( reg.getMensaje()));
                
            }
            
            
            fileOutputStream = new FileOutputStream(filename);
            sampleWorkbook.write(fileOutputStream);
            
            JOptionPane.showMessageDialog(
            centerPanel,
            "Archivo " + filename + " guardado con éxito",
            "Familia",
            JOptionPane.INFORMATION_MESSAGE );       
            
        } 
        catch (Exception ex) 
        {
            statusMessageLabel.setText(ex.getMessage());
        } 
        finally 
        {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }        
    }

    @Action
    public void exportarPaqueteExcel() throws ParseException, NoTienePlantasAsignadas {
        
        System.out.println("Exportar...");
        
        jFileChooser2.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileFilter filter = new FileNameExtensionFilter("Archivos de Excel", "xls", "xlsx", "ods");
        jFileChooser2.setFileFilter(filter);
        
        String filename = null;
        int returnVal = jFileChooser2.showOpenDialog( getComponent() );
        if ( returnVal == JFileChooser.APPROVE_OPTION)
        {
           filename = jFileChooser2.getSelectedFile().getAbsolutePath();
           if ( ! filename.endsWith(".xls") &&
                ! filename.endsWith(".xlsx") &&
                ! filename.endsWith(".ods") )
               filename = filename + ".xls";
        }
        else
            return;
        
        File file = new File(filename);
        
        if ( file.exists() )
        {
           int answer = JOptionPane.showConfirmDialog(
            correoPanel,
            "¿Desea sobrescribir el archivo seleccionado?",
            "Familia",
            JOptionPane.YES_NO_OPTION);     
           if ( answer != 0 )
               return;
        }
        
        System.out.println("Exportando novedades paquete " + filename + "...");
        
        Criterios findPaquete = obtenerCriteriosPaquete();
        List<Registro> regs = tm.getNovedadesPaquete( findPaquete, 0, Integer.MAX_VALUE );
        
        FileOutputStream fileOutputStream = null;
        HSSFWorkbook sampleWorkbook = null;
        HSSFSheet sampleDataSheet = null;
        try {
            /**
             * Create a new instance for HSSFWorkBook class and create a
             * sample worksheet using HSSFSheet class to write data.
             */
            sampleWorkbook = new HSSFWorkbook();
            sampleDataSheet = sampleWorkbook.createSheet("Novedades Paquete");
            /**
             * Create two rows using HSSFRow class, where headerRow denotes the
             * header and the dataRow1 denotes the cell data.
             */
            
            int row = 0;
            
            HSSFCellStyle cellStyle = setHeaderStyle(sampleWorkbook);
            
            
            HSSFRow titulos = sampleDataSheet.createRow(row++);
            HSSFCell titulosCell = titulos.createCell(0);
            titulosCell.setCellStyle(cellStyle);
            titulosCell.setCellValue(new HSSFRichTextString("Novedades Paquete"));

            HSSFRow fechaSibo = sampleDataSheet.createRow(row++);
            HSSFCell fechaSiboCell = fechaSibo.createCell(0);
            fechaSiboCell.setCellStyle(cellStyle);
            fechaSiboCell.setCellValue(new HSSFRichTextString("Fecha"));
            HSSFCell fechaInicioSiboCell = fechaSibo.createCell(1);
            fechaInicioSiboCell.setCellValue(new HSSFRichTextString( this.desdeSiboPaquete.getText() ));
            HSSFCell fechaFinSiboCell = fechaSibo.createCell(2);
            fechaFinSiboCell.setCellValue(new HSSFRichTextString(hastaSiboPaquete.getText()));

            HSSFRow procesadoSap = sampleDataSheet.createRow(row++);
            HSSFCell fechaSapCell = procesadoSap.createCell(0);
            fechaSapCell.setCellStyle(cellStyle);
            fechaSapCell.setCellValue(new HSSFRichTextString("Procesado SAP"));
            HSSFCell fechaInicioSapCell = procesadoSap.createCell(1);
            fechaInicioSapCell.setCellValue(new HSSFRichTextString( desdeSapPaquete.getText() ));
            HSSFCell fechaFinSapCell = procesadoSap.createCell(2);
            fechaFinSapCell.setCellValue(new HSSFRichTextString(hastaSapPaquete.getText()));

            HSSFRow idsRow = sampleDataSheet.createRow(row++);
            HSSFCell idsCell = idsRow.createCell(0);
            idsCell.setCellStyle(cellStyle);
            idsCell.setCellValue(new HSSFRichTextString("Ids"));
            HSSFCell idsValueCell = idsRow.createCell(1);
            idsValueCell.setCellValue(new HSSFRichTextString( jTextField37.getText() ));

            HSSFRow pernrRow = sampleDataSheet.createRow(row++);
            HSSFCell pernrCell = pernrRow.createCell(0);
            pernrCell.setCellStyle(cellStyle);
            pernrCell.setCellValue(new HSSFRichTextString("Pernrs"));
            HSSFCell pernrValueCell = pernrRow.createCell(1);
            pernrValueCell.setCellValue(new HSSFRichTextString( jTextField38.getText() ));
            
            HSSFRow estadoRow = sampleDataSheet.createRow(row++);
            HSSFCell estadoCell = estadoRow.createCell(0);
            estadoCell.setCellStyle(cellStyle);
            estadoCell.setCellValue(new HSSFRichTextString("Estado"));
            HSSFCell estadoValueCell = estadoRow.createCell(1);
            StringItem estado = (StringItem) estados.getElementAt(comboEstadoPaquete.getSelectedIndex());
            estadoValueCell.setCellValue(new HSSFRichTextString( estado.getLabel() ));

            HSSFRow descripcionRow = sampleDataSheet.createRow(row++);
            HSSFCell descripcionCell = descripcionRow.createCell(0);
            descripcionCell.setCellStyle(cellStyle);
            descripcionCell.setCellValue(new HSSFRichTextString("Descripción del error"));
            HSSFCell descripcionValueCell = descripcionRow.createCell(1);
            StringItem descripcion = (StringItem) descripcionesModel.getElementAt( descripcionesPaquete.getSelectedIndex() );
            descripcionValueCell.setCellValue(new HSSFRichTextString( descripcion.getLabel() ));
                        
            HSSFRow subdisivionRow = sampleDataSheet.createRow(row++);
            HSSFCell subdivisionCell = subdisivionRow.createCell(0);
            subdivisionCell.setCellStyle(cellStyle);
            subdivisionCell.setCellValue(new HSSFRichTextString("Subdivisión"));
            HSSFCell subdivisionValueCell = subdisivionRow.createCell(1);
            DefaultComboBoxModel subsModel = (DefaultComboBoxModel) this.comboSubsPaquete.getModel();
            StringItem sub = (StringItem) subsModel.getElementAt(comboSubsPaquete.getSelectedIndex());
            subdivisionValueCell.setCellValue(new HSSFRichTextString(sub.toString()));

            HSSFRow tipoRow = sampleDataSheet.createRow(row++);
            HSSFCell tipoCell = tipoRow.createCell(0);
            tipoCell.setCellStyle(cellStyle);
            tipoCell.setCellValue(new HSSFRichTextString("Tipo"));
            HSSFCell tipoValueCell = tipoRow.createCell(1);
            DefaultComboBoxModel tipoModel = (DefaultComboBoxModel) jComboBox7.getModel();
            StringItem tipo = (StringItem) tipoModel.getSelectedItem();
            tipoValueCell.setCellValue(new HSSFRichTextString( tipo.getLabel() ));
            
            int column = 0;
            HSSFRow headerRow = sampleDataSheet.createRow(row++);
            /**
             * Call the setHeaderStyle method and set the styles for the
             * all the three header cells.
             */
            HSSFCell firstHeaderCell = headerRow.createCell(column++);
            firstHeaderCell.setCellStyle(cellStyle);
            firstHeaderCell.setCellValue(new HSSFRichTextString("ID"));
            HSSFCell secondHeaderCell = headerRow.createCell(column++);
            secondHeaderCell.setCellStyle(cellStyle);
            secondHeaderCell.setCellValue(new HSSFRichTextString("PERNR"));
            HSSFCell divCell = headerRow.createCell(column++);
            divCell.setCellStyle(cellStyle);
            divCell.setCellValue(new HSSFRichTextString("DIV"));
            HSSFCell thirdHeaderCell = headerRow.createCell(column++);
            thirdHeaderCell.setCellStyle(cellStyle);
            thirdHeaderCell.setCellValue(new HSSFRichTextString("ESTADO"));
            HSSFCell fourthHeaderCell = headerRow.createCell(column++);
            fourthHeaderCell.setCellStyle(cellStyle);
            fourthHeaderCell.setCellValue(new HSSFRichTextString("Captura"));
            
            HSSFCell fechaFinCell = headerRow.createCell(column++);
            fechaFinCell.setCellStyle(cellStyle);
            fechaFinCell.setCellValue(new HSSFRichTextString("Cantidad"));
            HSSFCell horaIniCell = headerRow.createCell(column++);
            horaIniCell.setCellStyle(cellStyle);
            horaIniCell.setCellValue(new HSSFRichTextString("Reclamado PERNR"));
            HSSFCell horaFinCell = headerRow.createCell(column++);
            horaFinCell.setCellStyle(cellStyle);
            horaFinCell.setCellValue(new HSSFRichTextString("Reclamado por"));
            HSSFCell ccCell = headerRow.createCell(column++);
            ccCell.setCellStyle(cellStyle);
            ccCell.setCellValue(new HSSFRichTextString("Tipo"));

            
            HSSFCell registradoSiboCell = headerRow.createCell(column++);
            registradoSiboCell.setCellStyle(cellStyle);
            registradoSiboCell.setCellValue(new HSSFRichTextString("Registrado SIBO"));
            HSSFCell fifthHeaderCell = headerRow.createCell(column++);
            fifthHeaderCell.setCellStyle(cellStyle);
            fifthHeaderCell.setCellValue(new HSSFRichTextString("Subida a SAP"));
            HSSFCell sixthHeaderCell = headerRow.createCell(column++);
            sixthHeaderCell.setCellStyle(cellStyle);
            sixthHeaderCell.setCellValue(new HSSFRichTextString("Intentos de proceso"));
            HSSFCell operadorCell = headerRow.createCell(column++);
            operadorCell.setCellStyle(cellStyle);
            operadorCell.setCellValue(new HSSFRichTextString("Operador"));
            HSSFCell subCell = headerRow.createCell(column++);
            subCell.setCellStyle(cellStyle);
            subCell.setCellValue(new HSSFRichTextString("Subdivisión"));
            HSSFCell seventhHeaderCell = headerRow.createCell(column++);
            seventhHeaderCell.setCellStyle(cellStyle);
            seventhHeaderCell.setCellValue(new HSSFRichTextString("Descripción"));
            
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");            
            
            // Set the cell value for all the data rows.
  
            for ( Registro registro : regs ) 
            {
                 int columnValue = 0;
                 NovedadPaquete reg = (NovedadPaquete) registro;
                 HSSFRow dataRow = sampleDataSheet.createRow(row++);
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( String.valueOf(reg.getId())));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( reg.getPernr() ) );
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( TimesoftManager.getNombres(reg)));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(reg.getSubdivision()));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( reg.getRetorno()));
                 
                 if ( reg.getFechaCaptura() != null )
                     dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(df.format(reg.getFechaCaptura())));
                 else
                     dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(""));
                 
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(String.valueOf(reg.getIcantidad())));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(reg.getCodreclama()));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(reg.getNombreeclama()));
                 
                 String tipoStr = "";
                 if ( reg.getItipo() != null )
                 {
                     if ( reg.getItipo().equals( timesoft.Constantes.CASINO ))
                         tipoStr =  "CASINO";
                     else if ( reg.getItipo().equals( timesoft.Constantes.BOLSA_DE_PAPEL))
                         tipoStr =  "BOLSA";
                     else
                         tipoStr = "";
                 }
                 
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(tipoStr));
                 
                 if ( reg.getFechacrea() != null )
                     dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( df.format(reg.getFechacrea())));
                 else
                     dataRow.createCell(columnValue).setCellValue(new HSSFRichTextString( ""));
  
                 if ( reg.getCntrl() != null )
                     dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( df.format(reg.getCntrl())));
                 else 
                     dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( "" ));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( reg.getCntdr() ) );
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(reg.getOperador()));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString(reg.getBtrtl()));
                 dataRow.createCell(columnValue++).setCellValue(new HSSFRichTextString( reg.getMensaje()));
                
            }
            
            
            fileOutputStream = new FileOutputStream(filename);
            sampleWorkbook.write(fileOutputStream);
            
            JOptionPane.showMessageDialog(
            centerPanel,
            "Archivo " + filename + " guardado con éxito",
            "Familia",
            JOptionPane.INFORMATION_MESSAGE );       
            
        } 
        catch (Exception ex) 
        {
            statusMessageLabel.setText(ex.getMessage());
        } 
        finally 
        {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }        
    }
    

    /**
     * This method is used to set the styles for all the headers
     * of the excel sheet.
     * @param sampleWorkBook - Name of the workbook.
     * @return cellStyle - Styles for the Header data of Excel sheet.
     */
    private HSSFCellStyle setHeaderStyle(HSSFWorkbook sampleWorkBook) {
        HSSFFont font = sampleWorkBook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setColor(IndexedColors.PLUM.getIndex());
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        HSSFCellStyle cellStyle = sampleWorkBook.createCellStyle();
        cellStyle.setFont(font);
        return cellStyle;
    }

      
    @Action
    private Task buscarMarcaciones() {
        try
        {
            Criterios criterios = this.obtenerCriteriosMarcaciones();
            return new PantLogMarcacionesTask(getApplication(),criterios,paginaMarcaciones);
        }
        catch ( Exception ex )
        {
            log.error("Error al buscar marcaciones", ex);
            statusMessageLabel.setText(ex.getMessage());
            return null;
        }
    }
    
    // Paginar marcaciones
    @Action
    public Task avanzarPaginaMarcaciones() {
        paginaMarcaciones++;
        return buscarMarcaciones();
    }

    @Action
    public Task retrocederPaginaMarcaciones() {
        paginaMarcaciones--;
        return buscarMarcaciones();
    }
    
    // Paginar Novedades
    @Action
    public Task avanzarPaginaNovedades() {
        paginaNovedades++;
        return buscarNovedades();
    }

    @Action
    public Task retrocederPaginaNovedades() {
        paginaNovedades--;
        return buscarNovedades();
    }    

    @Action 
    public Task avanzarPaginaPaquete() {
        paginaPaquete++;
        return buscarPaquete();
    }
    
    @Action
    public Task retrocederPaginaPaquete() {
        paginaPaquete--;
        return buscarPaquete();
    }


    
    @Action
    public Task avanzarPaginaHistorial() {
        paginaHistorial++;
        return buscarIntentos();
    }
    
    @Action
    public Task retrocederPaginaHistorial() {
        paginaHistorial--;
        return buscarIntentos();
    }


    private void showIrapagDialog() {
        irapag.setSize(187,123);
        irapag.setLocationRelativeTo(panelLogMarcaciones);        
        irapag.setVisible(true);        
    }
    
    @Action
    public void showAsignarPlantaDialog() {
        
        List<String> listaSubdivisionesCorreo = new ArrayList<String>();
        for ( StringItem div : tm.getDivisiones() )
            listaSubdivisionesCorreo.add(div.getValue());
              
        Set<String> todasSubs = new LinkedHashSet<String>(listaSubdivisionesCorreo);
        
        Usuario selUsr = (Usuario) listUsuarios.getSelectedValue();        
        selUsr = tm.getUsuario(selUsr.getLogin());
        Set<String> subsDelUsuario = new LinkedHashSet<String>();
        for( UsuarioPlanta up : selUsr.getPlantas() )
            subsDelUsuario.add( up.getPk().getPlanta() );
        
        Set<String> subsSinAsignar = new LinkedHashSet<String>(todasSubs);
        subsSinAsignar.removeAll(subsDelUsuario);
        
        DefaultComboBoxModel subdivisionesAsignar = new DefaultComboBoxModel();
        for ( String sub : subsSinAsignar )
            subdivisionesAsignar.addElement(tm.getItem(sub));
        
        this.comboPlantasAsignar.setModel(subdivisionesAsignar);
        
        asignarPlanta.setSize( 275, 124 );
        asignarPlanta.setLocationRelativeTo(editarUsuarioPanel);
        asignarPlanta.setVisible(true);
    }
    
    @Action
    public void irapagMarcaciones() {
        irapagMode = timesoft.Constantes.MARCACIONES;
        showIrapagDialog();
    }

    @Action
    public void cerrarIrapag() {
        irapag.setVisible(false);
    }

    @Action
    public Task goToPage() {
        
        irapag.setVisible(false);
        
        int nuevaPagina = 0;
        
        try
        {
            nuevaPagina = Integer.parseInt(irapagTextField.getText()) - 1;
        }
        catch ( Exception ex ) 
        {
            irapagTextField.setText("");
            return null;
        }

        switch( irapagMode ) 
        {
            case timesoft.Constantes.MARCACIONES:
                paginaMarcaciones = nuevaPagina;
                return buscarMarcaciones();
            case timesoft.Constantes.NOVEDADES:
                paginaNovedades = nuevaPagina;
                return buscarNovedades();
            case timesoft.Constantes.NOVEDADES_PAQUETE:
                paginaPaquete = nuevaPagina;
                return buscarPaquete();
            case HISTORIAL_MODE:
                paginaHistorial = nuevaPagina;
                return buscarIntentos();
        }
        
        return null;
        
    }

    @Action
    public void irapagNovedades() {
        irapagMode = timesoft.Constantes.NOVEDADES;
        showIrapagDialog();        
    }

    @Action
    public void irapagPaquete() {
        irapagMode = timesoft.Constantes.NOVEDADES_PAQUETE;
        showIrapagDialog();           
    }

    @Action
    public void irapagHistorial() {
        irapagMode = HISTORIAL_MODE;
        showIrapagDialog();            
    }

    @Action
    public void editarUsuario() {
        
        this.checkListaPlantas();

        Usuario selUsr = (Usuario) this.listUsuarios.getSelectedValue();

        if( selUsr != null ) {
            
            this.labelNombreUsuarioEditar.setText( selUsr.getLogin() );
            this.labelRolUsuarioEditar.setText( selUsr.getRol() );

            
  

            // actualizar el usuario
            selUsr = tm.getUsuario(selUsr.getLogin());
            
            // Actualizar el listado de plantas
            List<UsuarioPlanta> plantas = selUsr.getPlantas();

            DefaultListModel plantasListModel = new DefaultListModel();
            for ( UsuarioPlanta up : plantas )
                plantasListModel.addElement(new StringItem(up.getPk().getPlanta(), up.getPk().getPlanta()));

            this.listPlantasUsuario.setModel( plantasListModel );
            listPlantasUsuario.setSelectionMode( ListSelectionModel.SINGLE_SELECTION);            
            
            
            if ( this.esAdministrador(selUsr) )
            {
                this.listasPlantasScrollPanel.setVisible(false);
                this.botonAgregarPlanta.setVisible(false);
                this.botonEliminarPlanta.setVisible(false);
                labelPlantasTodas.setVisible(true);
            }
            else 
            {
                this.listasPlantasScrollPanel.setVisible(true);
                this.botonAgregarPlanta.setVisible(true);
                this.botonEliminarPlanta.setVisible(true);
                this.labelPlantasTodas.setVisible(false);
            }            

            showPanel( editarUsuarioPanel );
            
        }
        
    }

    @Action
    public void cerrarAsignarPlanta() {
        asignarPlanta.setVisible(false);
        this.editarUsuario();
    }

    @Action
    public void asignarPlantaAUsuario() {
        StringItem plantaSelected = (StringItem) comboPlantasAsignar.getSelectedItem();
        
        if ( plantaSelected != null ) 
        {
            Usuario selUsr = (Usuario) this.listUsuarios.getSelectedValue();   
            tm.agregarPlanta(selUsr.getLogin(), plantaSelected.getValue() );
        }
        
        cerrarAsignarPlanta();
    }

    @Action
    public void eliminarPlanta() {
        
        
        int selected = this.listPlantasUsuario.getSelectedIndex();
        if ( selected < 0 )
              return;
        StringItem plantaSelected = (StringItem) this.listPlantasUsuario.getSelectedValue();
        Usuario selUsr = (Usuario) this.listUsuarios.getSelectedValue(); 
        String planta = plantaSelected.getValue();
        
        int n = JOptionPane.showConfirmDialog(
                    mainPanel,
                    "¿Desea deasignar la planta " + planta + "?",
                    "Desasignar planta",
                    JOptionPane.YES_NO_OPTION);        
        
        if ( n == 0 )
        {
            UsuarioPlanta up = new UsuarioPlanta(selUsr.getLogin(), planta );
            tm.desasignarPlanta(up);
        }
        
        this.editarUsuario();
                
    }

    @Action
    public void descargarMaestro() throws BadLocationException {
         int row = tablaMaestros.getSelectedRow();
         if ( row < 0 )
             return;
         
         DefaultTableModel tableModel = (DefaultTableModel) tablaMaestros.getModel();
         Maestro m = (Maestro) tableModel.getDataVector().get(row);
         String mensaje = "";
         
         if ( tm.existeMaestro(m.getPernr()) )
         {
             mensaje = "Ya existe el registro";
             JOptionPane.showMessageDialog(
             listaMaestrosPanel,
             mensaje,
             "Familia",
             JOptionPane.INFORMATION_MESSAGE );                
         }
         else 
         {
             try 
             {
                 tm.descargarMaestro(m);
                 mensaje = "Registro descargado con éxito";
                 m.setEstado("S");
                 tablaMaestros.repaint();                 
                 JOptionPane.showMessageDialog(
                 listaMaestrosPanel,
                 mensaje,
                 "Familia",
                 JOptionPane.INFORMATION_MESSAGE );                    
                 
             }
             catch ( Exception ex )
             {
                 String stackTrace = TimesoftManager.stackTraceString(ex);
                 mensaje = "Error al descargar el registro: " + stackTrace;
                 Document d = areaErrorDetallado.getDocument();
                 d.remove(0, d.getLength());
                 d.insertString(0, mensaje, null);
                 errorDetallado.setSize(390,340);
                 errorDetallado.setLocationRelativeTo(centerPanel);        
                 errorDetallado.setVisible(true);         
                 return;
             }             
         }
         
        

    }

    @Action
    public void cerrarErrorDetallado() {
        errorDetallado.setVisible(false);
    }

    @Action
    public void asignarContrasena() {
        nuevaContrasena.setSize(219,140);
        nuevaContrasena.setLocationRelativeTo(centerPanel);
        nuevaContrasena.setVisible(true);
    }

    @Action
    public void cerrarNuevaContrasena() {
        nuevaContrasena.setVisible(false);
    }

    @Action
    public void okNuevaContrasena() {
        
        String nueva1 = nuevaContrasenaText.getText();
        String errorMessage = null;
        
        if ( nueva1.length() >= 6 )
        {
            if ( nueva1.matches("\\S+") )
            {
                Usuario selUsr = (Usuario) this.listUsuarios.getSelectedValue();
                selUsr.setPassword(nueva1);
                tm.updateUsuario(selUsr);
            }
            else
            {
                errorMessage = "La contraseña nueva no debe tener espacios.";
            }
        }
        else
        {
            errorMessage = "La contraseña nueva debe tener mínimo 6 letras.";
        }        
        
        if (errorMessage != null)
        {
            JOptionPane.showMessageDialog(
            nuevaContrasena,
            errorMessage,
            "Familia",
            JOptionPane.ERROR_MESSAGE );            
        }
        else
        {
            JOptionPane.showMessageDialog(
            nuevaContrasena,
            "Contraseña asignada con éxito",
            "Familia",
            JOptionPane.INFORMATION_MESSAGE );       
            nuevaContrasena.setVisible(false);
            
        }
    }

}

