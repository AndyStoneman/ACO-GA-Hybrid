import java.util.Comparator;
import java.lang.Math;

/**
 * Used to compare ants based on their distance. 
 */
public class AntComparator implements Comparator<Ant> {
    
    /**
     * Compare method used to determine which fitness is larger.
     * 
     * @return
     *      Integer that determines which is larger for comparator method in Population class. 
     */
    public int compare(Ant a, Ant b) {
        long a1 = Math.round(a.getCurDistance());
        long b1 = Math.round(b.getCurDistance());
        return ((int) a1) - ((int) b1);
    }
}