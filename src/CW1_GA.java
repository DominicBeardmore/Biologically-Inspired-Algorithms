import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;

import javax.management.timer.Timer;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static io.jenetics.engine.EvolutionResult.toBestPhenotype;

import io.jenetics.DoubleGene;
import io.jenetics.SinglePointCrossover;
import io.jenetics.UniformCrossover;
import io.jenetics.MeanAlterer;
import io.jenetics.TournamentSelector;
import io.jenetics.EliteSelector;
import io.jenetics.Mutator;
import io.jenetics.GaussianMutator;
import io.jenetics.Optimize;
import io.jenetics.Phenotype;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.util.DoubleRange;

// Testing was as automated as possible. This programme would run each test 30 times and calculate means.
// Parameter settings can be set with the global variables
// There are 4 different test settings
	// 0 Population and Iteration. The arrays in the popItr method have to be commented out and the total comparison limit modified for each scenario 
	// 1 Mutation. 10 sets of parameters are created in the mutation method
	// 2 Crossover. 10 sets of parameters are created in the crossover method
	// 3 Default runs the parameters set at the top of the file 30 times. The mean of this test is printed
public class CW1_GA {
	private static final double A = 10;
	private static final double R = 5.12;
	private static final int N = 10;
	private static final int setting = 3; // 0 PopItr  //1 Mut // 2 crossover // 3 default
	
	// SET PARAMETERS HERE
	private int popSize = 100;
	private int numSurvivors = 1;
	private int tournamentSize = 2;
	private double probCrossover = 1;
	private double probMutation = 0.09;
	private int numIters = 100;
	
	private static double fitness(final double[] x) {
		double value = A*N;
		for (int i = 0; i < N; ++i) {
			value += x[i]*x[i] - A*cos(2.0*PI*x[i]);
		}

		return value;
	}

	public void parseParams(String paramFile) {
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(paramFile));

