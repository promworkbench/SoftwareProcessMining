package software.processmining.componentidentification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashSet;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;

import ModularityOptimizer_ThirdPartyLibrary.ModularityOptimizer;
import UtilityClasses.ClassClass;
import UtilityClasses.ComponentConfig;
import UtilityFunctions.ExtractMethodClassPackage;
import ch.epfl.lis.jmod.Jmod;
import ch.epfl.lis.jmod.JmodNetwork;
import ch.epfl.lis.jmod.JmodSettings;
import ch.epfl.lis.networks.Edge;
import ch.epfl.lis.networks.NetworkException;
import ch.epfl.lis.networks.Node;
import ch.epfl.lis.networks.Structure;
import ch.epfl.lis.networks.parsers.TSVParser;
import software.processmining.classinteractiondiscovery.ClassInteractionGraph;
import software.processmining.classinteractiondiscovery.ClassInteractionGraphPlugin;

/**
 * this plugin aims to provide a set of approaches to clustering classes to identify components. 
 * Input 1: a software event log (original data, each trace refers to one software execution);
 * Output: a set of component configurations.
 * 
 * Approach1: Markov Cluster Algorithm, 
 * https://www.programcreek.com/java-api-examples/index.php?source_dir=textclustering-master/src/net/sf/javaml/clustering/mcl/MarkovClustering.java
 * 
 * Community detection --> modularity
 * Component identification --> high cohesion and low coupling 
 * Jmod is an open-source Java library to perform module detection in networks. http://tschaffter.ch/projects/jmod/ 
 * 
 * Newman's spectral algorithm
 * Optimization1: Moving vertex method (MVM)
 * Optimization2: Global moving vertex method (gMVM)
 * Genetic algorithm-based method
 * Brute force approach
 * 
 * @author cliu3 2017-12-8
 *
**/

