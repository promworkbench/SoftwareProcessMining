package software.processmining.componentmodelevaluation;

public class VisualizeHPNQualityMetricMaps {
	//visualize the SoftwareEventLogStatistics as a string
		public static String visualizeQualityMetricsMap(HPNQualityMetricsMap com2quality)
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append("<html>"); 
			buffer.append("<body>");
			
			for(String com: com2quality.getComponentSet())
			{
				buffer.append("<h1 style=\"color:blue;\">"+"Quality Metrics of Component "+com+" </h1>");  
				
				buffer.append("<h2 style=\"color:#2F8AA1;\">"+"Fitness: "+com2quality.getComponentQualityMetrics(com).getFitness()+"</h2>");
				buffer.append("<h2 style=\"color:#2F8AA1;\">"+"Precision: "+com2quality.getComponentQualityMetrics(com).getPrecision()+"</h2>");
				buffer.append("<h2 style=\"color:#2F8AA1;\">"+"F-measure: "+com2quality.getComponentQualityMetrics(com).getFmeasure()+"</h2>");
				buffer.append("<h2 style=\"color:#2F8AA1;\">"+"Generalization: "+com2quality.getComponentQualityMetrics(com).getGeneralization()+"</h2>");
			}
		
			buffer.append("</body>");
			buffer.append("</html>");
			return buffer.toString();
		}		
}
