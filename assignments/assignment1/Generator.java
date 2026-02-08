package assignment1;

/*
 Narmina Suleymanova
 CRN 20966
 Random Number Generator Statistics
 08.02.26
 */

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

// class definition
public class Generator {

    // class attributes (+ private access modifiers)
    private final Random random = new Random();
    private String lastGeneratorName;


    // method definition, returns arraylist of n random numbers
    public ArrayList<Double> populate(int n, int randNumGen) {

        // object instantiation
        ArrayList<Double> values = new ArrayList<>(n);

        lastGeneratorName = generatorName(randNumGen);

        for (int i = 0; i < n; i++) {
            double value;

            // logic to select the specific generator based on parameter
            switch (randNumGen) {
                case 1:
                    value = random.nextDouble(); // java.util.Random
                    break;
                case 2:
                    value = Math.random(); // Math.random()
                    break;
                case 3:
                    value = ThreadLocalRandom.current().nextDouble(); // ThreadLocalRandom
                    break;
                default:
                    value = Math.random();
            }

            values.add(value);
        }

        return values;
    }

    // helper method to map integers to generator names for display + private accessibility
    private String generatorName(int id) {
        switch (id) {
            case 1: return "java.util.Random";
            case 2: return "Math.random()";
            case 3: return "ThreadLocalRandom";
            default: return "Unknown";
        }
    }


    // method definition, calculates statistics using stream api
    public ArrayList<Double> statistics(ArrayList<Double> randomValues) {
        // DoubleSummaryStatistics provides count, average, min, and max automatically
        DoubleSummaryStatistics stats = randomValues.stream()
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();

        double n = stats.getCount();
        double mean = stats.getAverage();

        // calculate sum of squared differences for sample standard deviation
        double variance = randomValues.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .sum();

        // Sample Standard Deviation uses Bessel's correction (n - 1)
        double stddev = (n > 1) ? Math.sqrt(variance / (n - 1)) : 0;

        ArrayList<Double> res = new ArrayList<>();
        res.add(n);
        res.add(mean);
        res.add(stddev);
        res.add(stats.getMin());
        res.add(stats.getMax());

        return res;
    }


    // method definition, displays results in tabular format
    public void display(ArrayList<Double> r, boolean headerOn) {

        if (headerOn) {
            System.out.printf("%25s %15s %15s %15s %15s %15s%n",
                    "Generator Used", "n", "mean", "stddev", "min", "max");
            System.out.println("--------------------------------------------------------------------------------------------------------");
        }

        System.out.printf("%25s %15.0f %15.6f %15.6f %15.6f %15.6f%n",
                lastGeneratorName, r.get(0), r.get(1), r.get(2), r.get(3), r.get(4));
    }


    // method definition, calls populate, statistics and display methods for different n values
    public void execute() {

        int[] sampleSizes = {10, 1000, 1000000};
        boolean header = true;

        for (int n : sampleSizes) {
            for (int gen = 1; gen <= 3; gen++) {
                ArrayList<Double> values = populate(n, gen);
                ArrayList<Double> stats = statistics(values);
                display(stats, header);
                header = false; // Only print the header for the first entry
            }
        }
    }

}