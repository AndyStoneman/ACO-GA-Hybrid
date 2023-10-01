import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Random;
import java.util.Timer;

/**
 * Implementation of the basic Ant System algorithm, which is a type of ant colony optimization. In this 
 * system, the next city is determined probabilistically and the pheremone is updated based on a simple
 * update formula. 
 *
 * @author Andy Stoneman, Michael Webber, Alex Clark
 * @version 5/2/2022
 */
public class AntSystem {
    /**
     * A final constant variable that determines the importance of the distance heuristic in calculating
     * probabilities. 
     */
    private static final double BETA = 3.5;
    /**
     * A final constant variable that determines the amount that pheremone evaporates each iteration. 
     */
    private static final double ROW = 0.5;
    /**
     * A final constant variable that determines the importance of the pheremone levels in calculating
     * probabilities. 
     */
    private static final double ALPHA = 1.5;
    /**
     * A final constant the denotes the number of ants that are used in the algorithm. 
     */
    private static final int NUM_ANTS = 20;
    /**
     * A list of lists x and y coordinate positions of all the cities in a problem file. 
     */
    private ArrayList<ArrayList<Double>> cityPositions;
    /**
     * A 2d array to hold the euclidean distances between each city. Mirrored over the diagonal. 
     */
    private double[][] distances;
    /**
     * A 2d array to hold the pheremone levels between each city. Mirrored over the diagonal.
     */
    private double[][] pheremoneLevels;
    /**
     * The number of iterations that will be run for a given experiment.
     */
    private int numIterations;
    /**
     * The most optimal tour for a particular problem. Given as an individual argument. 
     */
    private double optTour;
    /**
     * The number of cities in the problem file. 
     */
    private int numCities;
    /**
     * The name of the file that contains the TSP problem. 
     */
    private String fileName;
    /**
     * Variable used to choose between whether we want to have a set number of iterations or a percentage
     * cutoff for when the best tour crosses. 
     */
    private boolean percentageCutoff;
    /**
     * If we choose to run the algorithm until we hit a target or better, this number is the desired 
     * target. 
     */
    private double targetCutoff;
    /**
     * A 2d array that stores the numerator portion of the probability equation for every leg in the problem
     * which denotes how a city is picked for a given iteration. Updated every iteration when the pheremone 
     * is updated. 
     */
    private double[][] probabilitiesList;
    
    /**
     * Constructor for objects of class ACO. Sets number of cities based on file parsing. 
     */
    public AntSystem(String fileName, double optTour, boolean percentageCutoff, int numIterations, double targetCutoff) {
        this.fileName = fileName;
        Scanner scan = new Scanner(fileName);
        try {
            scan = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            System.out.println("Error occurred: file not found.");
            System.exit(0);
        }
        for (int i = 0; i < 3; i++) {
            scan.nextLine();
        }
        String[] getDimensions = scan.nextLine().split(":");
        this.numCities = Integer.valueOf(getDimensions[1].trim());
        this.optTour = optTour;
        this.percentageCutoff = percentageCutoff;
        if (percentageCutoff) {
            this.numIterations = Integer.MAX_VALUE;
            this.targetCutoff = targetCutoff;
        } else {
            this.numIterations = numIterations;
            this.targetCutoff = targetCutoff;
        }
    }
    
    /**
     * Gets all of the positions from the file so the file does not have to be read more than once. 
     * Allows for euclidean distances to be calculated much more easily. Stores positions in list of lists.
     */
    private void getCityPositions() {
        cityPositions = new ArrayList<ArrayList<Double>>(); //instantiate the list to hold positions
        //get the file information to iterate through
        Scanner scanProblem = new Scanner(this.fileName);
        try {
            scanProblem = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            System.out.println("Error occurred: file not found.");
            System.exit(0);
        }
        
        // skip first lines
        for (int i = 0; i < 6; i++) {
            scanProblem.nextLine();
        }
        
        //iterate through all integers in the file
        for (int i = 0; i < numCities; i++) {
            ArrayList<Double> position = new ArrayList<Double>();
            String coordinate = scanProblem.nextLine().trim();
            String[] components = coordinate.split("\\s+");
            //go until we hit a zero, adding all integers to the clause array
            for (int j = 1; j < 3; j++) {
                position.add(Double.valueOf(components[j].trim()));
            }   
            //add the clause array to the array holding all clauses/entire maxsat problem
            cityPositions.add(position);
        }
    }
    
