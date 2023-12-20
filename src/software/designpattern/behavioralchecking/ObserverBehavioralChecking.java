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
import software.designpattern.dynamicdiscovery.BasicFunctions;
import software.designpattern.patterndefinition.PatternClass;
import software.designpattern.patterndefinition.PatternSet;
import software.designpattern.patterndefinition.PatternSetImpl;

public class ObserverBehavioralChecking {
	public PatternSet ObserverPatternBehavioralConstraintsChecking(XFactory factory, XLog softwareLog, ClassTypeHierarchy cth, ArrayList<HashMap<String, Object>> result)
	{		
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
				if(BasicOperators.MethodcallNumberPerTrace(((MethodClass)result.get(i).get("notify")), invocation)<1
						||BasicOperators.MethodcallNumberPerTrace(((MethodClass)result.get(i).get("update")), invocation)<1
						||BasicOperators.MethodcallNumberPerTrace(((MethodClass)result.get(i).get("register")), invocation)<1
						//||BasicOperators.MethodcallNumberPerTrace(((MethodClass)result.get(i).get("unregister")), invocation)<1
						)
				{
					continue;
				}
					
				//@invocation-constraint 1: for each observer pattern instance, an observer object should be first registered to the subject object and then unregistered. 
				//it is not allowed that an observer is not registered but unregister.
				//get the observer 
				HashSet<ClassClass> ObserverClassTypeHierarchy = new HashSet<>();
				ObserverClassTypeHierarchy.addAll(BasicOperators.typeHierarchyClassSet(cth, (ClassClass)result.get(i).get("Observer")));
				
				//get the observer object set
				HashSet<String> observerObjects = BasicOperators.ObserverObjectSet(invocation,ObserverClassTypeHierarchy,
						((MethodClass)result.get(i).get("register")), 
						((MethodClass)result.get(i).get("unregister")));
				
				
//				//@invocation-constraint 1: for each observer object, for each a register method with the observer object as input, there exist a unregister with observer object as input.
//				int validedObserverObjectsNumber=0;
//				for(String observerObj: observerObjects)
//				{
//					//the register event set
//					HashSet<XEvent> registerEventSet=BasicOperators.getMethodCallSetwithParaObj(invocation, observerObj, ((MethodClass)result.get(i).get("register")));
//					
//					if(registerEventSet.size()==0)// there exists some observer object that are not registered but unregistered
//					{
//						break;
//					}
//					else// for each observer object, there exist at least one register method that take this object as input. 
//					{	//for each register event in the set, there should be a unregister event in the invocation satisfying: (1) it contains a parameter value of the current observer object, and (2) executed after register event 
//						int flag =1;
//						for(XEvent regEvent: registerEventSet)
//						{
//							if(!BasicOperators.checkExistenceUnregister(regEvent, invocation, observerObj, ((MethodClass)result.get(i).get("unregister"))))
//							{
//								flag=0;//there exist a register without unregister
//							}
//						}
//						if(flag ==1)
//						validedObserverObjectsNumber++;
//					}
//				}
//				
//				if(validedObserverObjectsNumber!=observerObjects.size())//if all observer objects are validated.
//				{
//					continue;
//				}
				
				
				//@invocation-constraint 2: each notify method call should invoke the update methods of currently registered observers
				//notify event set
				int validedNotifyNumber=0;
				HashSet<XEvent> notifyEventSet = BasicOperators.eventSetofMethodPerInvocation(invocation,((MethodClass)result.get(i).get("notify")));
				if(notifyEventSet.size()==0)// no notify 
				{
					break;
				}
				else// for notify
				{
					for(XEvent notifyEvent: notifyEventSet)
					{
						//the callee object set of invoked update methods 
						HashSet<String> updateObjects =BasicOperators.calleeObjectSetofInvokedEventsPerTrace(notifyEvent, invocation, ObserverClassTypeHierarchy);
						//the currently registered observer object set
						HashSet<String> registeredObjects=BasicOperators.currentlyRegisteredObservers(notifyEvent, observerObjects, invocation, 
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
			
			PatternClass NewP = BasicFunctions.CreatePatternInstance("Observer Pattern", result.get(i), softwareLog.size(), numberofValidatedInvocation);
			
			discoveredCandidateInstanceSet.add(NewP);
		
		}
		return discoveredCandidateInstanceSet;
	}
}
