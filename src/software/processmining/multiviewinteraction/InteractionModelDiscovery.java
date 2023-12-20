package software.processmining.multiviewinteraction;

import java.util.HashSet;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import UtilityClasses.MethodClass;
import openXESsoftwareextension.XSoftwareExtension;

public class InteractionModelDiscovery {

	/** 
	 * @param callerComponent: component of interactionMethod
	 * @param interactionMethod
	 * @param mcg: method calling graph, it is used to check the connectivity. 
	 * @param InterfaceIDSet: all interfaceIDSet
	 * @return a set of interfaces that are invoked by the interactionMethod: 
	 * (1) there is a directed path from the interactionMethod to interface; and 
	 * (2) the caller component of this interface is same with the component of interactionMethod
	 */
	public static HashSet<InterfaceID> invokedInterfaceSet(String Component, MethodClass interactionMethod, MethodCallingGraph mcg,  HashSet<InterfaceID> InterfaceIDSet)
	{
		HashSet<InterfaceID> interfaceIDSet = new HashSet<>();
//		HashMap<Integer, InterfaceID> count2Interface = new HashMap<>();
		int number;
		for(InterfaceID inter: InterfaceIDSet)
		{
			//get the method set of this interface, we only check those interfaces that are called by the component of the interaction method.
			if(inter.getInterfaceDescription().getCallerComponentSet().contains(Component))
			{
				//get the number of method per interface
				number=0;
				for(MethodClass m: inter.getInterfaceDescription().getMethodSet())
				{
					if(MethodCallConnectivity.connectivityChecking(mcg, interactionMethod, m)==true)
					{
						number++;//the number of invoked methods. 
					}
				}
//				if(number == inter.getInterfaceDescription().getMethodSet().size())//all methods of the interface are invoked by the interaction methods, we add the interface directly as a invoked interface. 
//				{
//					interfaceIDSet.add(inter);
//				} 
//				else if (number>0 && number<inter.getInterfaceDescription().getMethodSet().size())
//				{
//					count2Interface.put(number, inter);
//				}
				if(number!=0)
				{
					interfaceIDSet.add(inter);
				}
			}
		}
		
//		if(interfaceIDSet.size()>0)
//		{
//			return interfaceIDSet;
//		}
//		
//		if(count2Interface.size()>0)
//		{
//			//get the interface with the maximal value, 
//			interfaceIDSet.add(count2Interface.get(Collections.max(count2Interface.keySet())));
//		}
		
		return interfaceIDSet;
	}
	
	/*
	 * create the interaction log for each interaction method
	 */
	public static XLog createInteractionLog(XLog originalLog, HashSet<InterfaceID> interfaceIDSet, XFactory factory, MethodClass interactionMethod)
	{
		//initialize the interaction log. 
		XLog interactionLog =InitializeInteractionLog.initializeInteraction(factory, interactionMethod.toString());
		for(XTrace originalTrace: originalLog)
		{
			XTrace interactionTrace = factory.createTrace();
			for(InterfaceID inter: interfaceIDSet)
			{
				//create the start and end events for the current interface, make sure the time stamp
				interactionTrace.addAll(createStartCompleteInterfaceEventse(originalTrace,inter,factory));
			}
			interactionLog.add(interactionTrace);
		}
		
		return interactionLog;
	}
	
	/*
	 * this method create the start and complete event for an interface based on its method set, and input trace. 
	 */
	public static HashSet<XEvent> createStartCompleteInterfaceEventse(XTrace trace, InterfaceID inter, XFactory factory)
	{
		//method set of the current interface
		HashSet<MethodClass> methodSet = inter.getInterfaceDescription().getMethodSet();
		HashSet<String> methodSetString= new HashSet<>();
		for(MethodClass m: methodSet)
		{
			methodSetString.add(m.toString());
		}
		
		//caller method set of interface
		HashSet<MethodClass> callerMethodSet = inter.getInterfaceDescription().getCallerMethodSet();
		HashSet<String> callerMethodSetString = new HashSet<>();
		for(MethodClass m: callerMethodSet)
		{
			callerMethodSetString.add(m.toString());
		}
		
		//get the event set of the interface
		HashSet<XEvent> eventSet = new HashSet<>();
		for(XEvent e: trace)
		{
			//make sure the events belongs to the current interface (1) method name and (2) caller method name
			if(methodSetString.contains(XSoftwareExtension.instance().extractPackage(e)+"."+
					XSoftwareExtension.instance().extractClass(e)+"."+XConceptExtension.instance().extractName(e))
					&& callerMethodSetString.contains(XSoftwareExtension.instance().extractCallerpackage(e)+"."+
					XSoftwareExtension.instance().extractCallerclass(e)+"."+XSoftwareExtension.instance().extractCallermethod(e)))
			{
				eventSet.add(e);
			}
		}
		
		//create two events for each interface
		XEvent startEvent = factory.createEvent();
		XEvent endEvent = factory.createEvent();
		
		//set the name of event
		XConceptExtension.instance().assignName(startEvent, inter.getName());
		XConceptExtension.instance().assignName(endEvent, inter.getName());
		
		//set the lifecycle to each event
		XLifecycleExtension.instance().assignTransition(startEvent, "start");
		XLifecycleExtension.instance().assignTransition(endEvent, "complete");
		
		//add the starttime in nano
		long min =Long.MAX_VALUE;
		long max =Long.MIN_VALUE;
		
		for(XEvent e: eventSet)
		{
			if(Long.parseLong(XSoftwareExtension.instance().extractStarttimenano(e))<min)
			{
				min=Long.parseLong(XSoftwareExtension.instance().extractStarttimenano(e));
			}
			if(Long.parseLong(XSoftwareExtension.instance().extractStarttimenano(e))>max)
			{
				max=Long.parseLong(XSoftwareExtension.instance().extractStarttimenano(e));
			}
		}
		XSoftwareExtension.instance().assignStarttimenano(startEvent, Long.toString(min));
		XSoftwareExtension.instance().assignStarttimenano(endEvent, Long.toString(max));
		
		HashSet<XEvent> interactionInterfaceEvents = new HashSet<>();
		interactionInterfaceEvents.add(startEvent);
		interactionInterfaceEvents.add(endEvent);
		
		return interactionInterfaceEvents;
	}
	
}
