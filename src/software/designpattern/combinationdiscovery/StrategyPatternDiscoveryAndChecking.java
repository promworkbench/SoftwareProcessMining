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
public class StrategyPatternDiscoveryAndChecking {
	/* for strategy pattern, the context, strategy and contextInterface are included while the algorithmInterface and setStrategy are missing.
	 * the DiscoveryCompleteStatePattern aims to find all missing roles from the execution log. 
	 */
	
	public ArrayList<HashMap<String, Object>> DiscoverCompleteStrategyPattern(UIPluginContext context, PatternClass patternCandidate, XLog softwareLog, ClassTypeHierarchy cth, HashMap<String, ArrayList<Object>> role2values)
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
			for(ClassClass cc:alternativeContextClassSet)// to avoid duplicated elements
			{
				if(!role2values.get("Context").contains(cc))
					role2values.get("Context").add(cc);
			}
		}

		//for strategy
		if(!BasicOperators.classIncludedInLog((ClassClass)role2values.get("Strategy").get(0), softwareLog))//the class is not included in the log
		{
			context.log("The value of Strategy is not included in the execution log!", MessageLevel.WARNING);
		}
		//get all classes that of typehierarchy with the context class, and also included in the log.
		HashSet<ClassClass> alternativeStrategyClassSet =BasicOperators.typeHierarchyClassSetInLog(cth, softwareLog, (ClassClass)role2values.get("Strategy").get(0));

		if(alternativeStrategyClassSet.size()!=0){
			for(ClassClass cc:alternativeStrategyClassSet)// to avoid duplicated elements
			{
				if(!role2values.get("Strategy").contains(cc))
					role2values.get("Strategy").add(cc);
			}
			
		}
				
		//for contextInterface
		if(!BasicOperators.methodIncludedInLog((MethodClass)role2values.get("contextInterface").get(0), softwareLog))// the method is not included in the log
		{
			context.log("The value of contextInterface is not included in the execution log!", MessageLevel.WARNING);
		}
		HashSet<MethodClass> alternativeMethodSet =BasicOperators.typeHierarchyMethodSetInLog(cth, softwareLog, (MethodClass)role2values.get("contextInterface").get(0));
		
		if(alternativeMethodSet.size()!=0){
			for(MethodClass mm: alternativeMethodSet)
			{
				if(!role2values.get("contextInterface").contains(mm))
					role2values.get("contextInterface").add(mm);
			}
		}
		
		//for the setStrategy role, (1) it is a method of the context; (2) it should include a parameter of Strategy type; and (3)it cannot be the init().
		HashSet<MethodClass> methodSetofContext = new HashSet<MethodClass>();
		//get the method set of context role 
		for(Object c: role2values.get("Context"))
		{
			methodSetofContext.addAll(BasicOperators.MethodSetofClass((ClassClass)c, softwareLog));//get all type hierarchy classes
		}
		
		//only select those with Strategy class as an input parameter type. 
		HashSet<ClassClass> strategyClassTypeHierarchy = new HashSet<>();
		for(Object strategyClass: role2values.get("Strategy"))
		{
			strategyClassTypeHierarchy.addAll(software.designpattern.behavioralchecking.BasicOperators.typeHierarchyClassSet(cth, (ClassClass)strategyClass));
		}
		for(MethodClass m: methodSetofContext)
		{
			if(!m.getMethodName().equals("init()"))//init()should not be included
			{
				System.out.println(m);
				//The parameter set of m
				for(ClassClass p: BasicOperators.ParameterSetofMethod(m, softwareLog))
				{
					if(strategyClassTypeHierarchy.contains(p)){//if a method has a parameter class that is of  context class, it may be a setState class
						if(!role2values.get("setStrategy").contains(m))
						{
							role2values.get("setStrategy").add(m);
						}
						System.out.println(m);
						break;
					}
				}
			}
	
		}
				
		//for the algorithmInterface role, (1) it is a method of the strategy; (2) it is invoked by the contextInterface method
		HashSet<MethodClass> methodSetofStrategy = new HashSet<MethodClass>();
		//get the method set of strategy role 
		for(Object c: role2values.get("Strategy"))
		{
			methodSetofStrategy.addAll(BasicOperators.MethodSetofClass((ClassClass)c, softwareLog));//get all type hierarchy classes
		}
		
		//get the method set invoked by contextInterface.
		HashSet<MethodClass> methodSetInovkedByContextInterface = new HashSet<MethodClass>();
		for(Object m: role2values.get("contextInterface"))
		{
			methodSetInovkedByContextInterface.addAll(BasicOperators.MethodSetofMethod((MethodClass)m, softwareLog));
		}
		
		for(MethodClass m: methodSetInovkedByContextInterface)
		{
			if(!m.getMethodName().equals("init()"))//init() should not be included
			{
				if(methodSetofStrategy.contains(m)&&!role2values.get("algorithmInterface").contains(m)){
					role2values.get("algorithmInterface").add(m);
				}
			}
		}
		
		//till now the strategy pattern candidate should be complete, each role may have multiple values.  
		//if there still exist role without value, we say this candidate is invalid according to the log.  
		for(String role: role2values.keySet())
		{
			//if the role is still missing, we just return null for the current pattern instance.
			//for strategy pattern, the setStrategy role can be empty
			if(role2values.get(role).size()==0)
			{
				if(role.equals("setStrategy"))
				{
					role2values.get("setStrategy").add(new MethodClass());// add default value, to ensure the combination operation work.
				}
				else
				{
					context.log(role+" is missing values according to the execution log for the current pattern instance!", MessageLevel.WARNING);
					return null;
				}
			}
		}
		
		//get the combination of all kinds of values, each combination is a candidate pattern instances
		ArrayList<HashMap<String, Object>> result =CandidateCombination.combination(role2values);
		System.out.println(role2values);
		System.out.println(result.size());
		return result;
	}
	/*
	 * for each complete strategy pattern candidate, we (1) first identify its invocation; and (2) check the behavior constraints. 
	 */
	
	public PatternSet StrategyPatternInvocationConstraintsChecking(UIPluginContext context, XFactory factory, PatternClass patternCandidate, XLog softwareLog, ClassTypeHierarchy cth, ArrayList<HashMap<String, Object>> result)
	{
		if(result==null)//if there is no complete candidates discovered, return null.
		{
			return null;
		}
		
		// intermediate results that are complete candidates
		PatternSet discoveredCandidateInstanceSet = new PatternSetImpl(); 
		
		for(int i = 0; i<result.size(); i++)//each result(i) is a candidate pattern instance
		{	
//			System.out.println(result.get(i));
			//get the strategy class type 
			HashSet<String> StrategyClassTypeHierarchy = new HashSet<>();
			for(ClassClass c:software.designpattern.behavioralchecking.BasicOperators.typeHierarchyClassSet(cth, (ClassClass)result.get(i).get("Strategy")))
			{
				StrategyClassTypeHierarchy.add(c.getPackageName()+"."+c.getClassName());
			}
			
			//get the Context class type 
			HashSet<String> ContextClassTypeHierarchy = new HashSet<>();
			for(ClassClass c: software.designpattern.behavioralchecking.BasicOperators.typeHierarchyClassSet(cth, (ClassClass)result.get(i).get("Context")))
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
				
				if(software.designpattern.behavioralchecking.BasicOperators.MethodcallNumberPerTrace(((MethodClass)result.get(i).get("contextInterface")), invocation)<1
						||software.designpattern.behavioralchecking.BasicOperators.MethodcallNumberPerTrace(((MethodClass)result.get(i).get("algorithmInterface")), invocation)<1)
				{
					continue;
				}
					
				//@invocation-constraint 1: the strategy change can only be done by strategy or context
				//the set of all setstrategy
				HashSet<XEvent> setStrategyEventSet = software.designpattern.behavioralchecking.BasicOperators.eventSetofMethodPerInvocation(invocation,
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
						int nextSetStrategyEventIndex=software.designpattern.behavioralchecking.BasicOperators.getNextEventAfterIndexX(invocation, 
								invocation.indexOf(setStrategyE), ((MethodClass)result.get(i).get("setStrategy")));
						if(nextSetStrategyEventIndex!=-1)//there exists a setStrategyEvent after the current one
						{
							//for each set Strategy event, we get the contextInterface after it. 
							XEvent firstContextInterfaceEvent =software.designpattern.behavioralchecking.BasicOperators.getFirstEventAfterIndexABeforeIndexB(invocation, invocation.indexOf(setStrategyE),
									nextSetStrategyEventIndex, ((MethodClass)result.get(i).get("contextInterface")));
							if(firstContextInterfaceEvent==null)//there do not exist such a contextInterface event
							{
								continue;
							}
							else 
							{
								//get the callee object set of the invoked handle method of request
								HashSet<String> calleeObjectofAlgorithmInterface =software.designpattern.behavioralchecking.BasicOperators.calleeObjectSetofInvokedEventsPerTraceV1(firstContextInterfaceEvent, invocation);
								//for each setState, we get its parameter mapping
								HashMap<ClassClass, String> paraMappingofsetStrategyEvent = software.designpattern.behavioralchecking.BasicOperators.constructParameterMapping(setStrategyE);
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
			
			PatternClass NewP = BasicOperators.CreatePatternInstanceCombination(patternCandidate, result.get(i), softwareLog.size(), numberofValidatedInvocation);
			discoveredCandidateInstanceSet.add(NewP);
		}
		return discoveredCandidateInstanceSet;
	}
	
}
