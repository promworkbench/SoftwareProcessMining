package software.processmining.interfacediscoveryevaluation;

import java.util.HashSet;

/**
 *  this class gives a full description of the software, i.e., a set of components. 
 * @author cliu3
 *
 */
public class SoftwareDescription {

	private HashSet<ComponentDescription> componentSet;
	
	public HashSet<ComponentDescription> getComponentSet() {
		return componentSet;
	}

	public void setComponentSet(HashSet<ComponentDescription> componentSet) {
		this.componentSet = componentSet;
	}

	//default constuctor
	public SoftwareDescription()
	{
		componentSet = new HashSet<>();
	}
	
	public int getNumberofComponent()
	{
		return componentSet.size();
	}
	
}
