package util;

import java.util.Random;

public class Poisson {
	
	Random rand = new Random();
	
	double t;  // starting time
	
	double lambda;
	
	int startPoint;
	
	public Poisson(double lambda, double startPoint) {
		this.t = startPoint;
		this.lambda = lambda;
	}
	
	public int nextPoisson() {
		double dt = -1 * Math.log(rand.nextDouble()) / lambda;
		t += dt;
		return (int) t;
	}
	
	public static void main(String[] args) {
		double interval = 10;
		Poisson p = new Poisson(1/interval, 100);
		for (int i=0; i<100; i++) {
			System.out.println(p.nextPoisson());
		}
	}

}
