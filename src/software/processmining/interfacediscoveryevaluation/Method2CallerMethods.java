package software.processmining.interfacediscoveryevaluation;

import java.util.HashSet;

import UtilityClasses.MethodClass;

/**
 * this class defines the mapping from a method to its caller method set
 * @author cliu3
 *
 */
public class Method2CallerMethods {
	private MethodClass method;
	private HashSet<MethodClass> callerMethodSet;
	
	//default constructor
	public Method2CallerMethods()
	{
		method= new MethodClass();
		callerMethodSet =new HashSet<>();
	}
	
	public MethodClass getMethod() {
		return method;
	}
	public void setMethod(MethodClass method) {
		this.method = method;
	}
	public HashSet<MethodClass> getCallerMethodSet() {
		return callerMethodSet;
	}
	public void setCallerMethodSet(HashSet<MethodClass> callerMethodSet) {
		this.callerMethodSet = callerMethodSet;
	}	
}
