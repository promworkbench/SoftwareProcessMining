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

public class StrategyBehevioralChecking {
	/*
	 * for each complete strategy pattern candidate, we (1) first identify its invocation; and (2) check the behavior constraints. 
	 */
	public PatternSet StrategyPatternInvocationConstraintsChecking(XFactory factory, XLog softwareLog, ClassTypeHierarchy cth, ArrayList<HashMap<String, Object>> result)
	{
		// intermediate results that are complete candidates
		PatternSet discoveredCandidateInstanceSet = new PatternSetImpl(); 
		
		for(int i = 0; i<result.size(); i++)//each result(i) is a candidate pattern instance
		{	
//			System.out.println(result.get(i));
			//get the strategy class type 
			HashSet<String> StrategyClassTypeHierarchy = new HashSet<>();
			for(ClassClass c:BasicOperators.typeHierarchyClassSet(cth, (ClassClass)result.get(i).get("Strategy")))
			{
				StrategyClassTypeHierarchy.add(c.getPackageName()+"."+c.getClassName());
			}
			
			//get the Context class type 
			HashSet<String> ContextClassTypeHierarchy = new HashSet<>();
			for(ClassClass c: BasicOperators.typeHierarchyClassSet(cth, (ClassClass)result.get(i).get("Context")))
			{
				ContextClassTypeHierarchy.add(c.getPackageName()+"."+c.getClassName());
			}
			
			//identify the invocations for each state pattern instance,
			HashSet<XTrace> invocationTraces = InvocationConstruction.strategyPatternInvocation(softwareLog, factory, cth, result.get(i));
			
			int numberofValidatedInvocation = 0;//the number of validated pattern invocations. 
			//for each invocation, we check the behavioral constraints.
			for(XTrace invocation: invocationTraces)
			{
				//@cardinality constraints: check invocation level constraints, contextInterface>=1 and algorithmInterface>=1;
				//no restriction for the setStrategy
				
				if(BasicOperators.MethodcallNumberPerTrace(((MethodClass)result.get(i).get("contextInterface")), invocation)<1
						||BasicOperators.MethodcallNumberPerTrace(((MethodClass)result.get(i).get("algorithmInterface")), invocation)<1)
				{
					continue;
				}
					
				//@invocation-constraint 1: the strategy change can only be done by strategy or context
				//the set of all setstrategy
				HashSet<XEvent> setStrategyEventSet =  BasicOperators.eventSetofMethodPerInvocation(invocation,
						((MethodClass)result.get(i).get("setStrategy")).getMethodName());
				if(setStrategyEventSet.size()!=0)
				{
					//for each one, its caller class should not be Strategy or context.
					int validedsetStrategy=0;
					for(XEvent setStrategyE: setStrategyEventSet)
					{
						String tempCallerClass = XSoftwareExtension.instance().extractCallerpackage(setStrategyE)+"."+XSoftwareExtension.instance().extractCallerclass(setStrategyE);
						if(!ContextClassTypeHierarchy.contains(tempCallerClass)
								&&!StrategyClassTypeHierarchy.contains(tempCallerClass))
						{
							validedsetStrategy++;
						}
					}
					if(validedsetStrategy!=setStrategyEventSet.size())//not all setStrategy are validated 
					{
						continue;
					}
				}
				
				
				//@invocation-constraint 2: contextInterface may invoke the algorithmInterface method of exactly one strategy class objects. 
				if(setStrategyEventSet.size()!=0)
				{
					int validedsetStrategy=0;
					for(XEvent setStrategyE: setStrategyEventSet)
					{
						//get the index of the next setState
						int nextSetStrategyEventIndex=BasicOperators.getNextEventAfterIndexX(invocation, 
								invocation.indexOf(setStrategyE), ((MethodClass)result.get(i).get("setStrategy")));
						if(nextSetStrategyEventIndex!=-1)//there exists a setStrategyEvent after the current one
						{
							//for each set Strategy event, we get the contextInterface after it. 
							XEvent firstContextInterfaceEvent =BasicOperators.getFirstEventAfterIndexABeforeIndexB(invocation, invocation.indexOf(setStrategyE),
									nextSetStrategyEventIndex, ((MethodClass)result.get(i).get("contextInterface")));
							if(firstContextInterfaceEvent==null)//there do not exist such a contextInterface event
							{
								continue;
							}
							else 
							{
								//get the callee object set of the invoked handle method of request
								HashSet<String> calleeObjectofAlgorithmInterface =BasicOperators.calleeObjectSetofInvokedEventsPerTraceV1(firstContextInterfaceEvent, invocation);
								//for each setState, we get its parameter mapping
								HashMap<ClassClass, String> paraMappingofsetStrategyEvent = BasicOperators.constructParameterMapping(setStrategyE);
								for(ClassClass c:paraMappingofsetStrategyEvent.keySet())
								{
									if(StrategyClassTypeHierarchy.contains(c.toString()))
									{
										if(calleeObjectofAlgorithmInterface.contains(paraMappingofsetStrategyEvent.get(c)))
										{
											validedsetStrategy++;
											break;
										}
										
									}
								}
							}
						}
						else//this is the last setState
						{
							validedsetStrategy++;
						}
					}

					if(validedsetStrategy!=setStrategyEventSet.size())//not all setState are validated, we allow the last setState may not have handle.  
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
			
			PatternClass NewP = BasicFunctions.CreatePatternInstance("Strategy Pattern", result.get(i), softwareLog.size(), numberofValidatedInvocation);
			discoveredCandidateInstanceSet.add(NewP);
		}
		return discoveredCandidateInstanceSet;
	}
	
}
