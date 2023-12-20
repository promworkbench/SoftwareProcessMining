package software.processmining.componentmodelevaluation;
/*
 * this class defines the fitness, precision, generalization metrics. 
 */
public class HPNQualityMetrics {

	private double fitness =0.0;
	private double precision =0.0;
	private double generalization =0.0;
	private double Fmeasure =0.0;
	
	public double getFmeasure() {
		return Fmeasure;
	}
	public void setFmeasure(double fmeasure) {
		Fmeasure = fmeasure;
	}
	public double getFitness() {
		return fitness;
	}
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	public double getPrecision() {
		return precision;
	}
	public void setPrecision(double precision) {
		this.precision = precision;
	}
	public double getGeneralization() {
		return generalization;
	}
	public void setGeneralization(double generalization) {
		this.generalization = generalization;
	}
	
}
