package software.processmining.interfacediscoveryevaluation;

import java.util.HashSet;

/**
 *  this class gives a full description of the component
 * @author cliu3
 *
 */
public class ComponentDescription {

	private String componentName;
	private HashSet<InterfaceDescription> interfaceSet;
	
	//default constructor
	public ComponentDescription()
	{
		componentName ="";
		interfaceSet = new HashSet<>();
	}

	public HashSet<InterfaceDescription> getInterfaceSet() {
		return interfaceSet;
	}

	public void setInterfaceSet(HashSet<InterfaceDescription> interfaceSet) {
		this.interfaceSet = interfaceSet;
	}
	

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}
	
	
}