    /**
     * Based on city positions, calculates all of the euclidean distances, and stores them in a 2d array, 
     * where column and row numbers represent two different cities. The array is mirrored over the 
     * diagonal, and every value in the diagonal is a 0, since the distance between the same city is 0.
     */
    private void getDistances() {
        distances = new double[numCities][numCities];
        for (int i = 0; i < numCities; i++) {
            for (int j = i; j < numCities; j++) {
                double firstPart = Math.pow(cityPositions.get(i).get(0) - cityPositions.get(j).get(0), 2);
                double secondPart = Math.pow(cityPositions.get(i).get(1) - cityPositions.get(j).get(1), 2);
                distances[i][j] = Math.sqrt(firstPart + secondPart);
                distances[j][i] = Math.sqrt(firstPart + secondPart);
            }
        }
    }
    
    /**
     * Creates default values for the pheremone levels before ants have constructed any tours. Levels 
     * are calculated by, starting at a random city and choosing the next closest city based on the 
     * distances just calculated. Once the tour is complete, it takes the number of ants and divides 
     * them by the total distance of the tour. We then set all pheremone levels on every leg to this 
     * default value. Also known as nearest neighbor tour.
     */
    private void initializePheremoneLevels() {
        pheremoneLevels = new double[numCities][numCities];
        ArrayList<Integer> cities = new ArrayList<Integer>();
        for (int i = 0; i < numCities; i++) {
            cities.add(i);
        }
        Random randomCity = new Random();
        int city = randomCity.nextInt(numCities);
        int first = city;
        double total = 0;
        int nextCity = 0;
        int counter = 0;
        while (counter < numCities) {
            double curMin = Integer.MAX_VALUE;
            for (int j = 0; j < cities.size(); j++) {
                //iterate through cities to find shortest distance
                if (distances[city][cities.get(j)] < curMin && distances[city][cities.get(j)] != 0) {
                    curMin = distances[city][cities.get(j)];
                    nextCity = cities.get(j);
                }
                
                //go to next city with shortest distance, and repeat process excluding everything before
            }
            total += curMin;
            cities.remove(cities.indexOf(city));
            city = nextCity;
            counter++;
        }
        total += distances[first][city];
        
        for (int i = 0; i < numCities; i++) {
            for (int j = i; j < numCities; j++) {
                if (i == j) {
                    pheremoneLevels[i][j] = 0.0;
                    pheremoneLevels[j][i] = 0.0;
                } else {
                    pheremoneLevels[i][j] = NUM_ANTS / total;
                    pheremoneLevels[j][i] = NUM_ANTS / total;
                }
            }
        }
    }

