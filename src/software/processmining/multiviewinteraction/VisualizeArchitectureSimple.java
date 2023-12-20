package software.processmining.multiviewinteraction;

import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotCluster;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

/**
 * this visualizer aims to visualize the software architecture in terms of static binding. 
 * @author cliu3
 */
public class VisualizeArchitectureSimple {
	@Plugin(name = "Visualize Software Architecture (static binding)", 
			returnLabels = { "Dot visualization" }, 
			returnTypes = { JComponent.class }, 
			parameterLabels = { "SoftwareArchitecture" }, 
			userAccessible = true)
			@Visualizer
			@UITopiaVariant(affiliation = "TU/e", author = "Cong Liu", email = "c.liu.3@tue.nl;liucongchina@163.com")	
			@PluginVariant(variantLabel = "Visualize Software Architecture", 
					requiredParameterLabels = {0})// it needs one input parameter
	public JComponent visualizeTop(PluginContext context, SoftwareArchitectureModel softwareArchitecture)
	{
		Dot dot = new Dot();
		dot.setDirection(GraphDirection.topDown);
		dot.setOption("label", "Software Architecture");
		dot.setOption("fontsize", "36");
		
		//construct the simple architecture model
		SoftwareArchitectureModelSimple simpleArchitectureModel = constructSimpleArchitectureModel(softwareArchitecture);
		
		return new DotPanel(visualizeSimpleArchitectureView(simpleArchitectureModel,dot));
	}

	public static Dot visualizeSimpleArchitectureView(SoftwareArchitectureModelSimple simpleArchitectureModel, Dot dot)
	{
		//get the invoked interfaces, for those interfaces, we create its provided ports. 
		HashSet<String> invokedInterfaces = new HashSet<>();
		for(String callingInterface: simpleArchitectureModel.getInterfaceInteractions().keySet())
		{
			invokedInterfaces.addAll(simpleArchitectureModel.getInterfaceInteractions().get(callingInterface));
		}
		
		//visualize the component model
		//for each component model 
		// mapping<interface, dotcluster>
		HashMap<String, DotNode> interface2InterfaceDotNode= new HashMap<String, DotNode>();
		HashMap<String, DotNode> interface2ProvidedInterfacePortNode = new HashMap<String, DotNode>();
		for(String component: simpleArchitectureModel.getComponent2Interfaces().keySet())
		{
			//create a cluster for each component.
			DotCluster componentCluster =dot.addCluster();
			componentCluster.setOption("label", component); // component name, as the label
			componentCluster.setOption("penwidth", "5.0"); // width of the component border
			componentCluster.setOption("fontsize", "36");
			componentCluster.setOption("color","black");
			
			//add interface to connector
			for(String inter: simpleArchitectureModel.getComponent2Interfaces().get(component))// handle each interface in the current component. 
			{
				//create a dot node for each interface in the component cluster. 
				DotNode InterfaceNode =componentCluster.addNode(inter);
				//InterfaceNode.setOption("label", inter);
				InterfaceNode.setOption("penwidth", "3.0");
				InterfaceNode.setOption("fontsize", "36");
				//InterfaceNode.setOption("style","dashed");
				InterfaceNode.setOption("style", "filled");
				interface2InterfaceDotNode.put(inter, InterfaceNode);
				
				if(invokedInterfaces.contains(inter))
				{
					//create a dotnode for each interface (for provided port) out of the component cluster.
					DotNode providedInterfaceNode =dot.addNode("");
					//providedInterfaceNode.setOption("fontsize", "18");
					providedInterfaceNode.setOption("shape", "circle");
					providedInterfaceNode.setOption("width", ".2");
					providedInterfaceNode.setOption("color", "blue"); //border color
					providedInterfaceNode.setOption("style", "filled");
					providedInterfaceNode.setOption("fillcolor", "blue"); 
					interface2ProvidedInterfacePortNode.put(inter, providedInterfaceNode);
					
					//create an edge from InterfaceNode to providedInterfaceNode
					DotEdge e = dot.addEdge(InterfaceNode, providedInterfaceNode);
					e.setOption("arrowhead", "none");
					e.setOption("color", "blue");// edge color
					e.setOption("penwidth", "3.0");
				}
				
			}
		}
		
		//visualize interation
		for(String callingInterface: simpleArchitectureModel.getInterfaceInteractions().keySet())
		{
			for(String calledInterface: simpleArchitectureModel.getInterfaceInteractions().get(callingInterface))
			{
				//create an edge from calling Interface to called interface
				//add an edge from the current t to the cluster
				DotEdge tempEdge = dot.addEdge(interface2ProvidedInterfacePortNode.get(calledInterface), interface2InterfaceDotNode.get(callingInterface));				
				// set arc from the nested transition to cluster
				tempEdge.setOption("color", "blue");// edge color
				tempEdge.setOption("penwidth", "3.0");
				tempEdge.setOption("fontcolor", "red");
				tempEdge.setOption("fontsize", "24");
				tempEdge.setOption("dir", "both"); //otherwise arrowtail option does not work. 
				tempEdge.setOption("arrowtail","tee");
				//tempEdge.setOption("arrowhead","none");
			}
		}
		
		return dot;
	}
	
	
	public static SoftwareArchitectureModelSimple constructSimpleArchitectureModel (SoftwareArchitectureModel softwareArchitecture)
	{
		SoftwareArchitectureModelSimple simpleArchitectureView = new SoftwareArchitectureModelSimple();
				
		//reduce the software architecture
		//(1) a set of component models: only keep the component name and its interface names. 
		HashMap<String, HashSet<String>> component2Interfaces = new HashMap<>();
		for(ComponentModel com: softwareArchitecture.getComponentModelSet().getComponentModelSet())
		{
			//get the interface set of the compoennt
			HashSet<String> interfaceSet = new HashSet<>();
			for(InterfaceID2HierarchicalPetriNet inter: com.getI2hpn())
			{
				interfaceSet.add(inter.getInter().getName());
			}
			component2Interfaces.put(com.getComponentName(), interfaceSet);
		}
		
		simpleArchitectureView.setComponent2Interfaces(component2Interfaces);
		
		//(2) a set of interface interactions: from an interface to a set of interfaces
		HashMap<String, HashSet<String>> interfaceInteractions = new HashMap<>();
		for(MethodInterface interactionMethod:softwareArchitecture.getMethod2connectorModel().keySet())
		{
			if(!interfaceInteractions.keySet().contains(interactionMethod.getInterName()))
			{
				//get the invoked interface set
				HashSet<String> invokedInterfaceSet = new HashSet<>();
				for(Transition t: softwareArchitecture.getMethod2connectorModel().get(interactionMethod).getTransitions())
				{
					if (!t.isInvisible()) // not invisible transition
					{
						invokedInterfaceSet.add(t.getLabel());
					} 
				}
				interfaceInteractions.put(interactionMethod.getInterName(), invokedInterfaceSet);
			}
			else{
				//get the invoked interface set
				HashSet<String> invokedInterfaceSet = new HashSet<>();
				for(Transition t: softwareArchitecture.getMethod2connectorModel().get(interactionMethod).getTransitions())
				{
					if (!t.isInvisible()) // not invisible transition
					{
						invokedInterfaceSet.add(t.getLabel());
					} 
				}
				interfaceInteractions.get(interactionMethod.getInterName()).addAll(invokedInterfaceSet);
			}
			
		}
		simpleArchitectureView.setInterfaceInteractions(interfaceInteractions);
		
		return simpleArchitectureView;
	}
}
