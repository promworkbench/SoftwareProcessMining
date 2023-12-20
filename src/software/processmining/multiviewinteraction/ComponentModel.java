package software.processmining.multiviewinteraction;

import java.util.HashSet;

public class ComponentModel {
	private String componentName ="";// component name
	private HashSet<InterfaceID2HierarchicalPetriNet> i2hpnSet = new HashSet<>();
	
	public String getComponentName() {
		return componentName;
	}
	public void setComponentName(String component) {
		this.componentName = component;
	}
	public HashSet<InterfaceID2HierarchicalPetriNet> getI2hpn() {
		return i2hpnSet;
	}
	public void setI2hpn(HashSet<InterfaceID2HierarchicalPetriNet> i2hpnSet) {
		this.i2hpnSet = i2hpnSet;
	}
}
