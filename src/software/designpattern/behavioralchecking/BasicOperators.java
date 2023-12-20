package software.designpattern.behavioralchecking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

import UtilityClasses.ClassClass;
import UtilityClasses.ClassTypeHierarchy;
import UtilityClasses.MethodClass;
import UtilityFunctions.ExtractMethodClassPackage;
import openXESsoftwareextension.XSoftwareExtension;
import software.designpattern.patterndefinition.ConstructRole2Values;
import software.designpattern.patterndefinition.PatternClass;

public class BasicOperators {
	/**
	 * get the typehierarchyClass
	 * @param cth
	 * @param c: the class that is not included in the log
	 * @return a set of classes
	 */
	public static HashSet<ClassClass> typeHierarchyClassSet(ClassTypeHierarchy cth, ClassClass c)
	{			
		HashSet<ClassClass> classSet = new HashSet<>();
		
		for(HashSet<ClassClass> cc: cth.getAllCTH())
		{
			if(cc.contains(c))
			{
				classSet.addAll(cc);// if we find the group of inherited classes, then stop.
				return classSet;
			}
		}
		
		classSet.add(c);// otherwise, return the only class. 
		return classSet;
	}
	
	/**
	 * Given a trace and class, get the object set of the class
	 * Note that 0 is not considered. 
	 * @param c
	 * @param trace
	 * @return
	 */
	public static HashSet<String> ObjectSetClassPerTrace(ClassClass c, XTrace trace)
	{
		HashSet<String> objectSet = new HashSet<>();
		for(XEvent event: trace)
		{
			if(c.toString().equals(XSoftwareExtension.instance().extractPackage(event)+"."+XSoftwareExtension.instance().extractClass(event)))
			{
				objectSet.add(XSoftwareExtension.instance().extractClassObject(event));
			}
			
		}
		objectSet.remove("0");// remove the object id if it is 0/ 
		return objectSet;
	}
	
	/*
	 * Given a callee method name and a trace, return the number of times this callee method occures in the trace.
	 */
	public static int MethodcallNumberPerTrace(MethodClass methodName, XTrace trace)
	{
		int count =0;
		for(XEvent event: trace)
		{
//			if(methodName.toString().equals(XSoftwareExtension.instance().extractPackage(event)+"."+
//					XSoftwareExtension.instance().extractClass(event)+"."+XConceptExtension.instance().extractName(event)))
			if(methodName.getMethodName().equals(XConceptExtension.instance().extractName(event)))
			{
				count++;
			}
		}
		return count;
	}
	
	/*
	 * Given an invocation trace, a class typehierarchy of observer, register method and unregister method, we get a set of parameter object of this class typehierarchy
	 * 
	 */
	public static HashSet<String> ObserverObjectSet(XTrace invocation, HashSet<ClassClass> ObserverClassTypeHierarchy,
			MethodClass registerMethod, MethodClass unregisterMethod)
	{
		HashSet<String> observerObjSet = new HashSet<>();
		for(XEvent event: invocation)
		{
			String methodName = XSoftwareExtension.instance().extractPackage(event)+"."+XSoftwareExtension.instance().extractClass(event)
					+"."+XConceptExtension.instance().extractName(event);
			if(registerMethod.toString().equals(methodName)||
					unregisterMethod.toString().equals(methodName))
			{
				//parse the parameter class set (as a sequence), and get the value. 
				HashMap<ClassClass,String> mapping = constructParameterMapping(event);
				for(ClassClass cc: mapping.keySet())
				{
					if(ObserverClassTypeHierarchy.contains(cc))
					{
						observerObjSet.add(mapping.get(cc));
					}
				}
			}
		}
		
		return observerObjSet;
	}
	
