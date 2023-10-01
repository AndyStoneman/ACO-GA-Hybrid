public class Main {
    public static void main(String[] args) {
        //code to run from terminal
        //AGHybrid agHybrid = new AntSystem(args[0], Double.valueOf(args[1]), Boolean.valueOf(args[2]), Integer.valueOf(args[4]), Double.valueOf(args[5]), args[6]);
        //agHybrid.solve();
        
        AGHybrid agHybrid = new AGHybrid("/Users/andrewstoneman/Desktop/NIC_Project_3/project3-aco-for-tsp/ALL_tsp/d2103.tsp", 80450, false, 50, 1.43, "OX");
        agHybrid.solve();
    }
}
