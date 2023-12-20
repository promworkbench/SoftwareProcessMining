package software.designpattern.patterndefinition;

import java.util.HashSet;
import java.util.Set;

/**
 * this class defines a set of singleton pattern instances detected from the log. 
 * @author cliu3
 *
 */
public class SingletonPatternSet implements PatternSet{
	
	private Set<PatternClass> singletonPatternSet= new HashSet<PatternClass>();

	public Set<PatternClass> getPatternSet() {
		return singletonPatternSet;
	}

	public void setPatternSet(Set<PatternClass> singletonPatternSet) {
		this.singletonPatternSet = singletonPatternSet;
	}
	
	public void add(PatternClass opc)
	{
		singletonPatternSet.add(opc);
	}

	public int size() {
		// TODO Auto-generated method stub
		return singletonPatternSet.size();
	}

	public void addPatternSet(Set<PatternClass> patternSet) {
		// TODO Auto-generated method stub
		
		singletonPatternSet.addAll(patternSet);
		
	}


}
