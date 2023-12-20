package software.processmining.multiviewinteraction;

import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotCluster;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

import software.processmining.componentbehaviordiscovery.VisualizeHPNandInteraction2Dot;

/**
 * this visualizer aims to visualize the software architecture with only connector beahvior. 
 * @author cliu3
 */
public class VisualizeArchitectureMiddle {
	@Plugin(name = "Visualize Software Architecture (no interface behavior and cardinality)", 
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
		
		//construct the medium architecture
		SoftwareArchitectureModelMedium mediumArchitectureModel = constructMediumArchitectureModel(softwareArchitecture);
		
		return new DotPanel(visualizeMediumArchitectureModel(mediumArchitectureModel,dot));
	}
	
	public static Dot visualizeMediumArchitectureModel(SoftwareArchitectureModelMedium mediumArchitectureModel, Dot dot)
	{
		//visualize the interface to connector
		//store all interface to pn cluster
		HashMap<MethodInterface, DotCluster> interface2InteractionCluster = new HashMap<MethodInterface, DotCluster>(); 
		// it records all interface (transition) node in the connector model. 
		HashSet<DotNode> tDotInterfaceNodeSet = new HashSet<>();
		for(MethodInterface interactionMethod:mediumArchitectureModel.getConnectorModels().keySet())
		{
			//create a cluster for each connector.
			DotCluster interactionCluster =dot.addCluster();
			interactionCluster.setOption("fontsize", "18");
			interactionCluster.setOption("style", "filled");
			//interactionCluster.setOption("shape", "ellipse");
			interactionCluster.setOption("fillcolor", "lightblue");
			interactionCluster.setOption("color", "white");
			VisualizeHPNandInteraction2Dot.visualizePN2Dot(mediumArchitectureModel.getConnectorModels().get(interactionMethod), interactionCluster, tDotInterfaceNodeSet);
			interface2InteractionCluster.put(interactionMethod, interactionCluster);
		}
		
		//visualize the component model
		//for each component model 
		// mapping<interface, dotcluster>
		HashMap<String, DotNode> interface2InterfaceDotNode= new HashMap<String, DotNode>();
		for(String component: mediumArchitectureModel.getComponent2Interfaces().keySet())
		{
			//create a cluster for each component.
			DotCluster componentCluster =dot.addCluster();
			componentCluster.setOption("label", component); // component name, as the label
			componentCluster.setOption("penwidth", "5.0"); // width of the component border
			componentCluster.setOption("fontsize", "36");
			componentCluster.setOption("color","black");
			
			//add interface to connector
			for(String inter: mediumArchitectureModel.getComponent2Interfaces().get(component))// handle each interface in the current component. 
			{
				//create a cluster for each interface 
				DotNode InterfaceNode =componentCluster.addNode(inter);
				//InterfaceNode.setOption("label", inter);
				InterfaceNode.setOption("penwidth", "3.0");
				InterfaceNode.setOption("fontsize", "36");
				//InterfaceNode.setOption("style","dashed");
				InterfaceNode.setOption("style", "filled");
				
				// we connect the interface to its connector cluster
				for(MethodInterface methodI: interface2InteractionCluster.keySet())
				{
					if(methodI.getInterName().equals(inter))
					{
						//add an edge from the current t to the cluster
						LocalDotEdge tempEdge = new LocalDotEdge(InterfaceNode, interface2InteractionCluster.get(methodI), 1);
						dot.addEdge(tempEdge);
					
						// set arc from the interface node to cluster
						tempEdge.setOption("lhead", interface2InteractionCluster.get(methodI).getId());
						tempEdge.setOption("color", "red");// edge color
						tempEdge.setOption("penwidth", "3.0");
						tempEdge.setOption("style","dashed");// arrow style is dashed
						tempEdge.setOption("fontsize", "24");
						tempEdge.setOption("fontcolor", "red");
						// set the t as double line and add bgcolor
						//tDot.setOption("fillcolor", "lightblue");
						//tDot.setOption("peripheries","2");//double line
					}
				}	
				interface2InterfaceDotNode.put(inter, InterfaceNode);
			}
		}
		// add interface arc from connector transition to interface dotnode
		for(DotNode tNode:tDotInterfaceNodeSet)
		{
			for(String interID:interface2InterfaceDotNode.keySet())
			{
				//add an arc from interface transition to interface hpn
				if (tNode.getLabel().equals(interID))
				{
					//add an edge from the current t to the cluster
					DotEdge tempEdge = dot.addEdge(tNode, interface2InterfaceDotNode.get(interID));				
					// set arc from the nested transition to cluster
					tempEdge.setOption("color", "red");// edge color
					tempEdge.setOption("penwidth", "3.0");
					tempEdge.setOption("fontcolor", "red");
					tempEdge.setOption("fontsize", "24");
									
				}
			}
		}
		
		return dot;
	}
	
	public static SoftwareArchitectureModelMedium constructMediumArchitectureModel (SoftwareArchitectureModel softwareArchitecture)
	{
		SoftwareArchitectureModelMedium mediumArchitectureView = new SoftwareArchitectureModelMedium();
				
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
		
		mediumArchitectureView.setComponent2Interfaces(component2Interfaces);
		
		//(2) a set of connector models: from interface to pn
		HashMap<MethodInterface, Petrinet> connectorModels = new HashMap<>();
		for(MethodInterface interactionMethod:softwareArchitecture.getMethod2connectorModel().keySet())
		{
			connectorModels.put(interactionMethod, softwareArchitecture.getMethod2connectorModel().get(interactionMethod));
		}
		mediumArchitectureView.setConnectorModels(connectorModels);
		
		return mediumArchitectureView;
		
	}
	// inner class for edge dot, we have two types of edges, 
	//the normal one with arrows =1, and the double arrows with arrows =2.
	private static class LocalDotEdge extends DotEdge
	{
		public LocalDotEdge(DotNode source, DotNode target, int arrows)
		{
			super(source, target);
			if (arrows==1)
			{	
				setOption("arrowhead", "vee");
			}
			else
			{
				//startEdge =dot.addEdge(startTransitionDot,startPlaceDot);
				//setOption("penwidth","2");
				//startEdge.setOption("color", "black:black");
				//setOption("label", " N");
				setOption("arrowhead", "vee");//or to use normalnormal twice
				//startEdge.setOption("shape", "vee");
			}
		}
	}
}
