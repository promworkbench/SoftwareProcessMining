package software.designpattern.behavioralchecking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import UtilityClasses.ClassClass;
import UtilityClasses.ClassTypeHierarchy;
import UtilityClasses.MethodClass;
import openXESsoftwareextension.XSoftwareExtension;
import software.designpattern.dynamicdiscovery.BasicFunctions;
import software.designpattern.patterndefinition.PatternClass;
import software.designpattern.patterndefinition.PatternSet;
import software.designpattern.patterndefinition.PatternSetImpl;

public class StateBehavioralChecking {
	/*
	 * for each complete state pattern candidate, we (1) first identify its invocation; and (2) check the behavior constraints. 
	 */
	public PatternSet StatePatternBehavioralConstraintsChecking(XFactory factory, XLog softwareLog, ClassTypeHierarchy cth, ArrayList<HashMap<String, Object>> result)
	{		
		// intermediate results that are complete candidates
		PatternSet discoveredCandidateInstanceSet = new PatternSetImpl(); 
		
		for(int i = 0; i<result.size(); i++)//each result(i) is a candidate pattern instance
		{	
			//get the state class type as string 
			HashSet<String> StateClassTypeHierarchy = new HashSet<>();
			for(ClassClass c:BasicOperators.typeHierarchyClassSet(cth, (ClassClass)result.get(i).get("State")))
			{
				StateClassTypeHierarchy.add(c.getPackageName()+"."+c.getClassName());
			}
			
			//get the Context class type as string 
			HashSet<String> ContextClassTypeHierarchy = new HashSet<>();
			for(ClassClass c: BasicOperators.typeHierarchyClassSet(cth, (ClassClass)result.get(i).get("Context")))
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
				if(BasicOperators.MethodcallNumberPerTrace(((MethodClass)result.get(i).get("handle")), invocation)<1
						||BasicOperators.MethodcallNumberPerTrace(((MethodClass)result.get(i).get("request")), invocation)<1
						||BasicOperators.MethodcallNumberPerTrace(((MethodClass)result.get(i).get("setState")), invocation)<1)
				{
					continue;
				}
					
				//@invocation-constraint 1: the state change can only be done by state or context
				//the set of all setState (after the first request method), i.e., all state changes.
				HashSet<XEvent> setStateEvents =  BasicOperators.setStateMethodCallSet(invocation, 
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
				HashSet<XEvent> setStateEventSet = BasicOperators.eventSetofMethodPerInvocation(invocation,
						((MethodClass)result.get(i).get("setState")));		
				
				if(setStateEventSet.size()!=0)
				{
					int validedsetState=0;
					for(XEvent setStateE: setStateEventSet)
					{	//get the index of the next setState
						int nextSetStateEventIndex=BasicOperators.getNextEventAfterIndexX(invocation, 
								invocation.indexOf(setStateE), ((MethodClass)result.get(i).get("setState")));
						if(nextSetStateEventIndex!=-1)//there exists a setStateEvent after the current one
						{
							//for each set state event, we get the request after it. 
							XEvent firstRequestEvent =BasicOperators.getFirstEventAfterIndexABeforeIndexB(invocation, invocation.indexOf(setStateE),
									nextSetStateEventIndex, ((MethodClass)result.get(i).get("request")));
							if(firstRequestEvent==null)//there do not exist such a request event
							{
								continue;
							}
							else 
							{
								//get the callee object set of the invoked handle method of request
								HashSet<String> calleeObjectofHandle =BasicOperators.calleeObjectSetofInvokedEventsPerTraceV1(firstRequestEvent, invocation);
								//for each setState, we get its parameter mapping
								HashMap<ClassClass, String> paraMappingofsetStateEvent = BasicOperators.constructParameterMapping(setStateE);
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
			
			PatternClass NewP = BasicFunctions.CreatePatternInstance("State Pattern", result.get(i), softwareLog.size(), numberofValidatedInvocation);
			discoveredCandidateInstanceSet.add(NewP);
		}
		return discoveredCandidateInstanceSet;
	}
	
	
}
