package software.processmining.classinteractiondiscovery;

import java.util.Set;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
/**
 * this class defines a directed weighted graph based on GraphT.
 * it is used to represent the class interaction relation during the software execution. 
 * @author cliu3
 *
 */
public class ClassInteractionGraph {

	//A directed weighted graph is a non-simple directed graph in which multiple edges between any two vertices are not permitted, 
	//but loops are. The graph has weights on its edges.
	private DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g = 
			new DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
	
	public ClassInteractionGraph(DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g)
	{
		this.g =g;
	}
	
	/**
	 * add vertex to the graph
	 * @param name
	 */
	public void addVertex(String name) {
			g.addVertex(name);
	}
   
	/**
	 * add edge to the vertex
	 * @param v1
	 * @param v2
	 * @return
	 */
	public DefaultWeightedEdge addEdge(String v1, String v2) {
		return g.addEdge(v1, v2);
	}
	
	public DefaultWeightedEdge addEdge(DefaultWeightedEdge edge)
	{
		return g.addEdge(g.getEdgeSource(edge), g.getEdgeTarget(edge));
	}
   
	/**
	 * set the weight of each edge.
	 * @param e
	 * @param edge_weight
	 */
	public void setEdgeWeight(DefaultWeightedEdge e, double edge_weight) {
		g.setEdgeWeight(e, edge_weight);
	}
   
	/**
	 * return the current graph
	 * @return
	 */
	public DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> getClassInteractionGraph() {
		return g; 	
	}
	
	/**
	 * get the edge set
	 * @return
	 */
	public Set<DefaultWeightedEdge> getAllEdges()
	{
		return g.edgeSet();
	}
	
	/**
	 * get the weight of an edge
	 * @param edge
	 * @return
	 */
	public double getEdgeWeight(DefaultWeightedEdge edge)
	{
		return g.getEdgeWeight(edge);
	}
	
	/**
	 * get the vertext set
	 * @return
	 */
	public Set<String> getAllVertexes()
	{
		return g.vertexSet();
	}
}
