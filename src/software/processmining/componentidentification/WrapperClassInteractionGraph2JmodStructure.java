package software.processmining.componentidentification;

import org.jgrapht.graph.DefaultWeightedEdge;

import ch.epfl.lis.networks.Edge;
import ch.epfl.lis.networks.EdgeFactory;
import ch.epfl.lis.networks.NetworkException;
import ch.epfl.lis.networks.Node;
import ch.epfl.lis.networks.NodeFactory;
import ch.epfl.lis.networks.Structure;
import software.processmining.classinteractiondiscovery.ClassInteractionGraph;

/*
 * this class aims to wrapper a ClassInteractionGraph to a Structure. 
 * Note that ClassInteractionGraph is a directed weighted graph based on GraphT.
 */
public class WrapperClassInteractionGraph2JmodStructure {

	public static Structure<Node, Edge<Node>> wrapperClassInteractionGraph(ClassInteractionGraph cig) throws NetworkException, Exception
	{
		//step1: create the structure of the network
		NodeFactory<Node> nodeFactory = new NodeFactory<Node>(new Node());
		EdgeFactory<Edge<Node>> edgeFactory= new EdgeFactory<Edge<Node>>(new Edge<Node>());
		
		/*
		 * structure is essentially a directed weighted graph. 
		 */
		Structure<Node, Edge<Node>> structure = new Structure<Node,Edge<Node>>(nodeFactory,edgeFactory);
		
		for(DefaultWeightedEdge edge:cig.getAllEdges())
		{			
			Node from =nodeFactory.create();
			Node to =nodeFactory.create();
			from.setName(cig.getClassInteractionGraph().getEdgeSource(edge));
			to.setName(cig.getClassInteractionGraph().getEdgeTarget(edge));
			
			Edge<Node> e = edgeFactory.create();
			e.setSource(from);
			e.setTarget(to);
			e.setWeight(cig.getClassInteractionGraph().getEdgeWeight(edge));
			
			structure.addNode(from);
			structure.addNode(to);
			structure.addEdge(e);
		}
		
//		//Step3: writing the structure to a file. 
//		TSVParser<Node, Edge<Node>> parser = new TSVParser<Node, Edge<Node>>(structure);
//		parser.write(new File("src\\software\\processmining\\componentidentification\\ClassInteractionStructure.tsv").toURI());
		
		return structure;
	}
}
