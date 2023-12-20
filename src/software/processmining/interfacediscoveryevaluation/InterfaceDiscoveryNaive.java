package software.processmining.interfacediscoveryevaluation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import UtilityClasses.ClassClass;
import UtilityClasses.ComponentConfig;
import UtilityClasses.MethodClass;
import openXESsoftwareextension.XSoftwareExtension;
import software.processmining.interfacediscovery.ConstructSoftwareEventLog;

/**
 * According to this interface discovery approach, each component has only one interface. 
 * This interface contains all methods that are used by other components. 
 * 
 * @author cliu3
 *
 */
public class InterfaceDiscoveryNaive{

	/*
	 * single interface per component. 
	 */
	public SoftwareDescription naiveDiscoverySingleInterface(XLog originalLog, ComponentConfig comconfig, XFactory factory)
	{
		SoftwareDescription softwareDescription = new SoftwareDescription();
		
		HashSet<ComponentDescription> componentDescriptionSet = new HashSet<>();
		
		//transform the format of component configuration file
		HashMap<String, HashSet<String>> component2ClassSet = new HashMap<>();
		for(String com: comconfig.getAllComponents())
		{
			HashSet<String> classSet = new HashSet<>();//class set of the current component
			for(ClassClass c: comconfig.getClasses(com))
			{
				classSet.add(c.getPackageName()+"."+c.getClassName());// both the package and class names are used to describe each class
			}
			component2ClassSet.put(com, classSet);
		}
		
		for(String component: comconfig.getAllComponents())
		{
			// the class set of the current component
			Set<String> classSet =component2ClassSet.get(component);
			
			//get the event log of each component, and then get the top level event of each component
			HashSet<XEvent> topLevelEvents = getTopLevelEventPerComponent(ConstructSoftwareEventLog.generatingComponentEventLog(component, classSet, originalLog, factory), classSet);
		
			if(topLevelEvents.size()==0)//we do not create component description for the current component. 
			{
				continue;
			}
			
			//create a component description
			ComponentDescription componentDescription = new ComponentDescription();
			componentDescription.setComponentName(component);
			HashSet<InterfaceDescription> interfaceSet = new HashSet<>();
			componentDescription.setInterfaceSet(interfaceSet);
			
			//add to the component description set
			componentDescriptionSet.add(componentDescription);
			
			//create single interface description and add it the the interface set
			interfaceSet.add(createInterfaceDescription(topLevelEvents, component2ClassSet));
		}

		softwareDescription.setComponentSet(componentDescriptionSet);
		return softwareDescription;
	}
	
	
	/*
	 * create an interface for each method
	 */
	
	public SoftwareDescription naiveDiscoverySingleMethodPerInterface(XLog originalLog, ComponentConfig comconfig, XFactory factory)
	{
		SoftwareDescription softwareDescription = new SoftwareDescription();

		HashSet<ComponentDescription> componentDescriptionSet = new HashSet<>();
		
		//transform the format of component configuration file
		HashMap<String, HashSet<String>> component2ClassSet = new HashMap<>();
		for(String com: comconfig.getAllComponents())
		{
			HashSet<String> classSet = new HashSet<>();//class set of the current component
			for(ClassClass c: comconfig.getClasses(com))
			{
				classSet.add(c.getPackageName()+"."+c.getClassName());// both the package and class names are used to describe each class
			}
			component2ClassSet.put(com, classSet);
		}
		
		for(String component: comconfig.getAllComponents())
		{
			// the class set of the current component
			Set<String> classSet =component2ClassSet.get(component);
						
			//get the event log of each component, and then get the top level event of each component			
			HashSet<XEvent> topLevelEvents = getTopLevelEventPerComponent(ConstructSoftwareEventLog.generatingComponentEventLog(component, classSet, originalLog, factory), classSet);

			if(topLevelEvents.size()==0)//we do not create component description for the current component. 
			{
				continue;
			}
			
			//create a component description
			ComponentDescription componentDescription = new ComponentDescription();
			componentDescription.setComponentName(component);
			HashSet<InterfaceDescription> interfaceSet = new HashSet<>();
			componentDescription.setInterfaceSet(interfaceSet);
			
			//add to the component description set
			componentDescriptionSet.add(componentDescription);
			
			//cluster the top level events by class
			HashMap<String, HashSet<XEvent>> class2Events= clusterTopLeveEventUsingMethod(topLevelEvents);
			//for each class, we create an interface
			for(String c: class2Events.keySet())
			{
				//create single interface description and add it the the interface set
				interfaceSet.add(InterfaceDiscoveryNaive.createInterfaceDescription(class2Events.get(c), component2ClassSet));
			}
		
		}		
		
		softwareDescription.setComponentSet(componentDescriptionSet);
		return softwareDescription;
		
	}
	
