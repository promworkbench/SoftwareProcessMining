package software.designpattern.combinationdiscovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.events.Logger.MessageLevel;

import UtilityClasses.ClassClass;
import UtilityClasses.ClassTypeHierarchy;
import UtilityClasses.MethodClass;
import openXESsoftwareextension.XSoftwareExtension;
import software.designpattern.behavioralchecking.InvocationConstruction;
import software.designpattern.dynamicdiscovery.CandidateCombination;
import software.designpattern.patterndefinition.PatternClass;
import software.designpattern.patterndefinition.PatternSet;
import software.designpattern.patterndefinition.PatternSetImpl;

/*
 * this class defines the two main functions to (1) discover complete description of observer pattern; (2) check behavioral constraints. 
 */
public class StatePatternDiscoveryAndChecking {
	/* for state pattern, the context, state and request are included while setState and handle are missing.
	 * the DiscoveryCompleteStatePattern aims to find all missing roles from the execution log. 
	 */
	public ArrayList<HashMap<String, Object>> DiscoverCompleteStatePattern(UIPluginContext context, PatternClass patternCandidate, XLog softwareLog, ClassTypeHierarchy cth, HashMap<String, ArrayList<Object>> role2values)
	{
		//for those role with value, we need first make sure the values are also included in the log.
		//for context, we extend the class set to all classes with typehierarchy information
		if(!BasicOperators.classIncludedInLog((ClassClass)role2values.get("Context").get(0), softwareLog))//the class is not included in the log
		{
			context.log("The value of Context is not included in the execution log!", MessageLevel.WARNING);
		}
		//get all classes that of typehierarchy with the context class, and also included in the log.
		HashSet<ClassClass> alternativeContextClassSet =BasicOperators.typeHierarchyClassSetInLog(cth, softwareLog, (ClassClass)role2values.get("Context").get(0));

		if(alternativeContextClassSet.size()!=0){
			for(ClassClass cc: alternativeContextClassSet)
			{
				if(!role2values.get("Context").contains(cc))
				{
					role2values.get("Context").add(cc);
				}
			}
		}

		//for state
		if(!BasicOperators.classIncludedInLog((ClassClass)role2values.get("State").get(0), softwareLog))//the class is not included in the log
		{
			context.log("The value of State is not included in the execution log!", MessageLevel.WARNING);
		}
		//get all classes that of typehierarchy with the context class, and also included in the log.
		HashSet<ClassClass> alternativeStateClassSet =BasicOperators.typeHierarchyClassSetInLog(cth, softwareLog, (ClassClass)role2values.get("State").get(0));

		if(alternativeStateClassSet.size()!=0){
			for(ClassClass cc: alternativeStateClassSet)
			{
				if(!role2values.get("State").contains(cc))
				{
					role2values.get("State").add(cc);
				}
			}
		}

		//for request
		if(!BasicOperators.methodIncludedInLog((MethodClass)role2values.get("request").get(0), softwareLog))// the method is not included in the log
		{
			context.log("The value of request is not included in the execution log!", MessageLevel.WARNING);
		}
		HashSet<MethodClass> alternativeMethodSet =BasicOperators.typeHierarchyMethodSetInLog(cth, softwareLog, (MethodClass)role2values.get("request").get(0));
		
		if(alternativeMethodSet.size()!=0){
			for(MethodClass mm: alternativeMethodSet)
			{
				if(!role2values.get("request").contains(mm))
				{
					role2values.get("request").add(mm);
				}
			}
		}
		
		//for the setState role, (1) it is a method of the context; (2) it should include a parameter of State type; (3) the init() should not be included
		HashSet<MethodClass> methodSetofContext = new HashSet<MethodClass>();
		//get the method set of context role 
		for(Object c: role2values.get("Context"))
		{
			methodSetofContext.addAll(BasicOperators.MethodSetofClass((ClassClass)c, softwareLog));//get all type hierarchy classes
		}
		
		//only select those with state class as an input parameter type. 
		HashSet<ClassClass> stateClassTypeHierarchy = new HashSet<>();
		for(Object stateClass: role2values.get("State"))
		{
			stateClassTypeHierarchy.addAll(software.designpattern.behavioralchecking.BasicOperators.typeHierarchyClassSet(cth, (ClassClass)stateClass));
		}
		for(MethodClass m: methodSetofContext)
		{
			if(!m.getMethodName().equals("init()"))//init()should not be included
			{
				System.out.println(m);
				//The parameter set of m
				for(ClassClass p: BasicOperators.ParameterSetofMethod(m, softwareLog))
				{
					if(stateClassTypeHierarchy.contains(p)){//if a method has a parameter class that is of  context class, it may be a setState class
						if(!role2values.get("setState").contains(m))
						{
							role2values.get("setState").add(m);
						}
						System.out.println(m);
						break;
					}
				}
			}
		}

		//for the handle role, (1) it is a method of the state; (2) it is invoked by the request method
		HashSet<MethodClass> methodSetofState = new HashSet<MethodClass>();
		//get the method set of state role 
		for(Object c: role2values.get("State"))
		{
			methodSetofState.addAll(BasicOperators.MethodSetofClass((ClassClass)c, softwareLog));//get all type hierarchy classes
		}
		
		//get the method set invoked by request.
		HashSet<MethodClass> methodSetInovkedByRequest = new HashSet<MethodClass>();
		for(Object m: role2values.get("request"))
		{
			methodSetInovkedByRequest.addAll(BasicOperators.MethodSetofMethod((MethodClass)m, softwareLog));
		}
		
		for(MethodClass m: methodSetInovkedByRequest)
		{
			if(!m.getMethodName().equals("init()"))//init()should not be included
			{
				if(methodSetofState.contains(m)){
					if(!role2values.get("handle").contains(m))
					{
						role2values.get("handle").add(m);
					}
				}
			}
		}
		
		//till now the state pattern candidate should be complete, each role may have multiple values.  
		//if there still exist role without value, we say this candidate is invalid according to the log.  
		for(String role: role2values.keySet())
		{
			//if the role is still missing, we just return null for the current pattern instance.
			//for state pattern, the setState role can be empty
			if(role2values.get(role).size()==0){
				context.log(role+" is missing values according to the execution log for the current pattern instance!", MessageLevel.WARNING);
				return null;
			}
		}
		
		//get the combination of all kinds of values, each combination is a candidate pattern instances
		ArrayList<HashMap<String, Object>> result =CandidateCombination.combination(role2values);
		
//		for(int i = 0; i<result.size(); i++)
//		{	
//			PatternClass NewP = BasicOperators.CreatePatternInstance(patternCandidate, result.get(i), softwareLog.size(), 0);
//			discoveredCandidateInstanceSet.add(NewP);
//		}
		return result;
	}

