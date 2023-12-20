package software.processmining.classinteractiondiscovery;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import UtilityClasses.ClassClass;
import UtilityClasses.ComponentConfig;
import software.processmining.imports.ImportComponentConfiguration;


/*
 * this class aims to clustering the class interaction graph to different groups, by taking a threshold as input. 
 * The class interaction graph is a weighted directed graph.
 * Step1: remove the edges whose weight is less than the threshold; and
 * Step2: Use the weakly connected component algorithm (GraphT package) to get clusters. 
 * 
 */
public class ClusteringClassInteractionGraph {
	/**
	 * get the highest weight for the graph.
	 * @param cig
	 * @return
	 */
	public static int getHighestVaule(ClassInteractionGraph cig)
	{
		int highestValue=0;
		Set<DefaultWeightedEdge> edgeSet = cig.getAllEdges();
		
		for(DefaultWeightedEdge edge: edgeSet)
		{
			// if the weight of an edge bigger than the current maximal value
			if(highestValue<cig.getEdgeWeight(edge))
			{
				highestValue = (int) cig.getEdgeWeight(edge);
			}
		}
		
		return highestValue+10;
	}
	
	/**
	 * use the threshold to filter the graph, i.e., edges whose whose weight is less than the threshold, are removed. 
	 * @param threshold
	 * @param cog
	 * @return
	 */
	public static ClassInteractionGraph filterEdges(double threshold, ClassInteractionGraph cig)
	{
		// create a new cig
		DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g = 
				new DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		ClassInteractionGraph newg =new ClassInteractionGraph(g);
		
		// add nodes to newg
		for (String vertex: cig.getAllVertexes())
		{
			newg.addVertex(vertex);
		}
		
		Set<DefaultWeightedEdge> edgeSetKeeped = new HashSet<>();
		
		for(DefaultWeightedEdge edge: cig.getAllEdges())
		{
			// if the weight of an edge is bigger than the threshold, we add this edge should be add to the newg.
			if(threshold<=cig.getEdgeWeight(edge))
			{
				//newg.removeEdge(edge);
				edgeSetKeeped.add(edge);
			}
		}
		
		//add the edges to newg
		for(DefaultWeightedEdge e: edgeSetKeeped)
		{
			//System.out.println(e+" is add!");
			DefaultWeightedEdge tempE = newg.addEdge(e);
			newg.setEdgeWeight(tempE, cig.getEdgeWeight(e));
		}
		
		return newg;
	}
	
	/**
	 * for the filterd class interaction graph, we get its weakly connected graphs. 
	 * @param cog
	 * @return
	 */
	
	public static ComponentConfig getClusters(ClassInteractionGraph cig)
	{
		//compute all weakly connected component
	    ConnectivityInspector ci = new ConnectivityInspector(cig.getClassInteractionGraph());
	    
	    //Returns a list of Set s, where each set contains all vertices that are in the same maximally connected component.
	    java.util.List connected = ci.connectedSets();
	    
	    ComponentConfig comConfig = new ComponentConfig();
	    
	    for (int i=0;i<connected.size();i++)
	    {
	    	Set<ClassClass> classSet = new HashSet<ClassClass>();
	    	for(String s:(Set<String>)connected.get(i))
	    	{
	    		ClassClass c = new ClassClass();
	    		c.setClassName(ImportComponentConfiguration.extractClass(s));
	    		c.setPackageName(ImportComponentConfiguration.extractPackage(s));
	    		classSet.add(c);
	    	}
	    	comConfig.add("Component"+i, classSet);
	    }
	    return comConfig;
	}
	
}