	//this method tries to get the top level events of each component, i.e., the methods whose caller do not belong to the current component. 
	public static HashSet<XEvent> getTopLevelEventPerComponent(XLog componentLog, Set<String> classSet)
	{
		HashSet<XEvent> topLevelEvents = new HashSet<>();
		
		if(componentLog.size()>0)//the current component should contains trace. 
		{
			for(XTrace trace: componentLog)
			{
				for(XEvent event: trace)
				{
					if(!classSet.contains(XSoftwareExtension.instance().extractCallerpackage(event)+"."+XSoftwareExtension.instance().extractCallerclass(event)))
					{
						topLevelEvents.add(event);
					}
				}
			}
		}
				
		return topLevelEvents;
	}
	
	//create interface description for single interface, 
	/**
	 * 
	 * @param topLevelEvents, top level events of this interface
	 * @param component2ClassSet
	 * @return
	 */
	public static InterfaceDescription createInterfaceDescription(HashSet<XEvent> topLevelEvents, HashMap<String, HashSet<String>> component2ClassSet)
	{
		//create interface and add to the interface set 
		InterfaceDescription interfaceDescription = new InterfaceDescription();
		
		//construct the method to its caller method set
		HashMap<MethodClass, HashSet<MethodClass>> tempMethod2callerMethodSet = new HashMap<>();
		for(XEvent event: topLevelEvents)
		{
			MethodClass method = new MethodClass();
			method.setMethodName(XConceptExtension.instance().extractName(event));
			method.setClassName(XSoftwareExtension.instance().extractClass(event));
			method.setPackageName(XSoftwareExtension.instance().extractPackage(event));
			
			MethodClass callerMethod = new MethodClass();
			callerMethod.setMethodName(XSoftwareExtension.instance().extractCallermethod(event));
			callerMethod.setClassName(XSoftwareExtension.instance().extractCallerclass(event));
			callerMethod.setPackageName(XSoftwareExtension.instance().extractCallerpackage(event));
			
			if(tempMethod2callerMethodSet.keySet().contains(method)){
				tempMethod2callerMethodSet.get(method).add(callerMethod);
			}
			else{
				HashSet<MethodClass> callerMethodSet = new HashSet<>();
				callerMethodSet.add(callerMethod);
				tempMethod2callerMethodSet.put(method, callerMethodSet);
			}
		}
		
		HashSet<Method2CallerMethods> method2CallerMethodSet = new HashSet<>();
		HashSet<String> callerComponentSet = new HashSet<>();
		for(MethodClass m: tempMethod2callerMethodSet.keySet())
		{
			Method2CallerMethods m2m = new Method2CallerMethods();
			m2m.setMethod(m);
			m2m.setCallerMethodSet(tempMethod2callerMethodSet.get(m));
			method2CallerMethodSet.add(m2m);
		}
		
		interfaceDescription.setMethod2CallerMethodSet(method2CallerMethodSet);;//set the method2callermethodset 
		interfaceDescription.generateCallerMethodSet();//generate the caller method set 
		interfaceDescription.generateMethodSet();//generate the method set 
		
		for(MethodClass cm: interfaceDescription.getCallerMethodSet())
		{
			for(String com: component2ClassSet.keySet())
			{
				if(component2ClassSet.get(com).contains(cm.getPackageName()+"."+cm.getClassName()))
				{
					callerComponentSet.add(com);
					break;
				}
			}
		}
		interfaceDescription.setCallerComponentSet(callerComponentSet);//set the caller component set.
		
		return interfaceDescription;
	}
	
	
	//cluster the top level events according to the method, i.e., each method refers to a set of events
	public static HashMap<String, HashSet<XEvent>> clusterTopLeveEventUsingMethod(HashSet<XEvent> topLevelEvents)
	{
		HashMap<String, HashSet<XEvent>> method2Events = new HashMap<>();
		
		for(XEvent event: topLevelEvents)
		{
			String tempMethod = XSoftwareExtension.instance().extractPackage(event)+"."+XSoftwareExtension.instance().extractClass(event)+"."+XConceptExtension.instance().extractName(event);
			if(method2Events.keySet().contains(tempMethod))
			{
				method2Events.get(tempMethod).add(event);
			}
			else{
				HashSet<XEvent> tempEventS = new HashSet<>();
				tempEventS.add(event);
				method2Events.put(tempMethod, tempEventS);
			}
		}
		
		return method2Events;
	}
}
