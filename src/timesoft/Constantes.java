package timesoft;

import com.inga.utils.Fecha;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Constantes
{
  public static final String EXITO = "S";
  public static final String ERROR = "E";
  public static final int BATCH_SIZE_LARGE = 5;
  public static final int BATCH_SIZE_SMALL = 5;
  public static final String SUBDIVISION_GENERAL = "GENERAL";
  public static final String VERSION = "8";
  public static final String VERSION_FECHA = "Nov 26, 2010";
  public static final String NUEVA = "nueva";
  public static final String OTROS_MENSAJES = "_otros_mensajes_";
  public static final int TOP = 5000;
  public static final int MAX_SIZE = 999999;
  public static final int MESSAGE_MAX_LENGTH = 249;
  public static final String CASINO = "2T21";
  public static final String BOLSA_DE_PAPEL = "2T13";
  public static final int MAX_ERRORES = Integer.MAX_VALUE;
  public static final int STEP = 50;
  public static final long JAN011900 = -2208971020000L;
  public static final String DATUM_SAP_FORMAT = "yyyyMMdd";
  public static final String UHR_SAP_FORMAT = "HHmmss";
  public static final String STANDARD_DATE = "dd/MM/yyyy";
  public static final int MAESTROS = 1;
  public static final int CENTROSCOSTO = 2;
  public static byte[] KEY = "ASD23G23G2365264GEGWEGWE_2323ETRE00235AGEGEW".getBytes();
  public static final int DIAS_ATRAS = 1;
  public static final Fecha FECHA_1950_01_01 = new Fecha(1, 1, 1950);
  public static final Fecha FECHA_LIMITE_VIEJAS = new Fecha(1, 1, 2000);
  public static final String FUNCION_MARCACIONES = "ZFNHRPT_TBMARCACIONES";
  public static final String FUNCION_NOVEDADES = "ZFNHRPT_TBNOVEDADES";
  public static final String FUNCION_NOVEDADES_PAQUETE = "ZFNHRPT_TBBOLCAS";
  public static final String FUNCION_MAESTROS = "Z_HREC_PT_TBMAESTRO";
  public static final int SINCRONIZAR_MAESTROS = 1;
  public static final int SINCRONIZAR_CENTROS_COSTO = 2;
  public static final int MARCACIONES = 3;
  public static final int NOVEDADES = 4;
  public static final int NOVEDADES_PAQUETE = 5;
  public static final int CORREO = 6;
  public static final int ADMIN_USUARIOS = 7;
  public static final int PREVALIDAR_MARCACIONES = 8;
  public static final SimpleDateFormat _df2 = new SimpleDateFormat("yyyyMMdd");
  public static final SimpleDateFormat dfSibo = new SimpleDateFormat("yyyyMMddHHmmss");
  
  public static final String MATCH_REGISTRO_YA_EXISTE = ".*REGISTRO\\s+YA\\s+EXISTE\\s+PARA\\s+LA\\s+FECHA.*";
  public static final String REGISTRO_YA_EXISTE = "%Registro ya existe para la Fecha%";
  public static final String NO_SE_ENCUENTRA_ACTIVO = "%NO se encuentra Activo para la Fecha%";
  public static final String FECHA_RETROACTIVA_MAS_ANTIGUA = "%Modif.antes fe.retroactiv.más antigua%";
  public static final String NO_TIENE_REGISTRO_INFOTIPO = "%NO tiene Registro del % para la Fecha %";
  public static final String NO_SE_CREO_REGISTRO = "%NO se creo registro para la Fecha %";
  public static final String USUARIO_ESTA_TRATANDO_LA_PERSONA = "%El usuario % está tratando la persona%";
  public static final String AREA_BLOQUEADA_PARA_ACTUALIZACION = "%Área % bloqueada%";
  public static final String CAMPOS_FALTANTES = "%Complete%";
  public static final String NO_SE_HA_CREADO_REGISTRO_DE_GESTION = "%No se ha creado registro%";  
  public static final String NO_HAY_REPUESTA_DEL_SERVIDOR = "No hay respuesta del servidor";
  
  public static final Set<String> CODIGOS_DE_ERROR ;
  
  static 
  {
      Set<String> errores = new LinkedHashSet<String>();
      errores.add(REGISTRO_YA_EXISTE);
      errores.add(NO_SE_ENCUENTRA_ACTIVO);
      errores.add(FECHA_RETROACTIVA_MAS_ANTIGUA);
      errores.add(NO_TIENE_REGISTRO_INFOTIPO);
      errores.add(NO_SE_CREO_REGISTRO);
      errores.add(USUARIO_ESTA_TRATANDO_LA_PERSONA);
      errores.add(AREA_BLOQUEADA_PARA_ACTUALIZACION);
      errores.add(CAMPOS_FALTANTES);
      errores.add(NO_SE_HA_CREADO_REGISTRO_DE_GESTION);
      CODIGOS_DE_ERROR = Collections.unmodifiableSet(errores);
  }

  public static String getEstadoIntento(Character estado) {
    if (estado != null)
    {
      if (estado.equals(Character.valueOf('E')))
        return "ERROR";
      if (estado.equals(Character.valueOf('I')))
        return "INCOMPLETO";
      if (estado.equals(Character.valueOf('S'))) {
        return "OK";
      }
      return "";
    }

    return "";
  }

  public static String getActividad(Integer tipo) {
    if (tipo != null)
    {
      switch (tipo.intValue())
      {
      case 1:
        return "MAESTROS";
      case 2:
        return "C.COSTO";
      case 3:
        return "MARCACIONES";
      case 4:
        return "NOVEDADES";
      case 5:
        return "PAQUETE";
      case 6:
        return "CORREO";
      case 7:
        return "ADMIN_USUARIOS";
      case 8:
        return "PRE-VALIDACION";
      }
      return "";
    }

    return "";
  }
}