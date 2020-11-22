import net.sourceforge.jswarm_pso.FitnessFunction;
import java.lang.Math;

public class MyFitnessFunction extends FitnessFunction {
	private static final double A = 10;
	private static final int N = 10;

	public MyFitnessFunction() {super(false);}

	public double evaluate(double position[]) { 
		double value = A*N;
		for (int i = 0; i < N; ++i) {
			value += Math.pow(position[i],2) - A*Math.cos(2.0*Math.PI*position[i]);
		}

		return value;
	}
} 
