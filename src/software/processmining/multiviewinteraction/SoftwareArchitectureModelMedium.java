package software.processmining.multiviewinteraction;

import java.util.HashMap;
import java.util.HashSet;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;

public class SoftwareArchitectureModelMedium {
	//(1) a set of component models: only keep the component name and its interface names. 
	HashMap<String, HashSet<String>> component2Interfaces = new HashMap<>();
	
	//(2) a set of connector models: from interface to pn
	HashMap<MethodInterface, Petrinet> connectorModels = new HashMap<>();

	public HashMap<String, HashSet<String>> getComponent2Interfaces() {
		return component2Interfaces;
	}

	public void setComponent2Interfaces(HashMap<String, HashSet<String>> component2Interfaces) {
		this.component2Interfaces = component2Interfaces;
	}

	public HashMap<MethodInterface, Petrinet> getConnectorModels() {
		return connectorModels;
	}

	public void setConnectorModels(HashMap<MethodInterface, Petrinet> connectorModels) {
		this.connectorModels = connectorModels;
	}
	
	
}
