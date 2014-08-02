/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timesoft.model;

/**
 *
 * @author Usuario
 */
public class IdRange {

    private Integer low;
    private Integer high;

    public IdRange( Integer pLow, Integer pHigh ) {
        low = pLow;
        high = pHigh;
    }

    /**
     * @return the low
     */
    public Integer getLow() {
        return low;
    }

    /**
     * @param low the low to set
     */
    public void setLow(Integer low) {
        this.low = low;
    }

    /**
     * @return the high
     */
    public Integer getHigh() {
        return high;
    }

    /**
     * @param high the high to set
     */
    public void setHigh(Integer high) {
        this.high = high;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if ( low != null )
           sb.append( low );
        else 
           sb.append( "?" );
        if ( high != null ) 
        {
            sb.append( "-" );
            sb.append( high );
        }
        else
            sb.append( "?");
        return sb.toString();
    }
            
          

}
