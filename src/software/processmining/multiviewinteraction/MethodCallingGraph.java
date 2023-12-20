package software.processmining.multiviewinteraction;

import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import UtilityClasses.MethodClass;

/**
 * this class defines a directed graph based on GraphT.
 * it is used to represent the method calling relation in a software log. 
 * @author cliu3
 *
 */
public class MethodCallingGraph {
	//A directed weighted graph is a non-simple directed graph in which multiple edges between any two vertices are not permitted, 
	//but loops are. The graph has weights on its edges.
	private DefaultDirectedGraph<MethodClass, DefaultEdge> g = 
			new DefaultDirectedGraph<MethodClass, DefaultEdge>(DefaultEdge.class);
	
	public MethodCallingGraph(DefaultDirectedGraph<MethodClass, DefaultEdge> g)
	{
		this.g =g;
	}
	
	/**
	 * add vertex to the graph
	 * @param name
	 */
	public void addVertex(MethodClass method) {
			g.addVertex(method);
	}
	
	/**
	 * add edge to the vertex
	 * @param v1
	 * @param v2
	 * @return
	 */
	public DefaultEdge addEdge(MethodClass m1, MethodClass m2) {
		return g.addEdge(m1, m2);
	}
	
	public DefaultEdge addEdge(DefaultEdge edge)
	{
		return g.addEdge(g.getEdgeSource(edge), g.getEdgeTarget(edge));
	}
	
	/**
	 * return the current graph
	 * @return
	 */
	public DefaultDirectedGraph<MethodClass, DefaultEdge> getMethodCallingGraph() {
		return g; 	
	}
	
	/**
	 * get the edge set
	 * @return
	 */
	public Set<DefaultEdge> getAllEdges()
	{
		return g.edgeSet();
	}
	
	/**
	 * get the vertext set
	 * @return
	 */
	public Set<MethodClass> getAllVertexes()
	{
		return g.vertexSet();
	}
}
