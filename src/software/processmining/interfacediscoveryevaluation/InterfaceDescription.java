package software.processmining.interfacediscoveryevaluation;

import java.util.HashSet;
import java.util.Objects;

import UtilityClasses.MethodClass;

/**
 *  this class gives a full description of the interface
 * @author cliu3
 *
 */
public class InterfaceDescription {
	private HashSet<Method2CallerMethods> method2CallerMethodSet;//the mapping from method 2 its caller method
	private HashSet<String> callerComponentSet; // we use string to represent the name of a component
	private HashSet<MethodClass> callerMethodSet; //the caller method set of this interface, this relies on method2CallerMethodSet;
	private HashSet<MethodClass> methodSet;// the method set of this interface, this relies on method2CallerMethodSet;
	
	//the default constructor
	public InterfaceDescription()
	{
		method2CallerMethodSet= new HashSet<Method2CallerMethods>();
		callerComponentSet =new HashSet<String>();
		callerMethodSet=new HashSet<MethodClass>();
		methodSet=new HashSet<>();
	}
	
	//generate the callerMethodSet using the method2CallerMethodSet;
	public void generateCallerMethodSet()
	{
		for(Method2CallerMethods m:method2CallerMethodSet)
		{
			callerMethodSet.addAll(m.getCallerMethodSet());
		}
	}
	
	//generate the methodSet using the method2CallerMethodSet
	public void generateMethodSet()
	{
		for(Method2CallerMethods m:method2CallerMethodSet)
		{
			methodSet.add(m.getMethod());
		}
	}
	
	
	public HashSet<Method2CallerMethods> getMethod2CallerMethodSet() {
		return method2CallerMethodSet;
	}
	public void setMethod2CallerMethodSet(HashSet<Method2CallerMethods> methodSet) {
		this.method2CallerMethodSet = methodSet;
	}
	public HashSet<String> getCallerComponentSet() {
		return callerComponentSet;
	}
	public void setCallerComponentSet(HashSet<String> callerComponentSet) {
		this.callerComponentSet = callerComponentSet;
	}
	public HashSet<MethodClass> getCallerMethodSet() {
		return callerMethodSet;
	}
	public void setCallerMethodSet(HashSet<MethodClass> callerMethodSet) {
		this.callerMethodSet = callerMethodSet;
	}

	public HashSet<MethodClass> getMethodSet() {
		return methodSet;
	}

	public void setMethodSet(HashSet<MethodClass> methodSet) {
		this.methodSet = methodSet;
	}	
	
	//the hashcode of an interface description is based on its method set and caller method set. 
	public int hashCode() {  
        return Objects.hash(methodSet, callerMethodSet);
    }  
	
	public boolean equals(Object other)
	{
		if (this==other)
		{
			return true;
		}
		if (other==null)
		{
			return false;
		}
		if (!(other instanceof InterfaceDescription))
		{
			return false;
		}
		if (this.hashCode()==((InterfaceDescription)other).hashCode()) // check the hashcode.
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
}
