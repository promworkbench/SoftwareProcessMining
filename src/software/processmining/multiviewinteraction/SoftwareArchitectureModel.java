package software.processmining.multiviewinteraction;

import java.util.HashMap;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;

/*
 * 2017-11-29
 * the software architecture is defined as 
 * (1) a set of component models that each is composed of a set of interface behavior models (we use flat Petri net). 
 * (2) interface cardinality information
 * (3) from interaction method (String) to connector model (flat petri net)
 */
public class SoftwareArchitectureModel {
	
	// the component models 
	private ComponentModelSet componentModelSet = new ComponentModelSet();
	
	//interface cardinality
	private HashMap<InterfaceID, Integer> interfaceCardinality = new HashMap<InterfaceID, Integer>();
	
	//from interaction method name to connector model 
	private HashMap<MethodInterface, Petrinet> method2connectorModel = new HashMap<>();
	
	
	public ComponentModelSet getComponentModelSet() {
		return componentModelSet;
	}

	public void setComponentModelSet(ComponentModelSet componentModelSet) {
		this.componentModelSet = componentModelSet;
	}

	public HashMap<InterfaceID, Integer> getInterfaceCardinality() {
		return interfaceCardinality;
	}

	public void setInterfaceCardinality(HashMap<InterfaceID, Integer> interfaceCardinality) {
		this.interfaceCardinality = interfaceCardinality;
	}

	public HashMap<MethodInterface, Petrinet> getMethod2connectorModel() {
		return method2connectorModel;
	}

	public void setMethod2connectorModel(HashMap<MethodInterface, Petrinet> method2connectorModel) {
		this.method2connectorModel = method2connectorModel;
	}
	
}