	/*
	 * construct the mapping from parameter type (class) to object value (string)
	 */
	public static HashMap<ClassClass,String> constructParameterMapping(XEvent event)
	{
		
		HashMap<ClassClass,String> parameterType2Value = new HashMap<>();
		
		ArrayList<String> currentParameterTypeList = new ArrayList<String>();
		ArrayList<String> currentParameterValueList = new ArrayList<String>();
		
		String tempParaType = XSoftwareExtension.instance().extractParameterTypeSet(event);
		String tempParaValue = XSoftwareExtension.instance().extractParameterValueSet(event);

		if(tempParaType.contains(","))// more than one parameters for the current method
		{
			for(String paraT: tempParaType.split("\\,"))
			{
				currentParameterTypeList.add(paraT);
			}
			if(tempParaValue!=null)
			{
				for(String paraV: tempParaValue.split("\\,"))
				{
					currentParameterValueList.add(paraV);
				}
			}
			else
			{
				for(int i =0;i<currentParameterTypeList.size();i++)
				{
					currentParameterValueList.add(null);
				}
			}
			
		}
		else
		{
			if (tempParaType.contains("."))// only one parameter for the current method
			{
				currentParameterTypeList.add(tempParaType);
				currentParameterValueList.add(tempParaValue);
			}
		}
		
		if(currentParameterTypeList.size()==0)//no parameter for the current method
		{
			return parameterType2Value;
		}
		else //one or more parameter for the current method
		{
			for(int i=0;i<currentParameterTypeList.size();i++)
			{
				ClassClass tempClass = new ClassClass();
				
				tempClass.setClassName(ExtractMethodClassPackage.getLast(currentParameterTypeList.get(i)));
				tempClass.setPackageName(ExtractMethodClassPackage.getFirstfrom2Parts(currentParameterTypeList.get(i)));
				
				parameterType2Value.put(tempClass, currentParameterValueList.get(i));
			}
			return parameterType2Value;
		}
	}
	
	/*
	 * Given a trace/invocation, a parameter object, method, return a set of events 
	 */
	public static HashSet<XEvent> getMethodCallSetwithParaObj(XTrace invocation, String paraObj, MethodClass method)
	{
		HashSet<XEvent> eventSet = new HashSet<>();	
		for(XEvent event: invocation)
		{
			if(constructParameterMapping(event).values().contains(paraObj) && 
					method.toString().equals(XSoftwareExtension.instance().extractPackage(event)+"."+XSoftwareExtension.instance().extractClass(event)
					+"."+XConceptExtension.instance().extractName(event)))
			{
				eventSet.add(event);
			}
		}
		
		return eventSet;
	}
	
	/*
	 * check if these exist a unregister method call that has the same observer object parameter of register method call. 
	 */
	public static boolean checkExistenceUnregister(XEvent regEvent, XTrace invocation, String observerObj, MethodClass unregisterMethodName)
	{
		for(int n= invocation.indexOf(regEvent)+1;n<invocation.size();n++)
		{
			if(constructParameterMapping(invocation.get(n)).values().contains(observerObj)
					&& 
					unregisterMethodName.toString().equals(XSoftwareExtension.instance().extractPackage(invocation.get(n))+"."+
					XSoftwareExtension.instance().extractClass(invocation.get(n))
							+"."+XConceptExtension.instance().extractName(invocation.get(n))))
			{
				return true;
			}
		}
		return false;
		
	}
	
	/*
	 * Event set of a certain method
	 */	
	public static HashSet<XEvent> eventSetofMethodPerInvocation(XTrace invocation, MethodClass method)
	{
		HashSet<XEvent> eventS = new HashSet<>();
		for(XEvent event: invocation)
		{
//			if(method.toString().equals(XSoftwareExtension.instance().extractPackage(event)+"."+XSoftwareExtension.instance().extractClass(event)
//					+"."+XConceptExtension.instance().extractName(event)))
			if(method.getMethodName().equals(XConceptExtension.instance().extractName(event)))
			{
				eventS.add(event);
			}
		}
		
		return eventS;
	}
	
