## Random Number Generator Analysis

This project implements a statistical analyzer of random numbers using different Java random numbers generator techniques. 

## Features:
Three way comparison: util.Random, Math.random(), and ThreadLocalRandom.
Computes mean, standard deviation, min and max.
Displays results clearly with the number generator name.

## Requirements:
Ensure you have the Java Development Kit (JDK) 8 or a newer version installed on your system.

## Logic & Methodology:
populate() - this method generates an ArrayList of size n of different random numbers
statistics() - this method calculates mean, standard deviation, min and max using StreamAPI
display() - this method displays all the results in a tabular format

## Theoretical Expectations:
The program is designed to demonstrate the Law of Large Numbers. Since the generators produce values in a uniform distribution [0,1), we expect the statistics to stabilize as n grows. 
