package crossorganization.processmining;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;
import org.processmining.plugins.declareminer.visualizing.DeclareMinerOutput;

/**
 * this class aims to discover the cross-organization interaction relation among activities
 * @author cliu3
 *
 */
public class CrossOrganizationInteractionRelationDiscovery {

	//construct activity precedence graph
	public static ActivityPrecedenceGraph ActivityPrecedencyGraphConstruction(DeclareMinerOutput output)
	{
		// we first construct a connected graph
		DefaultDirectedGraph<String, DefaultEdge> g = 
				new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		ActivityPrecedenceGraph apg = new ActivityPrecedenceGraph(g);
		
		//get the discovered constraints sets 
		HashMap<Integer,List<String>> constraintParametersMap =output.getConstraintParametersMap();
		
		//get the miner precedence relations, and construct the activity precedence graph
		for(ConstraintDefinition cd : output.getModel().getModel().getConstraintDefinitions() ){
			// the precedence relations are obtained.
			String source =constraintParametersMap.get(cd.getId()).get(0);
			String target =constraintParametersMap.get(cd.getId()).get(1);

			String Newsource =constraintParametersMap.get(cd.getId()).get(0);
			String Newtarget =constraintParametersMap.get(cd.getId()).get(1);
			//if the event types are considered, the discovered rules are described as "G-complete", we need to remove the "complete" part. 
			if(source.contains("-complete"))
			{
				Newsource=source.replaceAll("-complete", " ").trim();
				Newtarget=target.replaceAll("-complete", " ").trim();
			}
			
			System.out.println("All discovered preceeding interactions: "+Newsource +"-->"+Newtarget);
			
			apg.addVertex(Newsource);
			apg.addVertex(Newtarget);
			apg.addEdge(Newsource, Newtarget);
		}
		
//		System.out.println("Before transitive reduction, the number of edges:" +apg.getAllEdges().size());
//		
//		// perform the transitive reduction for the activity precedency graph
//		TransitiveReduction.INSTANCE.reduce(apg.getActivityPrecedenceGraph());
//		System.out.println("After transitive reduction, the number of edges:" +apg.getAllEdges().size());
		
		return apg;
	}
	
	// discover interaction relation among activities that belong to different organizations. 
	public static HashSet<CrossOrganizationInteraction> discoverCrossOrganizationInteractions(OrganizationConfig orgConfig, DeclareMinerOutput output)
	{
		HashSet<CrossOrganizationInteraction> allInteractions = new HashSet<>();
		ActivityPrecedenceGraph apg =CrossOrganizationInteractionRelationDiscovery.ActivityPrecedencyGraphConstruction(output);
		
		//we only consider precedence relations that involves activities that belong to different organizations
		
		for(DefaultEdge edge: apg.getAllEdges())
		{
			//get the source and target of each relation
			String sourceActivity = apg.getActivityPrecedenceGraph().getEdgeSource(edge);
			String targetActivity =apg.getActivityPrecedenceGraph().getEdgeTarget(edge);
			String sourceActivityOrg = orgConfig.getOrganization4Activity(sourceActivity);
			String targetActivityOrg = orgConfig.getOrganization4Activity(targetActivity);
			
			//check the belonging organization of them, if they are different, then create an interaction.
			if(sourceActivityOrg!=null && targetActivityOrg!=null
					&& !sourceActivityOrg.equals(targetActivityOrg))
			{
				System.out.println("Source Activity:" +sourceActivity);
				System.out.println("Target Activity:" +targetActivity);
				
				//create an interaction
				OrgActivity sourceOrgActivity = new OrgActivity(sourceActivity, sourceActivityOrg);
				OrgActivity targetOrgActivity = new OrgActivity(targetActivity, targetActivityOrg);
				
				HashSet<OrgActivity> sources = new HashSet<>();
				sources.add(sourceOrgActivity);
				HashSet<OrgActivity> targets = new HashSet<>();
				targets.add(targetOrgActivity);
				
				CrossOrganizationInteraction inter= new CrossOrganizationInteraction(sources, targets);
				allInteractions.add(inter);
			}
		}
		
		return allInteractions;
		
	}
	
	
	
}