	/*
	 * Given an event and an invocation, return the callee object of the invoked events
	 */
	public static HashSet<String> calleeObjectSetofInvokedEventsPerTrace(XEvent callerEvent, XTrace invocation, HashSet<ClassClass> ObserverClassTypeHierarchy)
	{
		
		//get the invoked event set
		HashSet<XEvent> invokedSet = new HashSet<>();
		for(XEvent event: invocation)
		{
			if(XSoftwareExtension.instance().extractCallermethod(event).equals(XConceptExtension.instance().extractName(callerEvent))
					&&XSoftwareExtension.instance().extractCallerclassobject(event).equals(XSoftwareExtension.instance().extractClassObject(callerEvent))
					&&Long.parseLong(XSoftwareExtension.instance().extractStarttimenano(callerEvent)) < Long.parseLong(XSoftwareExtension.instance().extractStarttimenano(event))
					&&Long.parseLong(XSoftwareExtension.instance().extractEndtimenano(callerEvent)) > Long.parseLong(XSoftwareExtension.instance().extractEndtimenano(event)))
			{
				invokedSet.add(event);
			}
		}
		//get the callee object of the invoked event set, and the class should belong to the ObserverClassTypeHierarchy
		HashSet<String> objectSet = new HashSet<>();
		HashSet<String> classSet = new HashSet<>();
		for(ClassClass c: ObserverClassTypeHierarchy)
		{
			classSet.add(c.toString());
		}
		
		for(XEvent e: invokedSet)
		{
			if(classSet.contains(XSoftwareExtension.instance().extractPackage(e)+"."+XSoftwareExtension.instance().extractClass(e)))
			objectSet.add(XSoftwareExtension.instance().extractClassObject(e));
		}
		
		objectSet.remove("0");// no 0 included. 
		return objectSet;
	}
	
	/*
	 * Given an event and an invocation, return the callee object of the invoked events
	 */
	public static HashSet<String> calleeObjectSetofInvokedEventsPerTraceV1(XEvent callerEvent, XTrace invocation)
	{
		
		//get the invoked event set
		HashSet<XEvent> invokedSet = new HashSet<>();
		for(XEvent event: invocation)
		{
			if(XSoftwareExtension.instance().extractCallermethod(event).equals(XConceptExtension.instance().extractName(callerEvent))
					&&XSoftwareExtension.instance().extractCallerclassobject(event).equals(XSoftwareExtension.instance().extractClassObject(callerEvent))
					&&Long.parseLong(XSoftwareExtension.instance().extractStarttimenano(callerEvent)) < Long.parseLong(XSoftwareExtension.instance().extractStarttimenano(event))
					&&Long.parseLong(XSoftwareExtension.instance().extractEndtimenano(callerEvent)) > Long.parseLong(XSoftwareExtension.instance().extractEndtimenano(event)))
			{
				invokedSet.add(event);
			}
		}
		//get the callee object of the invoked event set, and the class should belong to the ObserverClassTypeHierarchy
		HashSet<String> objectSet = new HashSet<>();
		
		for(XEvent e: invokedSet)
		{
			objectSet.add(XSoftwareExtension.instance().extractClassObject(e));
		}
		
		objectSet.remove("0");// no 0 included. 
		return objectSet;
	}
	
	/*
	 * get the currently registered observer objects
	 */
	public static HashSet<String> currentlyRegisteredObservers(XEvent notifyEvent, HashSet<String> allObserverObjects, XTrace invocation, MethodClass registerMethod, MethodClass unregisterMethod)
	{
		HashSet<String> currentObservers = new HashSet<>();
		for(int i =0;i<invocation.indexOf(notifyEvent);i++)
		{
			String methodName = XSoftwareExtension.instance().extractPackage(invocation.get(i))+"."+XSoftwareExtension.instance().extractClass(invocation.get(i))
					+"."+XConceptExtension.instance().extractName(invocation.get(i));
			if(registerMethod.toString().equals(methodName))// registered objects
			{
				for(String obj: constructParameterMapping(invocation.get(i)).values())
				{
					if(allObserverObjects.contains(obj))
						currentObservers.add(obj);
				}
			}
			else if(unregisterMethod.toString().equals(methodName))//unregistered objects
			{
				if(currentObservers.size()!=0)// if the current observers are empty, there is no need to remove 
				{
					for(String obj: constructParameterMapping(invocation.get(i)).values())
					{
						if(currentObservers.contains(obj))
							currentObservers.remove(obj);
					}
				}
				
			}
		}
		
		return currentObservers;
		
	}
	
	/*
	 * construct role 2 values mapping, for different design patterns 
	 */
	
