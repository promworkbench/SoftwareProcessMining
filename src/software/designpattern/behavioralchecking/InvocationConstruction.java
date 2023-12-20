package software.designpattern.behavioralchecking;

import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import UtilityClasses.ClassClass;
import UtilityClasses.ClassTypeHierarchy;
import UtilityClasses.MethodClass;
import UtilityFunctions.OrderingEventsNano;
import openXESsoftwareextension.XSoftwareExtension;

public class InvocationConstruction {
	/*
	 * invocation identification for observer Pattern
	 */
	public static HashSet<XTrace> observerPatternInvocation(XLog softwareLog, XFactory factory, ClassTypeHierarchy cth, HashMap<String, Object> resulti)
	{
		//identify the invocations for each observer pattern instance,
		HashSet<XTrace> invocationTraces = new HashSet<>();
		
		//the class set of observer role.
		HashSet<ClassClass> observerClassTypeHierarchy=BasicOperators.typeHierarchyClassSet(cth, (ClassClass)resulti.get("Observer"));
		HashSet<String> observerClassTypeSet = new HashSet<>();
		for(ClassClass c: observerClassTypeHierarchy)//construct the state class type set.
		{
			observerClassTypeSet.add(c.getPackageName()+"."+c.getClassName());
		}
		
		for(XTrace trace:softwareLog)
		{
			//get the subject object set for each trace.
			HashSet<String> SubjectObjects =BasicOperators.ObjectSetClassPerTrace((ClassClass)resulti.get("Subject"),trace);
			for(String SubjectO:SubjectObjects)//for each Subject object, we construct an invocation.
			{
			//if(!SubjectO.equals("0"))
			//	{
					XTrace invocation = factory.createTrace();
					for(XEvent event: trace)
					{
						//if the callee class object is a subject object, and the callee method is register, unregister, notify
						if(XSoftwareExtension.instance().extractClassObject(event).equals(SubjectO))
						{
							if(XConceptExtension.instance().extractName(event).equals(((MethodClass)resulti.get("register")).getMethodName())
									||XConceptExtension.instance().extractName(event).equals(((MethodClass)resulti.get("unregister")).getMethodName())
									||XConceptExtension.instance().extractName(event).equals(((MethodClass)resulti.get("notify")).getMethodName()))
							{
								invocation.add(event);
								//System.out.println("add event: "+XConceptExtension.instance().extractName(event));
							}
						}
						//else if the caller class object is the subject object, the callee method is update, the callee class is of observer class type
						else if(XSoftwareExtension.instance().extractCallerclassobject(event).equals(SubjectO))
						{
							if(XConceptExtension.instance().extractName(event).equals(((MethodClass)resulti.get("update")).getMethodName())
								&& observerClassTypeSet.contains(XSoftwareExtension.instance().extractPackage(event)+"."+XSoftwareExtension.instance().extractClass(event)))
							{
								invocation.add(event);
								//System.out.println("add event: "+XConceptExtension.instance().extractName(event));
							}
						}
					}
					//order the invocation trace by start time stamp. 
					
					if(invocation.size()!=0)
					invocationTraces.add(OrderingEventsNano.orderEventLogwithTimestamp(invocation, XSoftwareExtension.KEY_STARTTIMENANO));
			//	}
				
			}
		}
		
		return invocationTraces;
	}

