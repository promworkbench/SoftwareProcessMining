package software.processmining.interfacediscoveryevaluation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;

import UtilityClasses.ClassClass;
import UtilityClasses.ComponentConfig;
import openXESsoftwareextension.XSoftwareExtension;
import software.processmining.interfacediscovery.ConstructSoftwareEventLog;

/**
 * According to this interface discovery approach, the interface of each component is obtained by clustering methods of each component by its calling method. 
 * Note: the current implementation is a bit different from that in the 
 * Software Interface Behavior Discovery (Similarity) plugin-->software.processmining.interfacediscovery.InterfaceBehaviorDiscoveryPlugin
 * i.e., we do not merge the interface using similarity. 
 * 
 * @author cliu3
 *
 */

public class InterfaceDiscoveryUsingCallingMethod {
	public SoftwareDescription callingMethodBasedInterfaceDiscovery(XLog originalLog, ComponentConfig comconfig, XFactory factory)
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
			HashSet<XEvent> topLevelEvents = InterfaceDiscoveryNaive.getTopLevelEventPerComponent(ConstructSoftwareEventLog.generatingComponentEventLog(component, classSet, originalLog, factory), classSet);
		
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
			
			
			//cluster the top level events by calling method 
			HashMap<String, HashSet<XEvent>> callingMethod2Events= clusterTopLeveEventUsingCallingMethod(topLevelEvents);
			//for caller method, we create an interface
			for(String callingM: callingMethod2Events.keySet())
			{
				//create single interface description and add it the the interface set
				interfaceSet.add(InterfaceDiscoveryNaive.createInterfaceDescription(callingMethod2Events.get(callingM), component2ClassSet));
			}
		}		
		
		softwareDescription.setComponentSet(componentDescriptionSet);
		return softwareDescription;
	}
	
	//cluster the top level events according to the calling method 
	public static HashMap<String, HashSet<XEvent>> clusterTopLeveEventUsingCallingMethod(HashSet<XEvent> topLevelEvents)
	{
		HashMap<String, HashSet<XEvent>> callingMethod2Events = new HashMap<>();
		
		for(XEvent event: topLevelEvents)
		{
			String tempCallingMethod = XSoftwareExtension.instance().extractCallerpackage(event)+"."+XSoftwareExtension.instance().extractCallerclass(event)+"."+XSoftwareExtension.instance().extractCallermethod(event);
			if(callingMethod2Events.keySet().contains(tempCallingMethod))
			{
				callingMethod2Events.get(tempCallingMethod).add(event);
			}
			else{
				HashSet<XEvent> tempEventS = new HashSet<>();
				tempEventS.add(event);
				callingMethod2Events.put(tempCallingMethod, tempEventS);
			}
		}
		
		return callingMethod2Events;
	}
}
