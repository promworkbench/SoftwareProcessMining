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
import software.designpattern.behavioralchecking.InvocationConstruction;
/*
 * this class defines the two main functions to (1) discover complete description of observer pattern; (2) check behavioral constraints. 
 */
import software.designpattern.dynamicdiscovery.CandidateCombination;
import software.designpattern.patterndefinition.PatternClass;
import software.designpattern.patterndefinition.PatternSet;
import software.designpattern.patterndefinition.PatternSetImpl;

public class ObserverPatternDiscoveryAndChecking {
	/* for observer pattern, the subject, observer and notify are included while register, un-register, update are missing.
	 * the DiscoveryCompleteObserverPattern aims to find all missing roles from the execution log. 
	 */
	public ArrayList<HashMap<String, Object>> DiscoverCompleteObserverPattern(UIPluginContext context, PatternClass patternCandidate, XLog softwareLog, ClassTypeHierarchy cth, HashMap<String, ArrayList<Object>> role2values)
	{
		//for those role with value, we need first make sure the values are also included in the log.
		//for Subject, we extend the class set to all classes with typehierarchy information
		if(!BasicOperators.classIncludedInLog((ClassClass)role2values.get("Subject").get(0), softwareLog))//the class is not included in the log
		{
			context.log("The value of Subject is not included in the execution log!", MessageLevel.WARNING);
			//return null; //if there exist missing value, then this pattern is not considered anymore. 
		}
		//get all classes that of typehierarchy with the Subject class, and also included in the log.
		HashSet<ClassClass> alternativeSubjectClassSet =BasicOperators.typeHierarchyClassSetInLog(cth, softwareLog, (ClassClass)role2values.get("Subject").get(0));

		if(alternativeSubjectClassSet.size()!=0){
			for(ClassClass cc: alternativeSubjectClassSet)
			{
				if(!role2values.get("Subject").contains(cc))
				{
					role2values.get("Subject").add(cc);
				}
			}
		}
		
		//for Observer, we extend the class set to all classes with typehierarchy information
		if(!BasicOperators.classIncludedInLog((ClassClass)role2values.get("Observer").get(0), softwareLog))//the class is not included in the log
		{
			context.log("The value of Observer is not included in the execution log!", MessageLevel.WARNING);
			//return null;
		}
		
		//get all classes that of typehierarchy with the Observer class, and also included in the log.
		HashSet<ClassClass> alternativeObserverClassSet =BasicOperators.typeHierarchyClassSetInLog(cth, softwareLog, (ClassClass)role2values.get("Observer").get(0));

		if(alternativeObserverClassSet.size()!=0){
			for(ClassClass cc: alternativeObserverClassSet)
			{
				if(!role2values.get("Observer").contains(cc))
				{
					role2values.get("Observer").add(cc);
				}
			}
		}

		//for notify
		if(!BasicOperators.methodIncludedInLog((MethodClass)role2values.get("notify").get(0), softwareLog))// the method is not included in the log
		{
			context.log("The value of notify is not included in the execution log!", MessageLevel.WARNING);
			//return null;
		}
		HashSet<MethodClass> alternativeMethodSet =BasicOperators.typeHierarchyMethodSetInLog(cth, softwareLog, (MethodClass)role2values.get("notify").get(0));
		if(alternativeMethodSet.size()!=0){
			for(MethodClass mm: alternativeMethodSet)
			{
				if(!role2values.get("notify").contains(mm))
				{
					role2values.get("notify").add(mm);
				}
			}
		}
		
		//for the update role, (1) it is a method of the observer; (2) it is invoked by the notify method
		HashSet<MethodClass> methodSetofObserver = new HashSet<MethodClass>();
		
		//get the method set of Observer role 
		for(Object c: role2values.get("Observer"))
		{
			methodSetofObserver.addAll(BasicOperators.MethodSetofClass((ClassClass)c, softwareLog));//get all type hierarchy classes
		}
		
		//get the method set invoked by notify.
		HashSet<MethodClass> methodSetInovkedByNotify = new HashSet<MethodClass>();
		for(Object m: role2values.get("notify"))
		{
			methodSetInovkedByNotify.addAll(BasicOperators.MethodSetofMethod((MethodClass)m, softwareLog));
		}
		
		for(MethodClass m: methodSetInovkedByNotify)
		{
			if(methodSetofObserver.contains(m)){
				if(!m.getMethodName().equals("init()"))//init() should not be included
				{
					if(!role2values.get("update").contains(m))
					{
						role2values.get("update").add(m);
					}
				}
			}
		}
				
		//for the register(or unregister) role, (1) it is a method of Subject; (2) it should include a parameter of Observer type. 
		HashSet<MethodClass> methodSetofSubject = new HashSet<MethodClass>();
		//get the method set of subject role 
		for(Object c: role2values.get("Subject"))
		{
			methodSetofSubject.addAll(BasicOperators.MethodSetofClass((ClassClass)c, softwareLog));//get all type hierarchy classes
		}
		
		//only select those with observer class as an input parameter type. 
		HashSet<ClassClass> observerClassTypeHierarchy = new HashSet<>();
		for(Object observerClass: role2values.get("Observer"))
		{
			observerClassTypeHierarchy.addAll(software.designpattern.behavioralchecking.BasicOperators.typeHierarchyClassSet(cth, (ClassClass)observerClass));
		}
		for(MethodClass m: methodSetofSubject)
		{
			//The parameter set of m
			for(ClassClass p: BasicOperators.ParameterSetofMethod(m, softwareLog))
			{
				if(observerClassTypeHierarchy.contains(p)){//if a method has a parameter class that is of observer class, it may be a register class
					if(!m.getMethodName().equals("init()"))//init() should not be included
					{
//						if(!role2values.get("update").contains(m))
//						{role2values.get("update").add(m);}
						if(!role2values.get("register").contains(m))
						{role2values.get("register").add(m);}
						if(!role2values.get("unregister").contains(m))
						{role2values.get("unregister").add(m);}
						break;
					}
				}
			}
		}

		
		//till now the observer pattern candidate should be complete, each role may have multiple values.  
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
		System.out.println(result);
//		for(int i = 0; i<result.size(); i++)
//		{	
//			PatternClass NewP = BasicOperators.CreatePatternInstance(patternCandidate, result.get(i), softwareLog.size(), 0);
//			discoveredCandidateInstanceSet.add(NewP);
//		}
		return result;
	}


