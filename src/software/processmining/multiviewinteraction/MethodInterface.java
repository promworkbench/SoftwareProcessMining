package software.processmining.multiviewinteraction;

import UtilityClasses.MethodClass;

public class MethodInterface {

	//method and its belonging interface name. 
	private MethodClass method ;
	private String interName ;
	
	public MethodClass getMethod() {
		return method;
	}
	public void setMethod(MethodClass method) {
		this.method = method;
	}
	public String getInterName() {
		return interName;
	}
	public void setInterName(String interName) {
		this.interName = interName;
	}
			
}
