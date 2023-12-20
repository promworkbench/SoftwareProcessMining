package software.designpattern.dynamicdiscovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.model.XLog;

import UtilityClasses.ClassClass;
import UtilityClasses.ClassTypeHierarchy;
import UtilityClasses.MethodClass;


public class StrategyStaticDiscovery {
	/*
	 * this method aims to discover a set of Strategy design pattern candidates directly from software log. 
	 */
	public ArrayList<HashMap<String, Object>> DiscoverCompleteStrategyPattern(XLog softwareLog, ClassTypeHierarchy cth)
	{
		//store the final candidates that are detected by dynamic analysis from execution log. 
		 ArrayList<HashMap<String, Object>> result = new ArrayList<>();
		 
		 //for each possible Context + Strategy combination, we have one role2values
		 HashMap<String, ArrayList<Object>> role2values = new HashMap<>();
		 //initialize the keys of the map
		 role2values.put("Context", new ArrayList<>());
		 role2values.put("Strategy", new ArrayList<>());
		 role2values.put("setStrategy", new ArrayList<>());
		 role2values.put("contextInterface", new ArrayList<>());
		 role2values.put("algorithmInterface", new ArrayList<>());
		 
		 //define some temps
		 HashSet<MethodClass> setStrategySet = new HashSet<>();
		 HashSet<MethodClass> contextInterfaceSet = new HashSet<>();
		 HashSet<MethodClass> algorithmInterfaceSet = new HashSet<>();
		 //the method set of context and 
		 HashSet<MethodClass> contextMethods =new HashSet<>();
		 HashSet<MethodClass> strategyMethods = new HashSet<>();
		 
		//we first compute the mapping from cth.getAllCTH() to its method set. 
		 HashMap<HashSet<ClassClass>, HashSet<MethodClass>> classSet2MethodSet = new HashMap<>();
		 //the mapping from method to its parameter types
		 HashMap<MethodClass, HashSet<ClassClass>> methodSet2ParameterSet = new HashMap<>();
		 //the mapping from method to its callee method set. 
		 HashMap<MethodClass, HashSet<MethodClass>> methodSet2calleeMethodSet = new HashMap<>();

		 for(HashSet<ClassClass> cs: cth.getAllCTH())// for each group of classes->candidate subject 
		 {
			 //Method set of cs
			 HashSet<MethodClass> ms = BasicFunctions.MethodSetofClasses(cs, softwareLog);
			 classSet2MethodSet.put(cs, ms);
			 
			 //construct the parameter type and callee method set
			 for(MethodClass m: ms)
			 {
				 if(!methodSet2ParameterSet.keySet().contains(m))// the current m is not included in the mapping. 
				 {
					 methodSet2ParameterSet.put(m, BasicFunctions.ParameterSetofMethod(m, softwareLog));
				 }
				 
				 if(!methodSet2calleeMethodSet.keySet().contains(m))
				 {
					 methodSet2calleeMethodSet.put(m, BasicFunctions.MethodSetofMethod(m, softwareLog));
				 }
			 }
		 }
		 
		 int flag =0;//if a group of candidates that satisfies the structural constraints are found. 
		 
		 for(HashSet<ClassClass> contexts: cth.getAllCTH())// for each group of classes->candidate subject 
		 {
			 //the method set of contexts, should have >=2 methods @structural1: the context should include at least 2 methods
			 if(classSet2MethodSet.get(contexts).size()>=2)
			 {
				 //for the state classes
				 for(HashSet<ClassClass> strategies: cth.getAllCTH())
				 {
					 if(!strategies.equals(contexts))//the same class cannot be used both as context and strategy 
					 {
						 flag =0;//check if the current pare of Context+Strategy is a candidate
						 //the method set of contexts
						 contextMethods =classSet2MethodSet.get(contexts);
						 //the method set of states
						 strategyMethods =classSet2MethodSet.get(strategies);
						 
						 setStrategySet.clear();
						 
						 //setStrategy 
						 for(MethodClass setStrategy: contextMethods)
						 {
							//The parameter set of m @structural2: the Strategy class should be a parameter of setStrategy
							for(ClassClass para: methodSet2ParameterSet.get(setStrategy))
							{
								if(strategies.contains(para))//if a method has a parameter class that is of strategy class, it may be a setState
								{
									setStrategySet.add(setStrategy);
								}
							}
						 }
						 
						if(setStrategySet.size()>=1)//@structural3: there should at lease exist a setStrategy method
						{
							contextInterfaceSet.clear();
							algorithmInterfaceSet.clear();
							//contextInterface->algorithmInterface 
							HashSet<MethodClass> tempContextMethods = (HashSet<MethodClass>) contextMethods.clone();
							tempContextMethods.removeAll(setStrategySet);//the candidate contextInterface set is obtained by removing all setStrategy from the method set of context
							for(MethodClass contextInterfaceM: tempContextMethods) //@structural4: contextInterface method should invoke algorithmInterface method
							{
								for(MethodClass algorithmInterfaceM:methodSet2calleeMethodSet.get(contextInterfaceM))
								{
									if(strategyMethods.contains(algorithmInterfaceM)){
										flag =1;
										contextInterfaceSet.add(contextInterfaceM);//add candidate request method
										algorithmInterfaceSet.add(algorithmInterfaceM);//add candidate handle method
									}
								}
							}
						}
						if(flag==1)//add the subject, observer, notify, update, reg and unreg
						{
							//clear the values of each roles
							role2values.get("Context").clear();
							role2values.get("Strategy").clear();
							role2values.get("setStrategy").clear();
							role2values.get("contextInterface").clear();
							role2values.get("algorithmInterface").clear();

							//add subject, observer, notify, update, reg and unreg
							role2values.get("Context").add(contexts.toArray()[0]);
							role2values.get("Strategy").add(strategies.toArray()[0]);
							role2values.get("setStrategy").addAll(setStrategySet);
							role2values.get("contextInterface").addAll(contextInterfaceSet);
							role2values.get("algorithmInterface").addAll(algorithmInterfaceSet);
							
							//get the combination of all kinds of values, each combination is a candidate pattern instances
							for(HashMap<String, Object> candidate: CandidateCombination.combination(role2values))
							{
								result.add(candidate);
							}
						}
		
					 }//if strategy
				 }//for strategies
			 }
		 }//for contexts
		 return result;
		
	}	
}
