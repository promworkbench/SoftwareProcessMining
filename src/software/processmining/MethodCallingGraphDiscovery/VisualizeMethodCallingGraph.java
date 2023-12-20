package software.processmining.MethodCallingGraphDiscovery;

import java.util.HashMap;

import javax.swing.JComponent;

import org.jgrapht.graph.DefaultEdge;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

import UtilityClasses.MethodClass;
import software.processmining.multiviewinteraction.MethodCallingGraph;


public class VisualizeMethodCallingGraph {
	@Plugin(name = "Visualize Method Calling Graph", 
	returnLabels = { "Dot visualization" }, 
	returnTypes = { JComponent.class }, 
	parameterLabels = { "Method Calling Graph" }, 
	userAccessible = true)
	@Visualizer
	@UITopiaVariant(affiliation = "TU/e", author = "Cong Liu", email = "c.liu.3@tue.nl;liucongchina@163.com")	
	@PluginVariant(variantLabel = "Visualize Software Architecture", 
			requiredParameterLabels = {0})// it needs one input parameter
	public JComponent visualize(PluginContext context, MethodCallingGraph mcg) 
	{
		final Dot dot = new Dot();
		dot.setDirection(GraphDirection.leftRight);
		dot.setOption("label", "Method Calling Graph");

		//prepare the nodes
		HashMap<String, DotNode> activityToNode = new HashMap<String, DotNode>();
		for (MethodClass activity : mcg.getMethodCallingGraph().vertexSet()) 
		{
			DotNode node = dot.addNode(activity.toString());
			activityToNode.put(activity.toString(), node);
			node.setOption("shape", "box");
		}
		
		//prepare the edges
		for (DefaultEdge edge :mcg.getMethodCallingGraph().edgeSet()) 
		{
			String from = mcg.getMethodCallingGraph().getEdgeSource(edge).toString();
			String to =  mcg.getMethodCallingGraph().getEdgeTarget(edge).toString();

			dot.addEdge(activityToNode.get(from), activityToNode.get(to), "");
		}
		
		
		return new DotPanel(dot);
	}
	
	
}
