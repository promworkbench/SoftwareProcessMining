package software.designpattern.dynamicdiscovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.model.XLog;

import UtilityClasses.ClassClass;
import UtilityClasses.ClassTypeHierarchy;
import UtilityClasses.MethodClass;

public class ObserverStaticDiscovery {
	/*
	 * this method aims to discover a set of observer design pattern candidates directly from software execution data. 
	 */
	public ArrayList<HashMap<String, Object>> DiscoverCompleteObserverPattern(XLog softwareLog, ClassTypeHierarchy cth)
	{
		//store the final candidates that are detected by dynamic analysis from execution log. 
		 ArrayList<HashMap<String, Object>> result = new ArrayList<>();
		 
		 //for each possible Subject + Observer combination, we have one role2values
		 HashMap<String, ArrayList<Object>> role2values = new HashMap<>();
		 //initialize the keys of the map
		 role2values.put("Subject", new ArrayList<>());
		 role2values.put("Observer", new ArrayList<>());
		 role2values.put("update", new ArrayList<>());
		 role2values.put("notify", new ArrayList<>());
		 role2values.put("register", new ArrayList<>());
		 role2values.put("unregister", new ArrayList<>());
		 
		 //define some temps
		 HashSet<MethodClass> regUnregSet = new HashSet<>();
		 HashSet<MethodClass> notifySet = new HashSet<>();
		 HashSet<MethodClass> updateSet = new HashSet<>();
		 //the method set of subjects and observers
		 HashSet<MethodClass> subjectMethods =new HashSet<>();
		 HashSet<MethodClass> observerMethods = new HashSet<>();
		 
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
		 
		 int flag =0;//if a group of candidates that satisfies the structural constraints are found, we have flag =1.
		 
		 for(HashSet<ClassClass> subjects: cth.getAllCTH())// for each group of classes->candidate subject 
		 {
			 //the method set of subjects, should have >=3 methods @structural1: the subject should include as least 3 methods
			 if(classSet2MethodSet.get(subjects).size()>=3)
			 {
				 //for the observer classes
				 for(HashSet<ClassClass> observers: cth.getAllCTH())
				 {
					 if(!observers.equals(subjects))//the same class cannot be used both as subject and observers
					 {
						 flag =0;//check if the current pare of subject+observer is a candidate
						 //the method set of subjects
						 subjectMethods =classSet2MethodSet.get(subjects);
						 //the method set of observers
						 observerMethods =classSet2MethodSet.get(observers);
						 regUnregSet.clear();
						 
						 //register and unregister
						 for(MethodClass regUnreg: subjectMethods)
						 {
							//The parameter set of m @structural2: the observer class should be a parameter of reg/unreg
							for(ClassClass para: methodSet2ParameterSet.get(regUnreg))
							{
								if(observers.contains(para))//if a method has a parameter class that is of observer class, it may be a register/unregister class
								{
									regUnregSet.add(regUnreg);
								}
							}
						 }
						 
						if(regUnregSet.size()>=2)//@structural3: both reg and unreg should be identified
						{
							notifySet.clear();
							updateSet.clear();
							//notify->update 
							HashSet<MethodClass> tempsubjectMethods = (HashSet<MethodClass>) subjectMethods.clone();
							tempsubjectMethods.removeAll(regUnregSet);//the candidate notify set is obtained by removing all reg/ung from the method set of subject
							for(MethodClass notifyM: tempsubjectMethods) //@structural4: notify method should invoke update method, and not include a parameter of observer class
							{
								for(MethodClass updateM:methodSet2calleeMethodSet.get(notifyM))
								{
									if(observerMethods.contains(updateM)){
										flag =1;
										updateSet.add(updateM);//add candidate update method
										notifySet.add(notifyM);//add candidate notify method
									}
								}
							}
						}
						
						if(flag==1)//add the subject, observer, notify, update, reg and unreg
						{
							//clear the values of each roles
							role2values.get("register").clear();
							role2values.get("unregister").clear();
							role2values.get("update").clear();
							role2values.get("notify").clear();
							role2values.get("Observer").clear();
							role2values.get("Subject").clear();

							//add subject, observer, notify, update, reg and unreg
							role2values.get("Subject").add(subjects.toArray()[0]);
							role2values.get("Observer").add(observers.toArray()[0]);
							role2values.get("update").addAll(updateSet);
							role2values.get("notify").addAll(notifySet);
							role2values.get("register").addAll(regUnregSet);
							role2values.get("unregister").addAll(regUnregSet);
							
							//get the combination of all kinds of values, each combination is a candidate pattern instances
							for(HashMap<String, Object> candidate: CandidateCombination.combination(role2values))
							{
								if(!candidate.get("register").equals(candidate.get("unregister")))//the register != unregister
								result.add(candidate);
							}
						}
		
					 }//if observer
				 }//for observers
			 }
		 }//for subjects
		 
		 return result;
	}

}