	public static HashMap<String, ArrayList<Object>> Role2Values (PatternClass op)
	{
		//construct the mapping for observer pattern
		if(op.getPatternName().equals("Observer Pattern"))
		{
			return ConstructRole2Values.observerPattern(op);
		}
		else if(op.getPatternName().equals("State Pattern"))
		{
			return ConstructRole2Values.statePattern(op);
			
		}
		else if(op.getPatternName().equals("Strategy Pattern"))
		{
			return ConstructRole2Values.strategyPattern(op);
		}
		else if(op.getPatternName().equals("(Object)Adapter Pattern"))
		{
			return ConstructRole2Values.AdapterPattern(op);
		}
		else if(op.getPatternName().equals("Factory Method Pattern"))
		{
			return ConstructRole2Values.FactoryMethodPattern(op);
		}
		else if(op.getPatternName().equals("Command Pattern"))
		{
			return ConstructRole2Values.commandPattern(op);
		}
		else if(op.getPatternName().equals("Visitor Pattern"))
		{
			return ConstructRole2Values.visitorPattern(op);
		}
		else if(op.getPatternName().equals("Singleton Pattern"))
		{
			return ConstructRole2Values.SingletonPattern(op);
		}
		return null;
	}
	
	public static HashSet<XEvent> setStateMethodCallSet(XTrace invocation, MethodClass request, MethodClass setStateMethod)
	{
		HashSet<XEvent> setStateEvents = new HashSet<>();
		
		XEvent firstRequestEvent=null;
		//get the first request event
		for(int i =0; i<invocation.size();i++)
		{
//			if(request.toString().equals(XSoftwareExtension.instance().extractPackage(invocation.get(i))+"."+
//					XSoftwareExtension.instance().extractClass(invocation.get(i))+"."+
//					XConceptExtension.instance().extractName(invocation.get(i))))
			if(request.getMethodName().equals(XConceptExtension.instance().extractName(invocation.get(i))))
			{
				firstRequestEvent = invocation.get(i);
				break;
			}
		}
		
		if(firstRequestEvent!=null)//get all setState events after the first request event. 
		{
			for(int i= invocation.indexOf(firstRequestEvent)+1; i<invocation.size();i++)
			{
//				if(setStateMethod.toString().equals(XSoftwareExtension.instance().extractPackage(invocation.get(i))+"."+
//						XSoftwareExtension.instance().extractClass(invocation.get(i))+"."+
//						XConceptExtension.instance().extractName(invocation.get(i))))
				if(setStateMethod.getMethodName().equals(XConceptExtension.instance().extractName(invocation.get(i))))
				{
					setStateEvents.add(invocation.get(i));
				}
			}
		}
		
		return setStateEvents;
		
	}
	
	public static int getNextEventAfterIndexX(XTrace invocation, int X, MethodClass method)
	{
		for(int i=X+1;i<invocation.size();i++)
		{
//			if(method.toString().equals(XSoftwareExtension.instance().extractPackage(invocation.get(i))+"."+
//						XSoftwareExtension.instance().extractClass(invocation.get(i))+"."+
//						XConceptExtension.instance().extractName(invocation.get(i))))
			if(method.getMethodName().equals(XConceptExtension.instance().extractName(invocation.get(i))))
			{
				return i;
			}
		}
		return -1;
	}
	
	public static XEvent getFirstEventAfterIndexABeforeIndexB(XTrace invocation, int A, int B, MethodClass method)
	{
		
		for(int i =A+1;i<B;i++)
		{
//			if(method.toString().equals(XSoftwareExtension.instance().extractPackage(invocation.get(i))+"."+
//					XSoftwareExtension.instance().extractClass(invocation.get(i))+"."+
//					XConceptExtension.instance().extractName(invocation.get(i))))
			if(method.getMethodName().equals(XConceptExtension.instance().extractName(invocation.get(i))))
			{
				return invocation.get(i);
			}
		}
		return null;
	}

	/*
	 * Event set of a certain method
	 */
	
	public static HashSet<XEvent> eventSetofMethodPerInvocation(XTrace invocation, String methodName)
	{
		HashSet<XEvent> eventS = new HashSet<>();
		for(XEvent e: invocation)
		{
			if(XConceptExtension.instance().extractName(e).equals(methodName))
			{
				eventS.add(e);
			}
		}
		return eventS;
	}
	
	
}
