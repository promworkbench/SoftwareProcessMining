package crossorganization.processmining;

import java.util.HashSet;
import java.util.Set;

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

public class VisualizeCrossOrganizationBusinessProcessModel {
	@Plugin(name = "Visualize Cross-organization Business Process Model", 
	returnLabels = { "Dot visualization" }, 
	returnTypes = { JComponent.class }, 
	parameterLabels = { "Cross-organization Model" }, 
	userAccessible = true)
	@Visualizer
	@UITopiaVariant(affiliation = "TU/e", author = "Cong Liu", email = "c.liu.3@tue.nl OR liucongchina@163.com")	
	@PluginVariant(variantLabel = "Visualize Cross-organization Business Process Model", 
		requiredParameterLabels = {0})// it needs one input parameter
	
	public JComponent visualizeCrossorganizationModel(PluginContext context, CrossOrganizationBusinessProcessModel crossOrgModel)
	{	
		Dot dot = new Dot();
		dot.setDirection(GraphDirection.topDown);
		dot.setOption("label", "Cross-organization Business Process Model");
		dot.setOption("fontsize", "24");		
		
		// it records all activity (transition) nodes in the intra-organization model. 
		HashSet<DotNode> tDotNodeSet = new HashSet<>();
		
		// for each organization, we create a cluster and then visualize its pn 
		Set<String> orgSet = crossOrgModel.getAllOrganizations();
		
		for(String org:orgSet)
		{
			//create a cluster for each organization.
			DotCluster orgCluster =dot.addCluster();
			orgCluster.setOption("label", org); // organization name, as the label
			orgCluster.setOption("penwidth", "2.0"); // width of the organization border
			orgCluster.setOption("fontsize", "18");
			orgCluster.setOption("color","black");  
			orgCluster.setOption("style","dashed");
			
			//visualize the pn
			VisualizeHPNandInteraction2Dot.visualizePN2Dot(crossOrgModel.getOrganizationModel(org), orgCluster, tDotNodeSet);	
		}
		
		//for each interaction, we add a place to connect the corresponding transitions.
		//the source or target of an interaction may involve mutliple activities, attributed from choice behavior.
		HashSet<CrossOrganizationInteraction> interactions = crossOrgModel.getAllInteractions();
		for(CrossOrganizationInteraction inte: interactions)
		{			
			//add a circle 
			//MessageDotPlace messageDot= new MessageDotPlace(inte.toString());//the label of message place is the combination of source activity and target activity
			MessageDotPlace messageDot= new MessageDotPlace("");//the label of message place is the combination of source activity and target activity

			dot.addNode(messageDot);
			
			//find the source to message connections
			for(DotNode tDot: tDotNodeSet)
			{
				for(OrgActivity sourceA: inte.getSourceActivities())
				{
					if(tDot.getLabel().equals(sourceA.getActivity()))
					{
						//add an arc from tDot to messageDot
						DotEdge edge = dot.addEdge(tDot, messageDot);
						edge.setOption("style", "dashed");
						edge.setOption("color", "blue");// edge color
						edge.setOption("penwidth", "1.5");
					}

				}		
				
				//find the message to target connections
				for(OrgActivity targetA: inte.getTargetActivities())
				{
					if(tDot.getLabel().equals(targetA.getActivity()))
					{
						//add an arc from messageDot to tDot
						DotEdge edge = dot.addEdge(messageDot, tDot);
						edge.setOption("style", "dashed");
						edge.setOption("color", "blue");// edge color
						edge.setOption("penwidth", "1.5");
					}
				}
			}
			
		}
		
		return new DotPanel(dot);
	}
	
	
	//inner class for message/interaction place dot
	private static class MessageDotPlace extends DotNode {
			public MessageDotPlace(String messageInfor) {
				super(messageInfor, null);
				setOption("shape", "doublecircle");
				//setOption("shape", "circle");
				setOption("style", "dashed");
				setOption("color", "blue");
				setOption("penwidth", "1.5");
				//setOption("fillcolor", "grey");
			}
		
	}// inner class for place dot
	
}
