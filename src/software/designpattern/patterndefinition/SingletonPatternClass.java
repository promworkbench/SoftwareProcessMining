package software.designpattern.patterndefinition;

import java.util.Objects;

import UtilityClasses.ClassClass;
import UtilityClasses.MethodClass;

/**
 * this class defines the basic structure of the discovered observer pattern.
 * @author cliu3
 *
 */
public class SingletonPatternClass extends PatternClass{
	
	private ClassClass singletonClass =null;
	
	/*
	 * the getInstance method hould be added on the basis of event log.  
	 */
	
	private MethodClass getInstanceMethod = null;

	public ClassClass getSingletonClass() {
		return singletonClass;
	}

	public void setSingletonClass(ClassClass singletonClass) {
		this.singletonClass = singletonClass;
	}

	public MethodClass getGetInstanceMethod() {
		return getInstanceMethod;
	}

	public void setGetInstanceMethod(MethodClass getInstanceMethod) {
		this.getInstanceMethod = getInstanceMethod;
	}
	
	
	/**
	 * the equals determine the way to distinguish singleton pattern,
	 */
	public int hashCode() {  
        return 2*Objects.hash(singletonClass)+3*Objects.hash(getInstanceMethod);
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
		if (!(other instanceof SingletonPatternClass))
		{
			return false;
		}
		if (this.hashCode()==((SingletonPatternClass)other).hashCode()) // check the hashcode.
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	// write all informations
	public String toString() 
	{
		return this.singletonClass+","+this.getInstanceMethod;
		
	}

	
}
