import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;


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
import io.jenetics.util.DoubleRange;

public class CW1_GA {
	private static final double A = 10;
	private static final double R = 5.12;
	private static final int N = 10;

	private int popSize = 1000;
	private int numSurvivors = 1;
	private int tournamentSize = 2;
	private double probMutation = 0.01;
	private double probCrossover = 0.30;
	private int numIters = 1000;

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

	public void run() {
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
				//new Mutator<>(probMutation),
				new GaussianMutator<>(probMutation),
				//new UniformCrossover<>(probCrossover))
				new SinglePointCrossover<>(probCrossover))
				//new MeanAlterer<>(param))
			.build();

		final EvolutionStatistics<Double, ?>
			statistics = EvolutionStatistics.ofNumber();

		final Phenotype<DoubleGene, Double> best = engine.stream()
			.limit(numIters)
			.peek(statistics)
			// Uncomment the following line to get updates at each iteration
			//.peek(r -> System.out.println(statistics))
			.collect(toBestPhenotype());

		System.out.println(statistics);
		System.out.println(best);
	}


	public static void main(final String[] args) {
		CW1_GA alg = new CW1_GA();
		if(args.length>0) {
			alg.parseParams(args[0]);
		}
		alg.run();
	}
}
