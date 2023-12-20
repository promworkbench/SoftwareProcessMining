package software.designpattern.dynamicdiscovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.model.XLog;

import UtilityClasses.ClassClass;
import UtilityClasses.ClassTypeHierarchy;
import UtilityClasses.MethodClass;


public class StateStaticDiscovery {
	/*
	 * this method aims to discover a set of state design pattern candidates directly from software log. 
	 */
	public ArrayList<HashMap<String, Object>> DiscoverCompleteStatePattern(XLog softwareLog, ClassTypeHierarchy cth)
	{
		//store the final candidates that are detected by dynamic analysis from execution log. 
		 ArrayList<HashMap<String, Object>> result = new ArrayList<>();
		 
		 //for each possible Context + State combination, we have one role2values
		 HashMap<String, ArrayList<Object>> role2values = new HashMap<>();
		 //initialize the keys of the map
		 role2values.put("Context", new ArrayList<>());
		 role2values.put("State", new ArrayList<>());
		 role2values.put("setState", new ArrayList<>());
		 role2values.put("request", new ArrayList<>());
		 role2values.put("handle", new ArrayList<>());
		 
		 //define some temps
		 HashSet<MethodClass> setStateSet = new HashSet<>();
		 HashSet<MethodClass> requestSet = new HashSet<>();
		 HashSet<MethodClass> handleSet = new HashSet<>();
		 //the method set of context and state. 
		 HashSet<MethodClass> contextMethods =new HashSet<>();
		 HashSet<MethodClass> stateMethods = new HashSet<>();
		 
		 //we first compute the mapping from cth.getAllCTH() to its method set. 
		 final HashMap<HashSet<ClassClass>, HashSet<MethodClass>> classSet2MethodSet = new HashMap<>();
		 //the mapping from method to its parameter types
		 final HashMap<MethodClass, HashSet<ClassClass>> methodSet2ParameterSet = new HashMap<>();
		 //the mapping from method to its callee method set. 
		 final HashMap<MethodClass, HashSet<MethodClass>> methodSet2calleeMethodSet = new HashMap<>();

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
			 //the method set of subjects, should have >=2 methods @structural1: the context should include at least 2 methods
			 
			if(classSet2MethodSet.get(contexts).size()>=2)
			 {
				 //for the state classes
				 for(HashSet<ClassClass> states: cth.getAllCTH())
				 {
					 if(!states.equals(contexts))//the same class cannot be used both as context and state 
					 {
						 flag =0;//check if the current pare of Context+State is a candidate
						 //the method set of contexts
						 contextMethods =classSet2MethodSet.get(contexts);
						 //the method set of states
						 stateMethods =classSet2MethodSet.get(states);

						 setStateSet.clear();
						 
						 //setState
						 for(MethodClass setState: contextMethods)
						 {

							//The parameter set of m @structural2: the state class should be a parameter of setState
							for(ClassClass para: methodSet2ParameterSet.get(setState))
							{
								if(states.contains(para))//if a method has a parameter class that is of state class, it may be a setState
								{
									setStateSet.add(setState);
								}
							}
						 }
						 
						if(setStateSet.size()>=1)//@structural3: there should at lease exist a setState method
						{
							requestSet.clear();
							handleSet.clear();
							//request->handle 
							HashSet<MethodClass> tempContextMethods = (HashSet<MethodClass>) contextMethods.clone();
							tempContextMethods.removeAll(setStateSet);//the candidate request set is obtained by removing all setState from the method set of context
							for(MethodClass requestM: tempContextMethods) //@structural4: request method should invoke handle method
							{
								for(MethodClass handleM:methodSet2calleeMethodSet.get(requestM))
								{
									if(stateMethods.contains(handleM)){
										flag =1;
										requestSet.add(requestM);//add candidate request method
										handleSet.add(handleM);//add candidate handle method
									}
								}
							}
						}
						if(flag==1)//add the subject, observer, notify, update, reg and unreg
						{
							//clear the values of each roles
							role2values.get("Context").clear();
							role2values.get("State").clear();
							role2values.get("setState").clear();
							role2values.get("request").clear();
							role2values.get("handle").clear();

							//add subject, observer, notify, update, reg and unreg
							role2values.get("Context").add(contexts.toArray()[0]);
							role2values.get("State").add(states.toArray()[0]);
							role2values.get("setState").addAll(setStateSet);
							role2values.get("request").addAll(requestSet);
							role2values.get("handle").addAll(handleSet);
							
//							System.out.println("Candidates: "+role2values);
							//get the combination of all kinds of values, each combination is a candidate pattern instances
							for(HashMap<String, Object> candidate: CandidateCombination.combination(role2values))
							{
								result.add(candidate);
							}
						}
		
					 }//if state
				 }//for states
			 }
		 }//for contexts
		 return result;
		
	}
}
