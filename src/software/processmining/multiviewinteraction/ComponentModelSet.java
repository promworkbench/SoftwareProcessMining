package software.processmining.multiviewinteraction;

import java.util.HashSet;

public class ComponentModelSet {
	private HashSet<ComponentModel> componentModelSet=new HashSet<>();

	public HashSet<ComponentModel> getComponentModelSet() {
		return componentModelSet;
	}

	public void setComponentModel(ComponentModel componentModel) {
		componentModelSet.add(componentModel);
	} 

}
