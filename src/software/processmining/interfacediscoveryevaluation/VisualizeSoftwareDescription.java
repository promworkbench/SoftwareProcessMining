package software.processmining.interfacediscoveryevaluation;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotCluster;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

import UtilityClasses.MethodClass;

@Plugin(name = "Visualize Software Description", 
returnLabels = { "Dot visualization" }, 	
returnTypes = { JComponent.class }, 	
parameterLabels = {"Visualize Software Description"}, 	
userAccessible = true)
@Visualizer
public class VisualizeSoftwareDescription {
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Cong Liu", email = "c.liu.3@tue.nl")
	@PluginVariant(variantLabel = "cog", requiredParameterLabels = {0})
	
	public JComponent visualize(UIPluginContext context, SoftwareDescription sd) 
	{	
		Dot dot = convert(sd);
		return new DotPanel(dot);
	}
	
	public static Dot convert(SoftwareDescription cc)
	{
		Dot dot = new Dot();
		dot.setDirection(GraphDirection.leftRight);
		//dot.setDirection(GraphDirection.topDown);
		
		//for each component, we create a cluster, 
		for(ComponentDescription component: cc.getComponentSet())
		{
			DotCluster comCluster=dot.addCluster();
			comCluster.setLabel(component.getComponentName());
			comCluster.setOption("penwidth", "3");
			//for each interface
			for(InterfaceDescription interf: component.getInterfaceSet())
			{
				DotCluster interCluster = comCluster.addCluster();
				interCluster.setOption("penwidth", "2");
				interCluster.setOption("color", "blue");
				interCluster.setOption("style","dashed");
				
				for(MethodClass m: interf.getMethodSet())
				{
					DotNode methodNode = interCluster.addNode(m.toString());
					methodNode.setOption("shape", "box");
					methodNode.setOption("penwidth", "1");
				}
			}			
		}
		
		return dot;
	}
}
