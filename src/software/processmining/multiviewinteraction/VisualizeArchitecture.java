package software.processmining.multiviewinteraction;

import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotCluster;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

import software.processmining.componentbehaviordiscovery.VisualizeHPNandInteraction2Dot;

/**
 * this visualizer aims to visualize the software architecture with detaisl
 * (1) interface behavior
 * (2) connector behavior
 * (3) interface invocation cardinality.
 * @author cliu3
 */

public class VisualizeArchitecture {
	@Plugin(name = "Visualize Software Architecture", 
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
		
		//global variable: mapping<interface, dotcluster>
		HashMap<InterfaceID, DotCluster> interface2InterfaceCluster= new HashMap<InterfaceID, DotCluster>();
		
		//(1) for interface cardinality
		HashMap<InterfaceID, Integer> interface2Cardinality=softwareArchitecture.getInterfaceCardinality();
		
		//(2) for connector model. 
		//store all interaction interactionMethod to pn cluster
		HashMap<String, DotCluster> interactionMethod2InteractionCluster = new HashMap<String, DotCluster>(); 
		HashSet<DotNode> tDotInterfaceNodeSet = new HashSet<>();// it records all interface(transition) node in the connector model. 
		for(MethodInterface interactionMethod:softwareArchitecture.getMethod2connectorModel().keySet())
		{
			//create a cluster for each connector.
			DotCluster interactionCluster =dot.addCluster();
			//interactionCluster.setOption("label", xevent.toString());
			//interactionCluster.setOption("label", "Interaction");
			interactionCluster.setOption("fontsize", "24");
			interactionCluster.setOption("style", "filled");
			//interactionCluster.setOption("shape", "ellipse");
			interactionCluster.setOption("fillcolor", "lightblue");
			interactionCluster.setOption("color", "white");
			VisualizeHPNandInteraction2Dot.visualizePN2Dot(softwareArchitecture.getMethod2connectorModel().get(interactionMethod), interactionCluster, tDotInterfaceNodeSet);
			
			interactionMethod2InteractionCluster.put(interactionMethod.getMethod().toString(), interactionCluster);
		}
		
		
		//(3) for each component model 
		ComponentModelSet componentModelSet = softwareArchitecture.getComponentModelSet();
		for(ComponentModel componentModel: componentModelSet.getComponentModelSet())
		{
			//create a cluster for each component.
			DotCluster componentCluster =dot.addCluster();
			componentCluster.setOption("label", componentModel.getComponentName()); // component name, as the label
			componentCluster.setOption("penwidth", "5.0"); // width of the component border
			componentCluster.setOption("fontsize", "24");
			componentCluster.setOption("color","black");
			
			for(InterfaceID2HierarchicalPetriNet interface2HPN: componentModel.getI2hpn())// handle each interface in the current component. 
			{
				//create a cluster for each interface 
				DotCluster InterfaceCluster =componentCluster.addCluster();
				InterfaceCluster.setOption("label", interface2HPN.getInter().getName());
				InterfaceCluster.setOption("penwidth", "3.0");
				InterfaceCluster.setOption("style","dashed");
				VisualizeHPNandInteraction2Dot.visualizeHPN2Dot(interface2HPN.getHpn(), 
						interface2HPN.getInter().getName(), InterfaceCluster, interactionMethod2InteractionCluster);
				interface2InterfaceCluster.put(interface2HPN.getInter(), InterfaceCluster);
			}
		}
		//(4) add interface arc and cardinality
		for(DotNode tNode:tDotInterfaceNodeSet)
		{
			for(InterfaceID interID:interface2InterfaceCluster.keySet())
			{
				//add an arc from interface transition to interface hpn
				if (tNode.getLabel().equals(interID.getName()))
				{
					//add an edge from the current t to the cluster
					DotEdge tempEdge = dot.addEdge(tNode, interface2InterfaceCluster.get(interID));				
					// set arc from the nested transition to cluster
					tempEdge.setOption("lhead", interface2InterfaceCluster.get(interID).getId());
					tempEdge.setOption("color", "red");// edge color
					tempEdge.setOption("penwidth", "3.0");
					tempEdge.setOption("fontcolor", "red");
					tempEdge.setOption("fontsize", "24");
					if(interface2Cardinality.get(interID)>1){
						tempEdge.setOption("label", "*");// label
					}
					else {
						tempEdge.setOption("label", "1");// label
					}					
				}
			}
		}
				
		
		return new DotPanel(dot);
	}
}
