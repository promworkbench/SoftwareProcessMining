package software.processmining.multiviewinteraction;

import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import UtilityClasses.MethodClass;
import software.processmining.interfacediscoveryevaluation.ComponentDescription;
import software.processmining.interfacediscoveryevaluation.InterfaceDescription;
import software.processmining.interfacediscoveryevaluation.SoftwareDescription;

/**
 * this plugin aims to discover a simple architectural model by taking:
 * (1) software execution data
 * (2) Software description: it contains which methods are included in which interface
 * The architectural model contains: 
	//(1) a set of component models: only keep the component name and its interface names. 
	//(2) a set of interface interactions: from an interface to a set of interfaces
 * @author cliu3
 *
 */
@Plugin(
		name = "Simple Software Architecture Discovery Plugin",// plugin name
		
		returnLabels = {"Software Architecture"}, //return labels
		returnTypes = {SoftwareArchitectureModelSimple.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Event Log", "Software Desciption"},
		
		userAccessible = true,
		help = "This plugin aims to discover a simple architectural model of a piece of software." 
		)
public class SimpleSoftwareArchitectureDiscoveryPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl;liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Discovering simple software architectural model, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0, 1}
			)
	public SoftwareArchitectureModelSimple architectureDiscovery(UIPluginContext context, XLog originalLog, SoftwareDescription softwareDescription)
	{
		//get the log name from the original log. it is shown as the title of returned results. 
		context.getFutureResult(0).setLabel("Simple Software Architecture Discovery");
		
		SoftwareArchitectureModelSimple simpleArchitectureView = new SoftwareArchitectureModelSimple();
		
		//(1) a set of component models: only keep the component name and its interface names. 
		HashMap<String, HashSet<String>> component2Interfaces = new HashMap<>();
		simpleArchitectureView.setComponent2Interfaces(component2Interfaces);
		
		//(2) a set of interface interactions: from an interface to a set of interfaces
		//E.g., interface1--><interface2, interface3> means interface2, interface3 is required by interface1
		HashMap<String, HashSet<String>> interfaceInteractions = new HashMap<>();
		simpleArchitectureView.setInterfaceInteractions(interfaceInteractions);
		
		//all top-level <methods and interface name>-->component name, a small portion of them are interaction methods
		HashMap<MethodInterface, String> topLevelMethod2Component = new HashMap<MethodInterface, String>();
		
		//InterfaceID set of the whole software
		HashSet<InterfaceID> InterfaceIDSet = new HashSet<>();

		//(1) get all interfaces and components. 
		for(ComponentDescription componentDescription: softwareDescription.getComponentSet())
		{
			int num =0;
			//get the interface set of the compoennt
			HashSet<String> interfaceSet = new HashSet<>();
			for(InterfaceDescription interfaceDescription: componentDescription.getInterfaceSet())
			{
				InterfaceID interID= new InterfaceID();
				interID.setInterfaceDescription(interfaceDescription);
				interID.setName(componentDescription.getComponentName()+(num++));
				
				InterfaceIDSet.add(interID);
				
				//set the name the current interface
				interfaceSet.add(interID.getName());
				
				//each top-level method refers to its belonging interface name, its belonging component name.  
				for(MethodClass m: interfaceDescription.getMethodSet())
				{
					MethodInterface mi = new MethodInterface();
					mi.setMethod(m);
					mi.setInterName(interID.getName());
					topLevelMethod2Component.put(mi, componentDescription.getComponentName());
				}
				
			}
			//add the interface set to the component name
			component2Interfaces.put(componentDescription.getComponentName(), interfaceSet);
		}
		
		//(2) construct the interface interactions
		//create the methodcalling graph
		MethodCallingGraph mcg = MethodCallConnectivity.methodCallGraph(originalLog);
		context.getProvidedObjectManager().createProvidedObject("Method calling graph", mcg, MethodCallingGraph.class, context);

		//for each top-level method, we first check if it is a interaction method
		for(MethodInterface interactionMethod: topLevelMethod2Component.keySet())
		{
			HashSet<InterfaceID> invokedInterfaceSet = InteractionModelDiscovery.invokedInterfaceSet(
					topLevelMethod2Component.get(interactionMethod), interactionMethod.getMethod(), mcg, InterfaceIDSet);
			
			//return the invoked interface set of interactionMethod
			if(invokedInterfaceSet.size()>0)
			{
				HashSet<String>	invokedInter = new HashSet<>(); 
				
				for(InterfaceID id: invokedInterfaceSet)
				{
					invokedInter.add(id.getName());
				}
				
				if(!interfaceInteractions.keySet().contains(interactionMethod.getInterName()))
				{
					interfaceInteractions.put(interactionMethod.getInterName(), invokedInter);
				}
				else{
					interfaceInteractions.get(interactionMethod.getInterName()).addAll(invokedInter);
				}
				
			}	
		}
		
		return simpleArchitectureView;
	}
}
