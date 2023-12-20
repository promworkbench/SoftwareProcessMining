package software.designpattern.patterndefinition;

import java.util.HashSet;
import java.util.Set;



public class FactoryMethodPatternSet implements PatternSet{
	
	private Set<PatternClass> factoryPatternSet= new HashSet<PatternClass>();
	
	public Set<PatternClass> getPatternSet() {
		return factoryPatternSet;
	}

	public void setPatternSet(Set<PatternClass> factoryPatternSet) {
		this.factoryPatternSet = factoryPatternSet;
	}
	
	public void add(PatternClass fpc)
	{
		factoryPatternSet.add(fpc);
	}

	public int size() {
		// TODO Auto-generated method stub
		return factoryPatternSet.size();
	}

	public void addPatternSet(Set<PatternClass> patternSet) {
		// TODO Auto-generated method stub
		factoryPatternSet.addAll(patternSet);
	}
}