    /**
     * This method runs the entire AntSystem process, acting as a blueprint for the entire thing. It calls
     * on helper methods and also performs aspects of the algorithm within it as well. 
     */
    public void solve() {
        System.out.println("\nSTARTING REGULAR ANT COLONY SYSTEM\n");
        System.out.println("Running on file: " + fileName + "\n");
        
        //start timer
        long startTime = System.currentTimeMillis();
        
        //get the problem--positions array and 2d distances array
        getCityPositions();
        getDistances();
        
        //implement a 2d pheremone array that holds a specified start value
        initializePheremoneLevels();
        
        //create collection of ants
        Ant[] antCollection = new Ant[NUM_ANTS];
        for (int i = 0; i < NUM_ANTS; i++) {
            Ant ant = new Ant(numCities);
            antCollection[i] = ant;
        }
        
        //create a variable to check if best tour for an iteration is also best overall 
        double bestOverall = Integer.MAX_VALUE;
        
        //start iterations
        int iteration = 0;
        while (iteration < numIterations) {
            //calculate new probabilities for each leg based on new pheremone every iteration
            calculateProbabilities();
            
            //iterate through ants, creating a tour for each of them
            constructTours(antCollection);
            
            //after each iteration, update pheremone levels and calculate best tour of iteration
            double bestTour = updatePheremoneLevels(antCollection);
            
            if (bestTour < bestOverall) {
                bestOverall = bestTour;
            }
            
            //repeat this process until max # of iterations or percentage cutoff reached 
            System.out.println("Best tour in " + "iteration " + (iteration + 1) + ": " + String.format("%.0f", bestTour));
            
            //if doing percentage cutoff method check to see if best overall was less than cutoff
            if (percentageCutoff) {
                double percentageOver = bestOverall / optTour;
                if (percentageOver < targetCutoff) {
                    System.out.println("\nTarget cutoff " + targetCutoff + "x greater than optimal reached. Occurred at iteration " + (iteration + 1) + ".");
                    break;
                }
            }
            iteration++;
        }
        
        //stop timer
        long stopTime = System.currentTimeMillis();
        
        //print best tour and when it was achieved
        System.out.println("Best overall tour (rounded): " + String.format("%.0f", bestOverall));
        if (!percentageCutoff) {
            //calculate how much larger best was over optimal
            double percentageOver = bestOverall / optTour;
            System.out.println("Best was achieved in: " + numIterations + " iterations and was " + String.format("%.2f", percentageOver) + " times over the optimal");
        } 
        System.out.println("Execution time: " + String.valueOf((stopTime - startTime) / 1000.0) + " seconds");
    }
    
    /**
     * Helper method that constructs tours for all ants and creates a list of all cities used to determine
     * which cities haven't been visited yet. 
     * 
     * @param antCollection
     *      The collection of ants that will be constructing tours with. 
     */
    private void constructTours(Ant[] antCollection) {
        for (int i = 0; i < NUM_ANTS; i++) {
            Ant ant = antCollection[i];
            //reset ant to remove values from last iteration
            ant.resetAnt();
            //list of cities to check what ant hasn't visited yet
            ArrayList<Integer> cities = new ArrayList<Integer>();
            for (int j = 0; j < numCities; j++) {
                cities.add(j);
            }
            cities.remove(cities.indexOf(ant.getCity()));
            
            //iterations to move one ant through all cities and calculate probabilities to 
            //determine next city move
            makeSingleTour(ant, cities);
            
            ant.updateCurDistance(distances[ant.getCity()][ant.getFirstCity()]);
            ant.checkBestTour();
        }
    }
    
    /**
     * Helper method that constructs a tour for a singular ant. Does this by calling on another method
     * to determine probabilities of visiting a city, and then picks from that list of probabilities. 
     * 
     * @param ant
     *      A singular ant whose tour we are constructing.
     * @param cities
     *      A list of cities that represent what the ant has and hasn't visited yet. 
     */
    private void makeSingleTour(Ant ant, ArrayList<Integer> cities) {
        //iterate while there are still cities to visit
        int count2 = 0;
        while (count2 < numCities - 1) {
            double sum = 0.0;
            //get new probability sum for denominator
            for (int j = 0; j < cities.size(); j++) {
                sum += probabilitiesList[ant.getCity()][cities.get(j)];
            }
            
            //pick next city based on probabilities
            Random randomProb = new Random();
            double probability = randomProb.nextDouble();
            boolean finder = false;
            int nextCity = 0;
            double runningProbSum = 0;
            while (!finder) {
                runningProbSum += (probabilitiesList[ant.getCity()][cities.get(nextCity)] / sum);
                if (probability <= runningProbSum) {
                    ant.updateCurDistance(distances[ant.getCity()][cities.get(nextCity)]);
                    ant.updateCity(cities.get(nextCity));
                    cities.remove(cities.indexOf(cities.get(nextCity)));
                    finder = true;
                } else if (cities.size() < 2) {
                    ant.updateCurDistance(distances[ant.getCity()][cities.get(0)]);
                    ant.updateCity(cities.get(0));
                    finder = true;
                } else {
                    nextCity++;
                }
            }
            count2++;
        }
    }
    
