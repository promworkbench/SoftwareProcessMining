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
 * According to this interface discovery approach, the interface of each component is obtained by clustering methods of each component by its belonging class. 
 * 
 * @author cliu3
 *
 */

public class InterfaceDiscoveryUsingBelongingClass {

	public SoftwareDescription belongingClassBasedInterfaceDiscovery(XLog originalLog, ComponentConfig comconfig, XFactory factory)
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
			
			//cluster the top level events by class
			HashMap<String, HashSet<XEvent>> class2Events= clusterTopLeveEventUsingBelongingClass(topLevelEvents);
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
	
	//cluster the top level events according to the belonging classes, i.e., each class refers to a set of events
	public static HashMap<String, HashSet<XEvent>> clusterTopLeveEventUsingBelongingClass(HashSet<XEvent> topLevelEvents)
	{
		HashMap<String, HashSet<XEvent>> class2Events = new HashMap<>();
		
		for(XEvent event: topLevelEvents)
		{
			String tempClass = XSoftwareExtension.instance().extractPackage(event)+"."+XSoftwareExtension.instance().extractClass(event);
			if(class2Events.keySet().contains(tempClass))
			{
				class2Events.get(tempClass).add(event);
			}
			else{
				HashSet<XEvent> tempEventS = new HashSet<>();
				tempEventS.add(event);
				class2Events.put(tempClass, tempEventS);
			}
		}
		
		return class2Events;
	}
}
