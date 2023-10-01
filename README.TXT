PROJECT TITLE: Optimization Algorithms for Traveling Salesman Problem: A Comparison of Basic Ant System and Ant Colony Optimization-Genetic Algorithm Hybrid 
PURPOSE OF PROJECT: To determine most optimal performance on TSP between Basic Ant System ACO and ACO-GA Hybrid.
VERSION or DATE: 05/20/2022
AUTHORS: Andy Stoneman, Michael Webber, Alex Clark
USER INSTRUCTIONS:

Create an AGHybrid object using the following six arguments in the constructor:

1. Filename
2. The known optimal solution for the file
3. A boolean value--false if you want to specify the number of iterations, and true if you want to specify an optimal range.
4. **The number of iterations
5. **The optimal range
6. The type of crossover that is desired. Two options are implemented here: ordered crossover, which should be entered as 'OX', and partially mapped crossover, which should be entered as 'PMX'.


**NOTE: Both values have to be entered regardless of the boolean value. The one you do not use will be irrelevant. 

Finally, call solve() method on the AGHybrid object you created, and the algorithm will run!
