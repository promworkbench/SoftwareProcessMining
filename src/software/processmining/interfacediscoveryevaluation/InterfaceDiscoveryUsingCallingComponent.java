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
 * According to this interface discovery approach, the interface of each component is obtained by clustering methods of each component by its calling component. 
 * 
 * @author cliu3
 *
 */
public class InterfaceDiscoveryUsingCallingComponent {
	
	public SoftwareDescription callingComponentBasedInterfaceDiscovery(XLog originalLog, ComponentConfig comconfig, XFactory factory)
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
			
			
			//cluster the top level events by calling component 
			HashMap<String, HashSet<XEvent>> component2Events= clusterTopLeveEventUsingCallingComponent(topLevelEvents, component2ClassSet);
			//for each class, we create an interface
			for(String com: component2Events.keySet())
			{
				//create single interface description and add it the the interface set
				interfaceSet.add(InterfaceDiscoveryNaive.createInterfaceDescription(component2Events.get(com), component2ClassSet));
			}
		}		
		
		softwareDescription.setComponentSet(componentDescriptionSet);
		return softwareDescription;
		
	}
	
	//cluster the top level events according to the calling component 
	public static HashMap<String, HashSet<XEvent>> clusterTopLeveEventUsingCallingComponent(HashSet<XEvent> topLevelEvents, HashMap<String, HashSet<String>> component2ClassSet)
	{
		HashMap<String, HashSet<XEvent>> component2Events = new HashMap<>();
		
		for(XEvent event: topLevelEvents)
		{
			//get the calling component of the event
			String tempComponent = getCallingComponent(component2ClassSet,event);
			if(component2Events.keySet().contains(tempComponent))
			{
				component2Events.get(tempComponent).add(event);
			}
			else{
				HashSet<XEvent> tempEventS = new HashSet<>();
				tempEventS.add(event);
				component2Events.put(tempComponent, tempEventS);
			}
		}
		
		return component2Events;
	}
	
	public static String getCallingComponent(HashMap<String, HashSet<String>> component2ClassSet, XEvent event)
	{
		String callingComponent ="NULLMain";
		for(String com: component2ClassSet.keySet())
		{
			if(component2ClassSet.get(com).contains(XSoftwareExtension.instance().extractCallerpackage(event)+"."+XSoftwareExtension.instance().extractCallerclass(event)))
			{
				callingComponent=com;
				break;
			}
		}
		return callingComponent;
	}
}
