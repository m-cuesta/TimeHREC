/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timesoft.model;

/**
 *
 * @author Usuario
 */
public class Propiedad {

    private String label;
    private String name;
    private String value;

    public Propiedad(String label,String name,String value) {
        this.label = label;
        this.name = name;
        this.value = value;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

}
