package software.processmining.multiviewinteraction;

import java.util.HashMap;
import java.util.HashSet;

public class SoftwareArchitectureModelSimple {
	//(1) a set of component models: only keep the component name and its interface names. 
	HashMap<String, HashSet<String>> component2Interfaces = new HashMap<>();

	//(2) a set of interface interactions: from an interface to a set of interfaces
	//E.g., interface1--><interface2, interface3> means interface2, interface3 is required by interface1
	HashMap<String, HashSet<String>> interfaceInteractions = new HashMap<>();
	
	public HashMap<String, HashSet<String>> getComponent2Interfaces() {
		return component2Interfaces;
	}

	public void setComponent2Interfaces(HashMap<String, HashSet<String>> component2Interfaces) {
		this.component2Interfaces = component2Interfaces;
	}

	public HashMap<String, HashSet<String>> getInterfaceInteractions() {
		return interfaceInteractions;
	}

	public void setInterfaceInteractions(HashMap<String, HashSet<String>> interfaceInteractions) {
		this.interfaceInteractions = interfaceInteractions;
	}

}