@Plugin(
		name = "Integrated Clustering based Component Identification Tool",// plugin name
		
		returnLabels = {"Software Description"}, //return labels
		returnTypes = {ComponentConfig.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Event Log"},
		
		userAccessible = true,
		help = "This plugin aims to provide a set of approaches to identification interfaces." 
		)
public class ClusteringBasedComponentIdentificationPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl;liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Identifying Component, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	public ComponentConfig componentIdentification(UIPluginContext context, XLog softwarelog) throws NetworkException, Exception 
	{
       	ComponentConfig comconfig = new ComponentConfig();
		
		//select the approach for discovering interfaces. 
		String [] clusteringApproach = new String[6];
		//clusteringApproach[0]="Markov clustering algorithm"; //Markov Cluster Algorithm, https://micans.org/mcl/
		clusteringApproach[0]="Newman's spectral algorithm";
		clusteringApproach[1]="Newman's spectral algorithm (MVM)";
		clusteringApproach[2]="Newman's spectral algorithm (MVM + gMVM)";
		clusteringApproach[3]="Louvain algorithm";
		clusteringApproach[4]="Louvain algorithm (Multi-level Refinement)";
		clusteringApproach[5]="Smart local moving algorithm";

		String selectedDiscoveryApproach = ProMUIHelper.queryForObject(context, "Select one approach for discovering interfaces", clusteringApproach);
		context.log("The selected approach to identify component is: "+clusteringApproach, MessageLevel.NORMAL);	
		System.out.println("The selected approach to identify component is: "+selectedDiscoveryApproach);
			
		
		long startTime = System.currentTimeMillis();

		
		//create a classInteraction Graph
		ClassInteractionGraphPlugin cgp = new ClassInteractionGraphPlugin();
		ClassInteractionGraph cig =cgp.cigDiscovery(context, softwarelog);
		
		
		if(selectedDiscoveryApproach.startsWith("Newman"))//for Newman's algorithms, we use the Jmod package
		{
			Structure<Node, Edge<Node>> structure =WrapperClassInteractionGraph2JmodStructure.wrapperClassInteractionGraph(cig);
			
			//show the number of nodes and edges
			System.out.println("the number of nodes: "+structure.getSize());
			System.out.println("the number of edges: "+structure.getNumEdges());
			
			//writing the structure to a file. 
			TSVParser<Node, Edge<Node>> parser = new TSVParser<Node, Edge<Node>>(structure);
			parser.write(new File("src\\software\\processmining\\componentidentification\\ClassInteractionStructure.tsv").toURI());
				
			// modularity detection. 
			JmodNetwork network = new JmodNetwork(structure);
			
			//by default the MVM and gMVM are enabled.  
			JmodSettings settings = JmodSettings.getInstance();
			settings.setCommunityNetworkFormat(Structure.Format.TSV);
			//export the community tree
			settings.setExportCommunityTree(true);
			//set the output directory
			URI outputURI = new File("src\\software\\processmining\\componentidentification\\").toURI();
					
			if(selectedDiscoveryApproach.equals("Newman's spectral algorithm"))
			{
				settings.setUseMovingVertex(false);
				settings.setUseGlobalMovingVertex(false);
			}
			else if(selectedDiscoveryApproach.equals("Newman's spectral algorithm (MVM)")){
				settings.setUseMovingVertex(true);
				settings.setUseGlobalMovingVertex(false);
			}
			else if(selectedDiscoveryApproach.equals("Newman's spectral algorithm (MVM + gMVM)")){
				settings.setUseMovingVertex(true);
				settings.setUseGlobalMovingVertex(true);
			}
			
			Jmod jmod = new Jmod();
			jmod.setOutputDirectory(outputURI);
			jmod.runModularityDetection(network);
			jmod.printResult();
			// includes all data set and communities
			jmod.exportDataset();
			
			//read in the _indivisible_communities.txt, where each line refers to a component. 
			FileInputStream fis = new FileInputStream(new File("src\\software\\processmining\\componentidentification\\_indivisible_communities.txt"));
			 
			//Construct BufferedReader from InputStreamReader
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		 
			String line = null;
			int num=1;
			while ((line = br.readLine()) != null) {
	//			System.out.println(line);
				HashSet<ClassClass> classes = new HashSet<>();
				//for each line we create a component 
				String[] splits = line.split("\t");
				for(int i =1;i<splits.length;i++)
				{
					ClassClass c = new ClassClass();
					c.setClassName(ExtractMethodClassPackage.getLast(splits[i]));
					c.setPackageName(ExtractMethodClassPackage.getFirstfrom2Parts(splits[i]));
					classes.add(c);
				}
				
				comconfig.add("Component"+num, classes);
				num++;
			}
		 
			br.close();
			long endTime = System.currentTimeMillis();
			System.out.println("Execution time of "+selectedDiscoveryApproach+" is: "+ (endTime - startTime)+" milliseconds");
            //System.out.println("Elapsed time: %d seconds%n", Math.round((endTime - startTime) / 1000.0));

			return comconfig;
		}
		else{//for other community algorithms, we use Modularity Optimizer package
			
			//construct the tab delimited input file from the cig. 
			WrapperClassInteractionGraph2TabDelimitedFile tdf = new WrapperClassInteractionGraph2TabDelimitedFile(cig);
					
			//set the parameters for the modularity algorithms
			String inputFileName = "src\\software\\processmining\\componentidentification\\input_tab_delimited_text_file.txt";// input file directory 
			String outputFileName = "src\\software\\processmining\\componentidentification\\output_community_result_text_file.txt";// output file directory
	        
			//we use the standard modularity function, e.g., 1. 
			String modularityFunction = "1";
			
			//for the resolution parameter, we set 1.0. 
	        String resolution = "1"; 
	        
	        //the type of algorithm to use, (1 = Louvain; 2 = Louvain with multilevel refinement; 3 = smart local moving)
	        String algorithm = "0"; 
	        if(selectedDiscoveryApproach.equals("Louvain algorithm"))
			{
	        	algorithm = "1"; 
			}
	        else if(selectedDiscoveryApproach.equals("Louvain algorithm (Multi-level Refinement)"))
	        {
	        	algorithm = "2"; 
	        }
	        else if(selectedDiscoveryApproach.equals("Smart local moving algorithm"))
	        {
	        	algorithm = "3"; 
	        }
	        
	        // the number of random starts, e.g., 10
	        String nRandomStarts = "10";
	        
	        //the number of iterations, e.g., 10.
	        String nIterations = "10";
	        
	        //the random seed, e.g., 0.
	        String randomSeed = "0";
	        
	        //Print output (0 = no; 1 = yes)
	        String printOutput = "0"; 
	        
	        String [] para = {inputFileName, outputFileName, modularityFunction, resolution, algorithm, nRandomStarts, nIterations, randomSeed, printOutput};
	        
	        //the community detection implementation. 
	        ModularityOptimizer.main(para); 

	        
			//construct component results form the output_community_result_text_file.txt 
	        tdf.constructComponentConfigFromCommunityResult(outputFileName, comconfig);
	        long endTime = System.currentTimeMillis();
	        
			System.out.println("Execution time of "+selectedDiscoveryApproach+" is: "+ (endTime - startTime)+" milliseconds");
			
			return comconfig;
		}
	}
}
