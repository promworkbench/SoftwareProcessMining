package software.processmining.visualizer;

/*
 * this plugin first wrapping a petri net to a hierarchical petri net, then visualize a hierarchical one. 
 */
import javax.swing.JComponent;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

import software.processmining.componentbehaviordiscovery.HierarchicalPetriNet;
import software.processmining.componentbehaviordiscovery.VisualizeHPNandInteraction2Dot;

@Plugin(name = "Visualize Petri Net (Cong Liu)", 
returnLabels = { "Dot visualization" }, 	
returnTypes = { JComponent.class }, 	
parameterLabels = {"Visualize Petri Net"}, 	
userAccessible = true)
@Visualizer
public class VisualizePetriNet {
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Cong Liu", email = "c.liu.3@tue.nl")
	@PluginVariant(variantLabel = "petrinet", requiredParameterLabels = {0})
	
	public JComponent visualize(UIPluginContext context, Petrinet pn) 
	{	
		HierarchicalPetriNet hpn = new HierarchicalPetriNet();
		hpn.setPn(pn);
		
		Dot dot = new Dot();
		dot.setDirection(GraphDirection.topDown);
		
		VisualizeHPNandInteraction2Dot.visualizeHPN2Dot(hpn, pn.getLabel(), dot, null);
		return new DotPanel(dot);
	}
}
