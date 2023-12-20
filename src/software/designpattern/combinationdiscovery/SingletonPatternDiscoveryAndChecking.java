package software.designpattern.combinationdiscovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.events.Logger.MessageLevel;

import UtilityClasses.ClassClass;
import UtilityClasses.ClassTypeHierarchy;
import UtilityClasses.MethodClass;
import software.designpattern.behavioralchecking.InvocationConstruction;
import software.designpattern.dynamicdiscovery.CandidateCombination;
import software.designpattern.patterndefinition.PatternClass;
import software.designpattern.patterndefinition.PatternSet;
import software.designpattern.patterndefinition.PatternSetImpl;

/* for singleton pattern, the singleton class are included in the static tool while getInstance method is missing.
 * this class aims to find the missing role from the execution log.
 * Then check the behavioral constraints with respect the log. 
 */
public class SingletonPatternDiscoveryAndChecking {
	
	public ArrayList<HashMap<String, Object>> DiscoverCompleteSinglePattern(UIPluginContext context, PatternClass patternCandidate, XLog softwareLog, ClassTypeHierarchy cth, HashMap<String, ArrayList<Object>> role2values)
	{
		//for those role with value, we need first make sure the values are also included in the log.
		//for Singleton, we extend the class set to all classes with type hierarchy information
		if(!BasicOperators.classIncludedInLog((ClassClass)role2values.get("Singleton").get(0), softwareLog))//the class is not included in the log
		{
			context.log("The value of Singleton is not included in the execution log!", MessageLevel.WARNING);
			//return null; //if there exist missing value, then this pattern is not considered anymore. 
		}
		//get all classes that of typehierarchy with the Singleton class, and also included in the log.
		HashSet<ClassClass> alternativeSingletonClassSet =BasicOperators.typeHierarchyClassSetInLog(cth, softwareLog, (ClassClass)role2values.get("Singleton").get(0));
		if(alternativeSingletonClassSet.size()!=0){
			for(ClassClass cc: alternativeSingletonClassSet)
			{
				if(!role2values.get("Singleton").contains(cc))
				{
					role2values.get("Singleton").add(cc);
				}
			}
		}
		
		//for the getInstance(), it is a method of Singleton, and its return type is Singleton class. 
		HashSet<MethodClass> methodSetofSingleton = new HashSet<MethodClass>();
		
		//get the method set of Singleton role 
		for(Object c: role2values.get("Singleton"))
		{
			methodSetofSingleton.addAll(BasicOperators.MethodSetofClass((ClassClass)c, softwareLog));//get all type hierarchy classes
		}
		
		// the return type should be Singleton.
		for(MethodClass m: methodSetofSingleton)
		{
			if(!role2values.get("getInstance").contains(m))
			{role2values.get("getInstance").add(m);}
		}
	
		//till now the singleton pattern candidate should be complete, each role may have multiple values.  
		//if there still exist role without value, we say this candidate is invalid according to the log.  
		for(String role: role2values.keySet())
		{
			//if the role is still missing, we just return null for the current pattern instance.
			if(role2values.get(role).size()==0){
				context.log(role+" is missing values according to the execution log for the current pattern instance!", MessageLevel.WARNING);
				return null;
			}
		}
		
		//get the combination of all kinds of values, each combination is a candidate pattern instances
		ArrayList<HashMap<String, Object>> result =CandidateCombination.combination(role2values);
		
		
		return result;
	}
	
	
	
	/*
	 * for each complete observer pattern candidate, we (1) first identify its invocation; and (2) check the behavior constraints. 
	 * refer to the software.designpattern.behavioralchecking.ObserverBehavioralChecking implementation
	 */
	public PatternSet SingletonPatternBehavioralConstraintsChecking(UIPluginContext context, XFactory factory, PatternClass patternCandidate, XLog softwareLog, ClassTypeHierarchy cth, ArrayList<HashMap<String, Object>> result)
	{
		if(result==null)//if there is no complete candidates discovered, return null.
		{
			return null;
		}
		
		// intermediate results to store complete but not validated candidates
		PatternSet discoveredCandidateInstanceSet = new PatternSetImpl(); 
		
		for(int i = 0; i<result.size(); i++)//each result(i) is a candidate pattern instance
		{			
			//identify the invocations for each singleton pattern instance,
			HashSet<XTrace> invocationTraces = InvocationConstruction.singletonPatternInvocation(softwareLog, factory, cth, result.get(i));
			
			int numberofInvalidatedInvocation = 0;//the number of invalidated pattern invocations. 
			
			//for each invocation, we check the behavioral constraints.
			for(XTrace invocation: invocationTraces)
			{
				//@invocation-constraint 1: for each invocation, all getInstance has the same return value, not 0
				//@invocation-constraint 2: for each invocation, all method calls with Singleton as callee class have the same object, not 0.

				//get the return value set of all getInstance method. //get the callee object of all signleton class
				if(BasicOperators.getReturnValueSet(invocation, (MethodClass)result.get(i).get("getInstance")).size()>1
					||	BasicOperators.getCalleeObjectSet(invocation, (ClassClass)result.get(i).get("Singleton")).size()>1)
				{
					numberofInvalidatedInvocation++;
				}
			}
			
			if(numberofInvalidatedInvocation>0)//if there exists invalidated invocation, then the current candidate is not approved.
			{
				continue; //go to the next candidate
			}
			PatternClass NewP = BasicOperators.CreatePatternInstanceCombination(patternCandidate, result.get(i), softwareLog.size(), softwareLog.size());
			System.out.println(NewP);
			discoveredCandidateInstanceSet.add(NewP);
		}
		return discoveredCandidateInstanceSet;
	}
}
