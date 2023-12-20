package software.processmining.classinteractiondiscovery;

import UtilityClasses.ComponentConfig;

/**
 * this class is used to wrapper the componentconfig object such that it can be passed in the button listener. 
 * @author cliu3
 *
 */
public class ComponentConfigWrapper {
	private ComponentConfig value;

	public ComponentConfigWrapper(ComponentConfig value) {
       this.value = value;
   } 
	
	public void setValue(ComponentConfig value) {
		this.value = value;
	}
	
	public ComponentConfig getValue()
	{
		return this.value;
	}
}