	/*
	 * for each complete observer pattern candidate, we (1) first identify its invocation; and (2) check the behavior constraints. 
	 * refer to the software.designpattern.behavioralchecking.ObserverBehavioralChecking implementation
	 */
	public PatternSet ObserverPatternBehavioralConstraintsChecking(XFactory factory, PatternClass patternCandidate, XLog softwareLog, ClassTypeHierarchy cth, ArrayList<HashMap<String, Object>> result)
	{
		
		if(result==null)//if there is no complete candidates discovered, return null.
		{
			return null;
		}
		
		// intermediate results to store complete but not validated candidates
		PatternSet discoveredCandidateInstanceSet = new PatternSetImpl(); 
		
		for(int i = 0; i<result.size(); i++)//each result(i) is a candidate pattern instance
		{			
			//identify the invocations for each observer pattern instance,
			HashSet<XTrace> invocationTraces = InvocationConstruction.observerPatternInvocation(softwareLog, factory, cth, result.get(i));
			
			int numberofValidatedInvocation = 0;//the number of validated pattern invocations. 
			//for each invocation, we check the behavioral constraints.
			for(XTrace invocation: invocationTraces)
			{
				//check invocation level constraints, register>=1, unregister>=1, update>=1 and notify>=1;
				if(software.designpattern.behavioralchecking.BasicOperators.MethodcallNumberPerTrace(((MethodClass)result.get(i).get("notify")), invocation)<1
						||software.designpattern.behavioralchecking.BasicOperators.MethodcallNumberPerTrace(((MethodClass)result.get(i).get("update")), invocation)<1
						||software.designpattern.behavioralchecking.BasicOperators.MethodcallNumberPerTrace(((MethodClass)result.get(i).get("register")), invocation)<1
						//||BasicOperators.MethodcallNumberPerTrace(((MethodClass)result.get(i).get("unregister")), invocation)<1
						)
				{
					continue;
				}
					
				//@invocation-constraint 1: for each observer pattern instance, an observer object should be first registered to the subject object and then unregistered. 
				//it is not allowed that an observer is not registered but unregister.
				//get the observer 
				HashSet<ClassClass> ObserverClassTypeHierarchy = new HashSet<>();
				ObserverClassTypeHierarchy.addAll(software.designpattern.behavioralchecking.BasicOperators.typeHierarchyClassSet(cth, (ClassClass)result.get(i).get("Observer")));
				
				//get the observer object set
				HashSet<String> observerObjects = software.designpattern.behavioralchecking.BasicOperators.ObserverObjectSet(invocation,ObserverClassTypeHierarchy,
						((MethodClass)result.get(i).get("register")), 
						((MethodClass)result.get(i).get("unregister")));
				
				
				//@invocation-constraint 1: for each observer object, for each a register method with the observer object as input, there exist a unregister with observer object as input.
				int validedObserverObjectsNumber=0;
				for(String observerObj: observerObjects)
				{
					//the register event set
					HashSet<XEvent> registerEventSet=software.designpattern.behavioralchecking.BasicOperators.getMethodCallSetwithParaObj(invocation, observerObj, ((MethodClass)result.get(i).get("register")));
					
					if(registerEventSet.size()==0)// there exists some observer object that are not registered but unregistered
					{
						break;
					}
					else// for each observer object, there exist at least one register method that take this object as input. 
					{	//for each register event in the set, there should be a unregister event in the invocation satisfying: (1) it contains a parameter value of the current observer object, and (2) executed after register event 
						int flag =1;
						for(XEvent regEvent: registerEventSet)
						{
							if(!software.designpattern.behavioralchecking.BasicOperators.checkExistenceUnregister(regEvent, invocation, observerObj, ((MethodClass)result.get(i).get("unregister"))))
							{
								flag=0;//there exist a register without unregister
							}
						}
						if(flag ==1)
						validedObserverObjectsNumber++;
					}
				}
				
				if(validedObserverObjectsNumber!=observerObjects.size())//if all observer objects are validated.
				{
					continue;
				}
				
				
				//@invocation-constraint 2: each notify method call should invoke the update methods of currently registered observers
				//notify event set
				int validedNotifyNumber=0;
				HashSet<XEvent> notifyEventSet = software.designpattern.behavioralchecking.BasicOperators.eventSetofMethodPerInvocation(invocation,((MethodClass)result.get(i).get("notify")));
				if(notifyEventSet.size()==0)// no notify 
				{
					break;
				}
				else// for notify
				{
					for(XEvent notifyEvent: notifyEventSet)
					{
						//the callee object set of invoked update methods 
						HashSet<String> updateObjects =software.designpattern.behavioralchecking.BasicOperators.calleeObjectSetofInvokedEventsPerTrace(notifyEvent, invocation, ObserverClassTypeHierarchy);
						//the currently registered observer object set
						HashSet<String> registeredObjects=software.designpattern.behavioralchecking.BasicOperators.currentlyRegisteredObservers(notifyEvent, observerObjects, invocation, 
								((MethodClass)result.get(i).get("register")), 
								((MethodClass)result.get(i).get("unregister")));
						if(registeredObjects.equals(updateObjects))
						{
							validedNotifyNumber++;//all registered observer are updated, this is a validated notify. 
						}
					}
				}
				// only all notify methods are validated, this constraint is validated
				if(validedNotifyNumber!=notifyEventSet.size())//if all notify are validated.
				{
					continue;
				}
			
				
				numberofValidatedInvocation++;
			}
			if(numberofValidatedInvocation==0)//if there is no validated invocation, then the current candidate is not approved.
			{
				continue; //go to the next candidate
			}

			PatternClass NewP = BasicOperators.CreatePatternInstanceCombination(patternCandidate, result.get(i), softwareLog.size(), numberofValidatedInvocation);
			
			discoveredCandidateInstanceSet.add(NewP);
		
		}
		return discoveredCandidateInstanceSet;
	}
	
	
	
}
