/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timesoft.dao.impl;

import com.inga.utils.StringItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import timesoft.dao.DivisionDAO;

/**
 *
 * @author Camilo
 */
@Repository
public class DivisionMap implements DivisionDAO {
    
    private static final List<StringItem> DIVISIONES;
    
    private static final Map<StringItem,List<StringItem>> SUBDIVISIONES;

    public static final StringItem DIV_BALSOS = new StringItem("Balsos","BALS");
    public static final StringItem DIV_BOGOTA = new StringItem("Distrito Bogotá","DBOG");
    public static final StringItem DIV_BARRANQUILLA = new StringItem("Distrito Barranquilla","DBQL");
    public static final StringItem DIV_BUCARAMANGA = new StringItem("Distrito Bucaramanga","DBUC");
    public static final StringItem DIV_CALI = new StringItem("Distrito Cali","DCAL");
    public static final StringItem DIV_METAB = new StringItem("Distrito Meta/Boyacá","DMEB");
    public static final StringItem DIV_DMED = new StringItem("Distrito Medellín","DMED");
    public static final StringItem DIV_PER = new StringItem("Distrito Pereira","DPER");
    public static final StringItem DIV_TOLIM = new StringItem("Distrito Tolima/Huila","DTHU");
    public static final StringItem DIV_ECUADOR =new StringItem("Ecuador","ECUA");
    public static final StringItem DIV_GIR = new StringItem("Logística Girardota","LGIR");
    public static final StringItem DIV_LITA = new StringItem("Localidad Itagüí","LITA");
    public static final StringItem PCAJ = new StringItem("Planta Cajicá","PCAJ");
    public static final StringItem PCZF = new StringItem("Planta Cajicá Zona Franca","PCZF");
    public static final StringItem PMAT = new StringItem("Planta Materiales","PMAT");
    public static final StringItem PMED = new StringItem("Planta Medellín","PMED");
    public static final StringItem PPAN = new StringItem("Planta Pañales","PPAN");
    public static final StringItem PRIO = new StringItem("Planta Rionegro","PRIO");
    public static final StringItem REDO = new StringItem("República Dominicana","REDO");
    public static final StringItem SORE = new StringItem("Soresa","SORE");
        
    public static final StringItem ADMI = new StringItem("Administrativo","ADMI");
    public static final StringItem COCI = new StringItem("Cocineta","COCI");
    public static final StringItem SEGU = new StringItem("Seguridad","SEGU");
    public static final StringItem CAUT = new StringItem("Canal Autoservi","CAUT");
    public static final StringItem CTRA = new StringItem("Canal Tradición","CTRA");
    public static final StringItem VTMD = new StringItem("Ventas/Mercadeo","VTMD");
    public static final StringItem LOGI = new StringItem("Logística","LOGI");
    public static final StringItem VENT = new StringItem("Ventas","VENT");
    public static final StringItem ALMA = new StringItem("Almacén","ALMA");
    public static final StringItem CONV = new StringItem("Conversión","CONV");
    public static final StringItem DTEC = new StringItem("División Técnica","DTEC");
    public static final StringItem GCAL = new StringItem("Gestión Calidad","GCAL");
    public static final StringItem MCON = new StringItem("Mant. Conversión","MCON");
    public static final StringItem MELE = new StringItem("Mant. Electrónico","MELE");
    public static final StringItem MMOL = new StringItem("Mant. Molino","MMOL");
    public static final StringItem MOLI = new StringItem("Molino","MOLI");
    public static final StringItem PAGU = new StringItem("Planta de aguas","PAGU");
    public static final StringItem SGEN = new StringItem("Serv. Generales","SGEN");
    public static final StringItem SOCU = new StringItem("Salud ocupacional","SOCU");
    public static final StringItem DPRY = new StringItem("Desar. Proyectos","DPRY");
    public static final StringItem MANU = new StringItem("Manufactura","MANU");
    public static final StringItem MMEC = new StringItem("Mant. Mecánico","MMEC");
    public static final StringItem CALD = new StringItem("Calderas","CALD");
    public static final StringItem MPRI = new StringItem("Materias Primas","MPRI");
    public static final StringItem PHUM = new StringItem("Paños húmedos","PHUM");
    public static final StringItem SALI = new StringItem("Serv. Alimentaci.","SALI");
        