    /**
     * Helper method used to calculate/update the 2d probability array. The values stored here are just the
     * numerator portion of the probability equation for each leg in the problem. We calculate their sum 
     * based on remaining cities and divide them when actually checking in the makeSingleTour method. Probability
     * is based on the pheremone level and distance for each leg in the numerator, and is divided by the sum
     * of all available cities to determine a probability.
     */
    private void calculateProbabilities() {
        probabilitiesList = new double[numCities][numCities];
        double firstProbabilitySum = 0;
        for (int i = 0; i < numCities; i++) {
            for (int j = i; j < numCities; j++) {
                if (i == j) {
                    probabilitiesList[i][j] = 0.0;
                    probabilitiesList[j][i] = 0.0;
                } else {
                    double pheremoneNumerator = Math.pow(pheremoneLevels[i][j], ALPHA);
                    double distanceNumerator = Math.pow((1 / (distances[i][j])), BETA);
                    probabilitiesList[i][j] = (pheremoneNumerator * distanceNumerator);
                    probabilitiesList[j][i] = (pheremoneNumerator * distanceNumerator);
                }
            }
        }
    }
    
    /**
     * Helper method that updates pheremone levels after an iteration has completed and all tours have been
     * constructed. Evaporates all legs at constant rate and deposits on legs that were in an ants tour
     * proportional to the length of the ants tour. Also returns the best tour amongst the all of the ants
     * in that iteration.
     * 
     * @param antCollection
     *      The collection of ants that will be constructing tours with.
     *      
     * @return
     *      The best (smallest) tour from this iteration. 
     */
    private double updatePheremoneLevels(Ant[] antCollection) {
        //decrease all pheremone levels first
        for (int legCol = 0; legCol < numCities; legCol++) {
            for (int legRow = legCol; legRow < numCities; legRow++) {
                pheremoneLevels[legRow][legCol] *= (1 - ROW);
                pheremoneLevels[legCol][legRow] *= (1 - ROW);
            }
        }
        
        double bestTour = Integer.MAX_VALUE;
        //deposit pheremone to all visited legs and also find current shortest tour
        for (int i = 0; i < NUM_ANTS; i++) {
            //get an ant
            Ant ant = antCollection[i];
            
            //iterate through the ants previous tour and for each leg, add to that legs pheremone level by
            // an amount of 1 divided by the total distance of that ants tour
            for (int leg = 0; leg < ant.getCurTour().size() - 1; leg++) {
                pheremoneLevels[ant.getCurTour().get(leg)][ant.getCurTour().get(leg + 1)] += (1 / ant.getCurDistance());
                pheremoneLevels[ant.getCurTour().get(leg + 1)][ant.getCurTour().get(leg)] += (1 / ant.getCurDistance());
            }
            pheremoneLevels[ant.getCurTour().get(ant.getCurTour().size() - 1)][ant.getCurTour().get(0)] += (1 / ant.getCurDistance());
            pheremoneLevels[ant.getCurTour().get(0)][ant.getCurTour().get(ant.getCurTour().size() - 1)] += (1 / ant.getCurDistance());
            
            //also check if ant is best
            if (ant.getCurDistance() < bestTour) {
                bestTour = ant.getCurDistance();
            }
        }
        return bestTour;
    }
}