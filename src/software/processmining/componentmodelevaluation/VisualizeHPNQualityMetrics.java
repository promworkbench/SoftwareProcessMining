package software.processmining.componentmodelevaluation;

public class VisualizeHPNQualityMetrics {
	
	//visualize the SoftwareEventLogStatistics as a string
	public static String visualizeQualityMetrics(HPNQualityMetrics metric)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("<html>"); 
		buffer.append("<body>");
		
		buffer.append("<h1 style=\"color:blue;\">"+"Quality Metrics of Petri Net against Log"+"</h1>");  
		
		buffer.append("<h2 style=\"color:#2F8AA1;\">"+"Fitness: "+metric.getFitness()+"</h2>");
		buffer.append("<h2 style=\"color:#2F8AA1;\">"+"Precision: "+metric.getPrecision()+"</h2>");
		buffer.append("<h2 style=\"color:#2F8AA1;\">"+"F-measure: "+metric.getFmeasure()+"</h2>");
		buffer.append("<h2 style=\"color:#2F8AA1;\">"+"Generalization: "+metric.getGeneralization()+"</h2>");

		buffer.append("</body>");
		buffer.append("</html>");
		return buffer.toString();
	}		
}