	/*
	 * invocation identification for state Pattern
	 */
	public static HashSet<XTrace> statePatternInvocation(XLog softwareLog, XFactory factory, ClassTypeHierarchy cth, HashMap<String, Object> resulti)
	{
		//identify the invocations for each state pattern instance,
		HashSet<XTrace> invocationTraces = new HashSet<>();
		
		//the class set of state role.
		HashSet<ClassClass> stateClassTypeHierarchy=BasicOperators.typeHierarchyClassSet(cth, (ClassClass)resulti.get("State"));
		HashSet<String> stateClassTypeSet = new HashSet<>();
		for(ClassClass c: stateClassTypeHierarchy)//construct the state class type set.
		{
			stateClassTypeSet.add(c.getPackageName()+"."+c.getClassName());
		}

		for(XTrace trace:softwareLog)
		{
			//get the context object set for each trace.
			HashSet<String> ContextObjects =BasicOperators.ObjectSetClassPerTrace((ClassClass)resulti.get("Context"),trace);
			for(String ContextO:ContextObjects)//for each context object, we construct an invocation.
			{
				if(!ContextO.equals("0"))
				{
					XTrace invocation = factory.createTrace();
					for(XEvent event: trace)
					{
						//if the callee class object is a context object, and the callee method is setState or request
						if(XSoftwareExtension.instance().extractClassObject(event).equals(ContextO))
						{
							if(XConceptExtension.instance().extractName(event).equals(((MethodClass)resulti.get("request")).getMethodName())
									||XConceptExtension.instance().extractName(event).equals(((MethodClass)resulti.get("setState")).getMethodName()))
							{
								invocation.add(event);
//								System.out.println("add event: "+XConceptExtension.instance().extractName(event));
							}
						}
						//else if the caller class object is the context object, the callee method is handle, the callee class is of State class type
						else if(XSoftwareExtension.instance().extractCallerclassobject(event).equals(ContextO))
						{
							if(XConceptExtension.instance().extractName(event).equals(((MethodClass)resulti.get("handle")).getMethodName())
								&& stateClassTypeSet.contains(XSoftwareExtension.instance().extractPackage(event)+"."+XSoftwareExtension.instance().extractClass(event)))
							{
								invocation.add(event);
//								System.out.println("add event: "+XConceptExtension.instance().extractName(event));
							}
						}
					}
					
					if(invocation.size()!=0)
					{
						invocationTraces.add(OrderingEventsNano.orderEventLogwithTimestamp(invocation, XSoftwareExtension.KEY_STARTTIMENANO));
						
					}
				}
				
			}
		}
		
		return invocationTraces;
	}
	
	/*
	 * invocation identification for strategy Pattern
	 */
	public static HashSet<XTrace> strategyPatternInvocation(XLog softwareLog, XFactory factory, ClassTypeHierarchy cth, HashMap<String, Object> resulti)
	{
		//identify the invocations for each strategy pattern instance,
		HashSet<XTrace> invocationTraces = new HashSet<>();
		
		//the class set of strategy role.
		HashSet<ClassClass> stratetyClassTypeHierarchy=BasicOperators.typeHierarchyClassSet(cth, (ClassClass)resulti.get("Strategy"));
		HashSet<String> strategylassTypeSet = new HashSet<>();
		for(ClassClass c: stratetyClassTypeHierarchy)//construct the state class type set.
		{
			strategylassTypeSet.add(c.getPackageName()+"."+c.getClassName());
		}
		
		for(XTrace trace:softwareLog)
		{
			//get the context object set for each trace.
			HashSet<String> ContextObjects =BasicOperators.ObjectSetClassPerTrace((ClassClass)resulti.get("Context"),trace);
			for(String ContextO:ContextObjects)//for each context object, we construct an invocation.
			{
				if(!ContextO.equals("0"))
				{
					XTrace invocation = factory.createTrace();
					for(XEvent event: trace)
					{
						//if the callee class object is a context object, and the callee method is contextInterface
						if(XSoftwareExtension.instance().extractClassObject(event).equals(ContextO))
						{
							if(XConceptExtension.instance().extractName(event).equals(((MethodClass)resulti.get("contextInterface")).getMethodName())
									||XConceptExtension.instance().extractName(event).equals(((MethodClass)resulti.get("setStrategy")).getMethodName()))
							{
								invocation.add(event);
//								System.out.println("add event: "+XConceptExtension.instance().extractName(event));
							}
						}
						//else if the caller class object is the context object, the callee method is handle, the callee class is of State class type
						else if(XSoftwareExtension.instance().extractCallerclassobject(event).equals(ContextO))
						{
							if(XConceptExtension.instance().extractName(event).equals(((MethodClass)resulti.get("algorithmInterface")).getMethodName())
								&& strategylassTypeSet.contains(XSoftwareExtension.instance().extractPackage(event)+"."+XSoftwareExtension.instance().extractClass(event)))
							{
								invocation.add(event);
//								System.out.println("add event: "+XConceptExtension.instance().extractName(event));
							}
						}
					}

					if(invocation.size()!=0)
					invocationTraces.add(OrderingEventsNano.orderEventLogwithTimestamp(invocation, XSoftwareExtension.KEY_STARTTIMENANO));
				}
				
			}
		}
		
		return invocationTraces;
	}

	
	/*
	 * invocation identification for singleton pattern
	 */
	
	public static HashSet<XTrace> singletonPatternInvocation(XLog softwareLog, XFactory factory, ClassTypeHierarchy cth, HashMap<String, Object> resulti)
	{
		//identify the invocations for each singleton pattern instance,
		HashSet<XTrace> invocationTraces = new HashSet<>();
		
		for(XTrace trace: softwareLog)
		{
			invocationTraces.add(trace);
			
		}
		return invocationTraces;
	}
}
