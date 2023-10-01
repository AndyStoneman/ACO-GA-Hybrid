import java.util.ArrayList;
import java.util.Random;

/**
 * Class used to create an ant object, which represents an ant in ant colony optimization. For TSP the ant 
 * object moves through a tour, tracking the cities its visited. It keeps track of its best tour, and 
 * ideally finds increasingly better tours.
 */
public class Ant {
    /**
     * Random object that is used throughout the class, so it was made global. 
     */
    private static final Random RANDOM_OBJECT = new Random();
    /**
     * Represents the current city that the ant is located in. Reset every time it moves to new city. 
     */
    private int curCity; 
    /**
     * The distance that the ant has accumulated during its tour. Reset every iteration.  
     */
    private double curDistance;
    /**
     * The best tour that the ant has experienced across all of its iterations. 
     */
    private double bestTour;
    /**
     * The number of cities in the particular TSP problem. 
     */
    private int numCities;
    /**
     * The first city the ant visited. Used to add the final distance of the leg from last city to first city. 
     */
    private int firstCity;
    /**
     * List used to keep track of the cities that the ant visited. Reset every iteration. 
     */
    private ArrayList<Integer> curTour = new ArrayList<Integer>();

    /**
     * Constructor for objects of class Ant. Sets the number of cities and the best tour to a default value.
     */
    public Ant(int numCities) {
        // initialise instance variables
        this.numCities = numCities;
        bestTour = Integer.MAX_VALUE;
        for (int i = 0; i < numCities; i++) {
            curTour.add(i, -1);
        }
    }
    
    /**
     * Resets the ant so that it can store new values for its next iteration and not worry about the ones 
     * from the previous. 
     */
    public void resetAnt() {
        curTour.clear();
        firstCity = RANDOM_OBJECT.nextInt(numCities);
        curCity = firstCity;
        curTour.add(curCity);
        curDistance = 0.0;
    }
    
    /**
     * Checks to see if the ant's current distance is better than its current best tour. If it is, it sets
     * this distance to the new best tour value. 
     */
    public void checkBestTour() {
        if (this.curDistance < this.bestTour) {
            bestTour = this.curDistance;
        }
    }
    
    /**
     * Getter method to return the list of cities the ant has visited in this iteration. 
     * 
     * @return
     *      The list of cities the ant has visited. 
     */
    public ArrayList<Integer> getCurTour() {
        return curTour;
    }
    
    public void setTour(int position, int value) {
        this.curTour.set(position, value);
    }
    
    public void calculateDistance(double[][] distances) {
        for (int i = 0; i < numCities - 1; i++) {
            curDistance += distances[this.getCurTour().get(i)][this.getCurTour().get(i + 1)];
        }
        curDistance += distances[this.getCurTour().get(numCities - 1)][this.getCurTour().get(0)];
    }
    
    /**
     * Getter method to return the first city the ant started in. 
     * 
     * @return
     *      The first city the ant started in. 
     */
    public int getFirstCity() {
        return firstCity;
    }
    
    /**
     * Getter method used to return the current city the ant is in.
     * 
     * @return
     *      The current city the ant is in. 
     */
    public int getCity() {
        return curCity;
    }
    
    /**
     * Setter/Update method that updates the current city the ant is at for when it moves to a new one. Sets
     * the current city to the new one and adds the new one to the list of cities its visited. 
     * 
     * @param newCity
     *      The new city that ant moved to. 
     */
    public void updateCity(int newCity) {
        curCity = newCity;
        curTour.add(newCity);
    }
    
    /**
     * Getter method to return the current distance the ant has traveled in its tour. 
     * 
     * @return
     *      The distance the ant has traveled at a certain point in the tour. 
     */
    public double getCurDistance() {
        return curDistance;
    }
    
    /**
     * Setter/Update method for adding the distance of a new city to the current distance when moving. 
     * 
     * @param newDistance
     *      The new distance that we want to add to the ants current distance. 
     */
    public void updateCurDistance(double newDistance) {
        curDistance += newDistance;
        //System.out.println(num + " " + newDistance);
    }
    
    /**
     * Getter method that returns the best tour an ant has seen over its iterations.
     * 
     * @return
     *      The best tour for the ant. 
     */
    public double getBestTour() {
        return bestTour;
    }
}
