package software.processmining.componentidentification;
import java.io.File;
import java.net.URI;

import ch.epfl.lis.jmod.Jmod;
import ch.epfl.lis.jmod.JmodNetwork;
import ch.epfl.lis.jmod.JmodSettings;
import ch.epfl.lis.networks.Edge;
import ch.epfl.lis.networks.EdgeFactory;
import ch.epfl.lis.networks.NetworkException;
import ch.epfl.lis.networks.Node;
import ch.epfl.lis.networks.NodeFactory;
import ch.epfl.lis.networks.Structure;
import ch.epfl.lis.networks.parsers.TSVParser;

public class testJmod {

	//Jmod API: http://tschaffter.ch/projects/jmod/apis/1.2.2b/
	public static void main (String []args) throws NetworkException, Exception
	{
		
		// we test the input and output of Jmod library. 
		
		//step1: create the structure of the network
		NodeFactory<Node> nodeFactory = new NodeFactory<Node>(new Node());
		EdgeFactory<Edge<Node>> edgeFactory= new EdgeFactory<Edge<Node>>(new Edge<Node>());
		
		/*
		 * structure is essentially a directed weighted graph. 
		 */
		Structure<Node, Edge<Node>> structure = new Structure<Node,Edge<Node>>(nodeFactory,edgeFactory);
		
		//step2: add two nodes and one edge to the structure.
		Node a =nodeFactory.create();
		Node b =nodeFactory.create();
		a.setName("a");
		b.setName("b");
		
		Edge<Node> e1 = edgeFactory.create();
		e1.setSource(a);
		e1.setTarget(b);
		e1.setWeight(2);

		Edge<Node> e2 = edgeFactory.create();
		e2.setSource(b);
		e2.setTarget(a);
		e2.setWeight(3);
		
		structure.addNode(a);
		structure.addNode(b);
		structure.addEdge(e1);
		structure.addEdge(e2);
	
		Node c =nodeFactory.create();
		Node d =nodeFactory.create();
		c.setName("c");
		d.setName("d");
		
		Edge<Node> e3 = edgeFactory.create();
		e3.setSource(c);
		e3.setTarget(d);
		e3.setWeight(2);

		Edge<Node> e4 = edgeFactory.create();
		e4.setSource(d);
		e4.setTarget(c);
		e4.setWeight(3);
		
		Edge<Node> e5 = edgeFactory.create();
		e5.setSource(d);
		e5.setTarget(b);
		e5.setWeight(1);
		
		Node e = nodeFactory.create();
		e.setName("e");
		Node f = nodeFactory.create();
		f.setName("f");
		
		Edge<Node> e6 = edgeFactory.create();
		e6.setSource(e);
		e6.setTarget(f);
		e6.setWeight(1);
		
		structure.addNode(a);
		structure.addNode(b);
		structure.addNode(c);
		structure.addNode(d);
		structure.addNode(e);
		structure.addNode(f);
		structure.addEdge(e1);
		structure.addEdge(e2);
		structure.addEdge(e3);
		structure.addEdge(e4);
		structure.addEdge(e5);
		structure.addEdge(e6);
		
		//Step3: writing the structure to a file. 
		TSVParser<Node, Edge<Node>> parser = new TSVParser<Node, Edge<Node>>(structure);
		parser.write(new File("src\\software\\processmining\\componentidentification\\structure.tsv").toURI());
		
//		//Step4: read structure from file. 
//		parser.read(new File("src\\software\\processmining\\componentidentification\\structure.tsv").toURI());
//		
		System.out.println(structure.getSize());
		System.out.println(structure.getNumEdges());
		
		for(String ed: structure.getEdges().keySet())
		{
			System.out.println(structure.getEdges().get(ed));
		}
		
		//step5:modularity detection. 
		JmodNetwork network = new JmodNetwork(structure);
		
		//by default the MVM and gMVM are enabled.  
		JmodSettings settings = JmodSettings.getInstance();
//		settings.setUseGlobalMovingVertex(false);
//		settings.setUseMovingVertex(false);
		
//		//export the modules detected to files
//		settings.setExportCommunityNetworks(true);
		//specify the file format
		settings.setCommunityNetworkFormat(Structure.Format.TSV);
		//colors depending on which module a given node belongs to
//		settings.setExportColoredCommunities(true);
		
		//export the community tree
		settings.setExportCommunityTree(true);
		//set the output directory
		URI outputURI = new File("src\\software\\processmining\\componentidentification\\").toURI();
		
		Jmod jmod = new Jmod();
		jmod.setOutputDirectory(outputURI);
		jmod.runModularityDetection(network);
		jmod.printResult();
		// includes all data set and communities
		jmod.exportDataset();
		
		// the modularity value. 
		jmod.exportModularity(new File(".\\src\\software\\processmining\\componentidentification\\modularityValue.tsv").toURI());		
	}
}
