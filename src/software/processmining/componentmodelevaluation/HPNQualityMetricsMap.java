package software.processmining.componentmodelevaluation;

import java.util.HashMap;
import java.util.Set;

/*
 * this class defines the component to quality metrics
 */
public class HPNQualityMetricsMap {

	private HashMap<String, HPNQualityMetrics> component2qualitymetrics=new HashMap<>();
	
	
	/**
	 * adding com --> hpn 
	 * @param com
	 * @param hpn
	 */
	public void addComponentModel(String com, HPNQualityMetrics qua) {
		component2qualitymetrics.put(com, qua);
	}
	
	/**
	 * get the component set
	 * @return
	 */
	public Set<String> getComponentSet()
	{
		return component2qualitymetrics.keySet();
	}
	
	/**
	 * get the quality of a component
	 * @param com
	 * @return
	 */
	public HPNQualityMetrics getComponentQualityMetrics(String com) 
	{
		return component2qualitymetrics.get(com);
	}
	
	
	/**
	 * the number of components
	 * @return
	 */
	public int size()
	{
		return component2qualitymetrics.size();
	}
}
