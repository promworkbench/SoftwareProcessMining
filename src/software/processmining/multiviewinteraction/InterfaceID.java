package software.processmining.multiviewinteraction;
/*
 * the definition of interface is composed of InterfaceDescription + Interface ID (name)
 */

import software.processmining.interfacediscoveryevaluation.InterfaceDescription;

public class InterfaceID {

	private InterfaceDescription interfaceDescription = null;
	private String name ="";
	
	public InterfaceDescription getInterfaceDescription() {
		return interfaceDescription;
	}
	public void setInterfaceDescription(InterfaceDescription interfaceDescription) {
		this.interfaceDescription = interfaceDescription;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