			Enumeration enuKeys = properties.keys();
			while (enuKeys.hasMoreElements()) {
				String key = (String) enuKeys.nextElement();
				String value = properties.getProperty(key);
	
				if(key.equals("popSize")) {
					popSize = Integer.parseInt(value);
				} else if(key.equals("numSurvivors")) {
					numSurvivors = Integer.parseInt(value);
				} else if(key.equals("tournamentSize")) {
					tournamentSize = Integer.parseInt(value);
				} else if(key.equals("probMutation")) {
					probMutation = Double.parseDouble(value);
				} else if(key.equals("probCrossover")) {
					probCrossover = Double.parseDouble(value);
				} else if(key.equals("numIters")) {
					numIters = Integer.parseInt(value);
				} else {
					System.out.println("Unknown parameter "+key);
				} 
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Double run() {
		final Engine<DoubleGene, Double> engine = Engine
			.builder(
				CW1_GA::fitness,
				// Codec for 'x' vector.
				Codecs.ofVector(DoubleRange.of(-R, R), N))
			.populationSize(popSize)
			.optimize(Optimize.MINIMUM)
			.survivorsSize(numSurvivors)
			.survivorsSelector(new EliteSelector<>(numSurvivors))
			.offspringSelector(new TournamentSelector<>(tournamentSize))
			.alterers(
				new Mutator<>(probMutation),
//				new GaussianMutator<>(probMutation),
				//new UniformCrossover<>(probCrossover)
				new SinglePointCrossover<>(probCrossover) 
				//new MeanAlterer<>()
				)
			.build();

		final EvolutionStatistics<Double, ?>
			statistics = EvolutionStatistics.ofNumber();

		final Phenotype<DoubleGene, Double> best = engine.stream()
			.limit(numIters)
			.peek(statistics)
			// Uncomment the following line to get updates at each iteration
			//.peek(r -> System.out.println(statistics))
			//.peek(r -> System.out.println(((DoubleMomentStatistics) statistics.getFitness()).getMean()))
			.collect(toBestPhenotype());
		
		return best.getFitness();
	}


	public static void main(final String[] args) {
		CW1_GA alg = new CW1_GA();
		double[][] params = new double[10][6];
		
			// CREATE TEST PARAMS
			params = alg.genParams(setting);
			for (double[] paramSet : params) {
				alg.popSize = (int) paramSet[0];
				alg.numSurvivors = (int) paramSet[1];
				alg.tournamentSize = (int) paramSet[2];
				alg.probMutation = paramSet[3];
				alg.probCrossover = paramSet[4];
				alg.numIters = (int) paramSet[5];
				alg.runAlg(setting, alg);
			}
	}
	
	private void runAlg(int setting, CW1_GA alg ) {
		int repetitions = 30;
		double[] meanArr = new double [repetitions];
		double total = 0;
		long timeElapsed = 0;
		for (int i = 0; i < repetitions; i++) {
			Instant start = Instant.now();
			double best = alg.run();
			meanArr[i] = best;
			total += best;
			Instant finish = Instant.now();
			timeElapsed += Duration.between(start, finish).toMillis();
		}

		System.out.println("--------------------------------------------------");
		printParams(setting);
		System.out.println("Mean " + total / repetitions);
		System.out.println("Time " + timeElapsed / repetitions);
		System.out.println("--------------------------------------------------");
	}
	
	private void printMut() {
		System.out.println("Probability of Mutation: " + probMutation );
	}
	
	private void printCrossover() {
		System.out.println("Probability of Crossover: " + probCrossover );
	}
	
	private void printPop() {
		System.out.println("Population: " + popSize );
	}
	
	private void printItr() {
		System.out.println("Iterations: " + numIters );
	}
	
	private void printTournamentSize() {
		System.out.println("Tournament Size: " + tournamentSize );
	}
	
	private void printSurvivors() {
		System.out.println("Number of Survivors: " + numSurvivors );
	}
	
	private void printParams(int setting) {
		
		printMut();
		printCrossover();
		printPop();
		printItr();
		printTournamentSize();
		printSurvivors();
		
	}

	private double[][] genParams(int param) {
		double[][] params = new double[10][6];
		switch (param) {
			case 0: 
				params = popItr();
				break;
			case 1: 
				params = mutation();
				break;
			case 2: 
				params = crossover();
				break;
			case 3: 
				params = new double[][]{{popSize, numSurvivors, tournamentSize, probMutation,probCrossover, numIters}};
				break;
			default:
				
				
		}
		return params;
	}
	
	// Generates params for population and iteration
	private double[][] popItr() {
		int[] popArr = {50, 100, 200, 500, 1000, 2000, 4000, 5000, 10000, 20000}; // 1000000
//		int[] popArr = {40, 50, 80, 100, 125, 200, 250, 400, 500, 1000}; // 10000
		int totalComp = 1000000; // Total comparisons, needs to be changed for each scenario either 100,000 or 10,000
		double[][] params = new double[10][6];
		for (int i = 0; i < popArr.length; i++) {
			int itr = (totalComp / popArr[i]);
			params[i] = new double[]{popArr[i], numSurvivors, tournamentSize, probMutation, probCrossover, itr};
		}
		
		return params;
	}
	
	// Generates params for the mutation tests
	private double[][] mutation() {
		double[][] params = new double[10][6];
		// Changes the search increment
		double modFac = 0.01;
		for (int i = 0; i <= 9; i++) {
			double mut = (i * modFac);
			// To test a higher range, add to the mut value before putting into the params array
			params[i] = new double[]{popSize, numSurvivors, tournamentSize, mut, probCrossover, numIters};
		}
		
		return params;
	}
	
	// Generates params for the crossover tests
	private double[][] crossover() {
		double[][] params = new double[10][6];
		// Changes the search increment
		double crossProb = 0.1;
		for (int i = 0; i <= 9; i++) {
			// To test a higher range, add to the cross value before putting into the params array
			double cross = (i * crossProb);
			params[i] = new double[]{popSize, numSurvivors, tournamentSize, probMutation, cross, numIters};
		}
		
		return params;
	}
}