	/*
	 * for each complete state pattern candidate, we (1) first identify its invocation; and (2) check the behavior constraints. 
	 */
	public PatternSet StatePatternInvocationConstraintsChecking(UIPluginContext context, XFactory factory, PatternClass patternCandidate, XLog softwareLog, ClassTypeHierarchy cth, ArrayList<HashMap<String, Object>> result)
	{
		if(result==null)//if there is no complete candidates discovered, return null.
		{
			return null;
		}
		
		// intermediate results that are complete candidates
		PatternSet discoveredCandidateInstanceSet = new PatternSetImpl(); 
		
		for(int i = 0; i<result.size(); i++)//each result(i) is a candidate pattern instance
		{	
			//get the state class type as string 
			HashSet<String> StateClassTypeHierarchy = new HashSet<>();
			for(ClassClass c:software.designpattern.behavioralchecking.BasicOperators.typeHierarchyClassSet(cth, (ClassClass)result.get(i).get("State")))
			{
				StateClassTypeHierarchy.add(c.getPackageName()+"."+c.getClassName());
			}
			
			//get the Context class type as string 
			HashSet<String> ContextClassTypeHierarchy = new HashSet<>();
			for(ClassClass c: software.designpattern.behavioralchecking.BasicOperators.typeHierarchyClassSet(cth, (ClassClass)result.get(i).get("Context")))
			{
				ContextClassTypeHierarchy.add(c.getPackageName()+"."+c.getClassName());
			}
			
			//identify the invocations for each state pattern instance,
			HashSet<XTrace> invocationTraces = InvocationConstruction.statePatternInvocation(softwareLog, factory, cth, result.get(i));
			
			int numberofValidatedInvocation = 0;//the number of validated pattern invocations. 
			//for each invocation, we check the behavioral constraints.
			for(XTrace invocation: invocationTraces)
			{
				//@cardinality constraints: check invocation level constraints, request>=1 and handle >=1, and setState>=1;
				if(software.designpattern.behavioralchecking.BasicOperators.MethodcallNumberPerTrace(((MethodClass)result.get(i).get("handle")), invocation)<1
						||software.designpattern.behavioralchecking.BasicOperators.MethodcallNumberPerTrace(((MethodClass)result.get(i).get("request")), invocation)<1
						||software.designpattern.behavioralchecking.BasicOperators.MethodcallNumberPerTrace(((MethodClass)result.get(i).get("setState")), invocation)<1)
				{
					continue;
				}
					
				//@invocation-constraint 1: the state change can only be done by state or context
				//the set of all setState (after the first request method), i.e., all state changes.
				HashSet<XEvent> setStateEvents = software.designpattern.behavioralchecking.BasicOperators.setStateMethodCallSet(invocation, 
						((MethodClass)result.get(i).get("request")),
						((MethodClass)result.get(i).get("setState")));
				if(setStateEvents.size()!=0)
				{
					//for each one, its caller class should be state or context.
					int validedsetState=0;
					for(XEvent setStateE: setStateEvents)
					{
						String tempCallerClass = XSoftwareExtension.instance().extractCallerpackage(setStateE)+"."+XSoftwareExtension.instance().extractCallerclass(setStateE);
						if(ContextClassTypeHierarchy.contains(tempCallerClass)
								||StateClassTypeHierarchy.contains(tempCallerClass))
						{
							validedsetState++;
						}
					}

					if(validedsetState!=setStateEvents.size())//not all setstates are validated 
					{
						continue;
					}
				} 
					
				
				//@invocation-constraint 2: after state change the request method should invoke the handle method of the new state object
				//an exception case is that: for the last setState, there is no need to check the request
				HashSet<XEvent> setStateEventSet = software.designpattern.behavioralchecking.BasicOperators.eventSetofMethodPerInvocation(invocation,
						((MethodClass)result.get(i).get("setState")));		
				
				if(setStateEventSet.size()!=0)
				{
					int validedsetState=0;
					for(XEvent setStateE: setStateEventSet)
					{	//get the index of the next setState
						int nextSetStateEventIndex=software.designpattern.behavioralchecking.BasicOperators.getNextEventAfterIndexX(invocation, 
								invocation.indexOf(setStateE), ((MethodClass)result.get(i).get("setState")));
						if(nextSetStateEventIndex!=-1)//there exists a setStateEvent after the current one
						{
							//for each set state event, we get the request after it. 
							XEvent firstRequestEvent =software.designpattern.behavioralchecking.BasicOperators.getFirstEventAfterIndexABeforeIndexB(invocation, invocation.indexOf(setStateE),
									nextSetStateEventIndex, ((MethodClass)result.get(i).get("request")));
							if(firstRequestEvent==null)//there do not exist such a request event
							{
								continue;
							}
							else 
							{
								//get the callee object set of the invoked handle method of request
								HashSet<String> calleeObjectofHandle =software.designpattern.behavioralchecking.BasicOperators.calleeObjectSetofInvokedEventsPerTraceV1(firstRequestEvent, invocation);
								//for each setState, we get its parameter mapping
								HashMap<ClassClass, String> paraMappingofsetStateEvent = software.designpattern.behavioralchecking.BasicOperators.constructParameterMapping(setStateE);
								for(ClassClass c:paraMappingofsetStateEvent.keySet())
								{
									if(StateClassTypeHierarchy.contains(c.toString()))
									{
										if(calleeObjectofHandle.contains(paraMappingofsetStateEvent.get(c)))
										{
											validedsetState++;
											break;
										}	
									}
								}
							}
						}
						else//this is the last setState
						{
							validedsetState++;
						}
					}
					
					if(validedsetState!=setStateEventSet.size())//not all setState are validated, we allow the last setState may not have handle.  
					{
						continue;
					}
				}
				
				numberofValidatedInvocation++;
			}
			
			if(numberofValidatedInvocation==0)//if there is no validated invocation, then the current candidate is not approved.
			{
				continue;
			}
			
			PatternClass NewP = BasicOperators.CreatePatternInstanceCombination(patternCandidate, result.get(i), softwareLog.size(), numberofValidatedInvocation);
			discoveredCandidateInstanceSet.add(NewP);
		}
		return discoveredCandidateInstanceSet;
	}
}
