package software.processmining.multiviewinteraction;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

/**
 * this visualizer aims to visualize the simple software architecture in terms of static binding. 
 * @author cliu3
 */
public class VisualizeSimpleArchitectureModel {
	@Plugin(name = "Visualize Software Architecture (static binding)", 
			returnLabels = { "Dot visualization" }, 
			returnTypes = { JComponent.class }, 
			parameterLabels = { "SoftwareArchitecture" }, 
			userAccessible = true)
			@Visualizer
			@UITopiaVariant(affiliation = "TU/e", author = "Cong Liu", email = "c.liu.3@tue.nl;liucongchina@163.com")	
			@PluginVariant(variantLabel = "Visualize Software Architecture", 
					requiredParameterLabels = {0})// it needs one input parameter
	public JComponent visualizeTop(PluginContext context, SoftwareArchitectureModelSimple simpleSoftwareArchitecture)
	{
		Dot dot = new Dot();
		dot.setDirection(GraphDirection.topDown);
		dot.setOption("label", "Software Architecture");
		dot.setOption("fontsize", "36");
				
		return new DotPanel(VisualizeArchitectureSimple.visualizeSimpleArchitectureView(simpleSoftwareArchitecture,dot));
	}

}