    static 
    {
        

        
        
        List<StringItem> divisiones = new ArrayList<StringItem>();
        divisiones.add(DIV_BALSOS);
        divisiones.add(DIV_BOGOTA);
        divisiones.add(DIV_BARRANQUILLA);
        divisiones.add(DIV_BUCARAMANGA);
        divisiones.add(DIV_CALI);
        divisiones.add(DIV_METAB);
        divisiones.add(DIV_DMED);
        divisiones.add(DIV_PER);
        divisiones.add(DIV_TOLIM);
        divisiones.add(DIV_ECUADOR);
        divisiones.add(DIV_GIR);
        divisiones.add(DIV_LITA);
        divisiones.add(PCAJ);
        divisiones.add(PCZF);
        divisiones.add(PMAT);
        divisiones.add(PMED);
        divisiones.add(PPAN);
        divisiones.add(PRIO);
        divisiones.add(REDO);
        divisiones.add(SORE);
        
        DIVISIONES = Collections.unmodifiableList(divisiones);
        
        Map<StringItem,List<StringItem>> subdivisiones = new LinkedHashMap<StringItem,List<StringItem>>();
        List<StringItem> subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(COCI);
        subs.add(SEGU);
        subdivisiones.put(DIV_BALSOS,subs);
        
        subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(CAUT);
        subs.add(COCI);
        subs.add(CTRA);
        subs.add(VTMD);
        subdivisiones.put(DIV_BOGOTA,subs);

        subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(CAUT);
        subs.add(COCI);
        subs.add(CTRA);
        subs.add(VTMD);
        subdivisiones.put(DIV_BARRANQUILLA,subs);
        
        subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(CAUT);
        subs.add(COCI);
        subs.add(CTRA);
        subs.add(VTMD);
        subdivisiones.put(DIV_BUCARAMANGA,subs);
        
        subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(CAUT);
        subs.add(COCI);
        subs.add(CTRA);
        subs.add(LOGI);
        subs.add(VTMD);
        subdivisiones.put(DIV_CALI,subs);
        
        subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(CAUT);
        subs.add(COCI);
        subs.add(CTRA);
        subs.add(VTMD);
        subdivisiones.put(DIV_METAB,subs);
        
        subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(CAUT);
        subs.add(COCI);
        subs.add(CTRA);
        subs.add(VTMD);
        subdivisiones.put(DIV_DMED,subs);
        
        subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(CAUT);
        subs.add(COCI);
        subs.add(CTRA);
        subs.add(VTMD);
        subdivisiones.put(DIV_PER,subs);
        
        subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(CAUT);
        subs.add(COCI);
        subs.add(CTRA);
        subs.add(VTMD);
        subdivisiones.put(DIV_TOLIM,subs);
        
        subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(VENT);
        subdivisiones.put(DIV_ECUADOR,subs);
        
        subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(COCI);
        subs.add(LOGI);
        subs.add(SEGU);
        subdivisiones.put(DIV_GIR,subs);
        
        subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(COCI);
        subs.add(LOGI);
        subs.add(SEGU);
        subdivisiones.put(DIV_LITA,subs);
        
        subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(ALMA);
        subs.add(COCI);
        subs.add(CONV);
        subs.add(DTEC);
        subs.add(GCAL);
        subs.add(LOGI);
        subs.add(MCON);
        subs.add(MELE);
        subs.add(MMOL);
        subs.add(MOLI);
        subs.add(PAGU);
        subs.add(SEGU);
        subs.add(SGEN);
        subs.add(SOCU);
        subdivisiones.put(PCAJ,subs);
        
        subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(ALMA);
        subs.add(COCI);
        subs.add(CONV);
        subs.add(DTEC);
        subs.add(GCAL);
        subs.add(LOGI);
        subs.add(MCON);
        subs.add(MELE);
        subs.add(MMOL);
        subs.add(MOLI);
        subs.add(PAGU);
        subs.add(SEGU);
        subs.add(SGEN);
        subs.add(SOCU);
        subdivisiones.put(PCZF,subs);
        
        subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(ALMA);
        subs.add(COCI);
        subs.add(DPRY);
        subs.add(GCAL);
        subs.add(LOGI);
        subs.add(MANU);
        subs.add(MELE);
        subs.add(MMEC);
        subs.add(SEGU);
        subs.add(SGEN);
        subs.add(SOCU);
        subdivisiones.put(PMAT,subs);
        
        subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(ALMA);
        subs.add(CALD);
        subs.add(COCI);
        subs.add(CONV);
        subs.add(DTEC);
        subs.add(GCAL);
        subs.add(LOGI);
        subs.add(MCON);
        subs.add(MELE);
        subs.add(MMOL);
        subs.add(MOLI);
        subs.add(MPRI);
        subs.add(PAGU);
        subs.add(PHUM);
        subs.add(SALI);
        subs.add(SEGU);
        subs.add(SGEN);
        subs.add(SOCU);
        subdivisiones.put(PMED,subs);
        
        subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(ALMA);
        subs.add(COCI);
        subs.add(DTEC);
        subs.add(GCAL);
        subs.add(LOGI);
        subs.add(MANU);
        subs.add(MELE);
        subs.add(MMEC);
        subs.add(SEGU);
        subs.add(SGEN);
        subs.add(SOCU);
        subdivisiones.put(PPAN,subs);
        
        subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(ALMA);
        subs.add(COCI);
        subs.add(DPRY);
        subs.add(GCAL);
        subs.add(LOGI);
        subs.add(MANU);
        subs.add(MELE);
        subs.add(MMEC);
        subs.add(SALI);
        subs.add(SEGU);
        subs.add(SGEN);
        subs.add(SOCU);
        subdivisiones.put(PRIO,subs);
        
        subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(VENT);
        subdivisiones.put(REDO,subs);
        
        subs = new ArrayList<StringItem>();
        subs.add(ADMI);
        subs.add(COCI);
        subs.add(MPRI);
        subs.add(SEGU);
        subdivisiones.put(SORE,subs);
       
        SUBDIVISIONES = Collections.unmodifiableMap(subdivisiones);

    }

    public List<StringItem> getDivisiones() {
        return DIVISIONES;
    }

    public List<StringItem> getSubdivisiones(StringItem division) {
        return SUBDIVISIONES.get(division);
    }
    
    public StringItem getItem(String value) {
        for ( StringItem item : getDivisiones() )
        {
            if ( item.getValue().equals(value) )
                return item;
            
            for ( StringItem sub : getSubdivisiones(item) )
            {
                if ( sub.getValue().equals(value) )
                    return sub;
            }
        }
        return null;
    }

    
}
