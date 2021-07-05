import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import net.sourceforge.jswarm_pso.Neighborhood1D;
import net.sourceforge.jswarm_pso.FitnessFunction;
import net.sourceforge.jswarm_pso.Particle;
import net.sourceforge.jswarm_pso.Swarm;
import java.lang.Math;
import java.time.Duration;
import java.time.Instant;

public class CW1_PSO {
	// There are 6 settings to run all the different tests. The numbers are the position in the param array.
		// 0 Population and iteration
		// 2 Neighbourhood weight
		// 3 Inertia Weight
		// 4 Personal Weight
		// 5 Global Weight
		// 6 Velocity
	// There is no default setting that only runs the params set. However PSO is quick enough that this was not an issue to wait a short time for individual when required
	
	private static double R = 5.12;

	private static final int setting = 6; // 0 PopItr  // 2 Neighweight // 3 IW // 4 Personal Weight // 5 GW // 6 Velocity
	
	// SET PARAMETERS HERE
	private int numParticles = 80;
	private int numIters = 125;
	private double neighWeight = 0.25;
	private double inertiaWeight = 0.5;
	private double personalWeight = 2.25;
	private double globalWeight = 0.5;
	private double maxMinVelocity = 0.04;

	public void parseParams(String paramFile) {
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(paramFile));

			Enumeration enuKeys = properties.keys();
			while (enuKeys.hasMoreElements()) {
				String key = (String) enuKeys.nextElement();
				String value = properties.getProperty(key);
	
				if(key.equals("numParticles")) {
					numParticles = Integer.parseInt(value);
				} else if(key.equals("neighWeight")) {
					neighWeight = Double.parseDouble(value);
				} else if(key.equals("inertiaWeight")) {
					inertiaWeight = Double.parseDouble(value);
				} else if(key.equals("personalWeight")) {
					personalWeight = Double.parseDouble(value);
				} else if(key.equals("globalWeight")) {
					globalWeight = Double.parseDouble(value);
				} else if(key.equals("maxMinVelocity")) {
					maxMinVelocity = Double.parseDouble(value);
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

	public double run() {
		// Create a swarm (using 'MyParticle' as sample particle 
		// and 'MyFitnessFunction' as finess function)
		Swarm swarm = new Swarm(numParticles
			, new MyParticle()
			, new MyFitnessFunction());
		// Set position (and velocity) constraints. 
		// i.e.: where to look for solutions

		// Use neighborhood
		Neighborhood1D neigh = new Neighborhood1D(numParticles / 10, true);
		swarm.setNeighborhood(neigh);
		swarm.setNeighborhoodIncrement(neighWeight);

		// Set weights of velocity update formula
		swarm.setInertia(inertiaWeight); // Previous velocity weight
                swarm.setParticleIncrement(personalWeight); // Personal best weight
                swarm.setGlobalIncrement(globalWeight); // Global best weight

		// Set limits to velocity value
		swarm.setMaxMinVelocity(maxMinVelocity);

		// Set max and min positions
		swarm.setMaxPosition(+R);
		swarm.setMinPosition(-R);

		// Optimize a few times
		for( int i = 0; i < numIters; i++ ) { 
			swarm.evolve();
			//System.out.println(swarm.toStringStats());
		}
		
		return swarm.getBestFitness();
	}

	public static void main(final String[] args) {
		CW1_PSO alg = new CW1_PSO();
		double[][] params = new double[10][7];
		if(args.length>0) {
			alg.parseParams(args[0]);
		}
		
		params = alg.genParams();
		
		for (double[] paramSet : params) {
			alg.numParticles 	= (int) paramSet[0];
			alg.numIters 		= (int) paramSet[1];
			alg.neighWeight 	= paramSet[2];
			alg.inertiaWeight 	= paramSet[3];
			alg.personalWeight 	= paramSet[4];
			alg.globalWeight 	= paramSet[5];
			alg.maxMinVelocity 	= paramSet[6];
			alg.runAlg(setting, alg);
		}
	}
	
	private double[][] genParams() {

		double[][] params = stdParams();
		switch (setting) {
			case 0: // Population Iteration
				params = popItr();
				break;
			case 2: // neighboohood weight
				params = nw(params, neighWeight);
				break;
			case 3: // Inertia Weight
				params = nw(params, inertiaWeight);
				break;
			case 4: // Personal weight
				params = nw(params, personalWeight);
				break;
			case 5: // Global Weight
				params = nw(params, globalWeight);
				break;
			case 6: // Velocity
				params = nw(params, maxMinVelocity);
				break;
			default:
		}
		return params;
	}
	
	private double[][] stdParams(){
		double[][] params = new double[10][6];
		for (int i = 0; i < 10; i++) {
			params[i] = new double[]{
					numParticles,
					numIters,
					neighWeight,
					inertiaWeight,
					personalWeight,
					globalWeight,
					maxMinVelocity
					};
		}
		
		return params;
	}

	// Generates params for population and iteration
	private double[][] popItr() {
//		int[] pop = {50, 100, 200, 500, 1000, 2000, 4000, 5000, 10000, 20000}; // 1000000
		int[] pop = {40, 50, 80, 100, 125, 200, 250, 400, 500, 1000}; // 10000
		int totalComp = 10000; // Total comparisons, needs to be changed for each scenario either 100,000 or 10,000
		double[][] params = new double[10][6];
		for (int i = 0; i < pop.length; i++) {
			int itr = (totalComp / pop[i]);
			params[i] = new double[]{pop[i], itr, neighWeight, inertiaWeight, personalWeight, globalWeight, maxMinVelocity };
		}
		
		return params;
	}
	
	// Modifies 1 param based on the setting value
	private double[][] nw(double[][] params, double param) {
		double[] nw = modParam(param);

		for (int i = 0; i < 10; i++) {
			params[i][setting] = nw[i];
		}

		return params;
	}
	
	private double[] modParam(double param) {
		double[] modParam = new double[10];
		double modFac = 5; // Increment size

		for (int i = 0; i <= 9; i++) {
			double nw = (i * modFac);
			modParam[i] = nw;
		}
		
		return modParam;
	}

	private void runAlg(int setting, CW1_PSO alg) {
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
	
	private void printParams(int setting) {
		printParticles();
		printIter();
		printNeighW();
		printInertiaWeight();
		printPersW();
		printGlobalWeight();
		printVelocity();
	}

	private void printParticles() {
		System.out.println("Number of Particles: " + numParticles );
	}
	
	private void printIter() {
		System.out.println("Number of Iterations: " + numIters );
	}
	
	private void printNeighW() {
		System.out.println("neighbourhood weight " + neighWeight );
	}
	
	private void printInertiaWeight() {
		System.out.println("Inertia weight: " + inertiaWeight );
	}
	
	private void printPersW() {
		System.out.println("Personal Weight: " + personalWeight );
	}
	
	private void printGlobalWeight() {
		System.out.println("Global Weight: " + globalWeight );
	}
	
	private void printVelocity() {
		System.out.println("Velocity: " + maxMinVelocity );
	}
	
}
