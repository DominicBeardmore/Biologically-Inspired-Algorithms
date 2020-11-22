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

public class CW1_PSO {
	private static double R = 5.12;

	private int numParticles = 1000;
	private int numIters = 1000;
	private double neighWeight = 1.0;
	private double inertiaWeight = 1.0;
	private double personalWeight = 1.0;
	private double globalWeight = 1.0;
	private double maxMinVelocity = 0.0001;

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

	public void run() {
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
		System.out.println(swarm.toStringStats());
	}

	public static void main(final String[] args) {
		CW1_PSO alg = new CW1_PSO();
		if(args.length>0) {
			alg.parseParams(args[0]);
		}
		alg.run();
	}
}
