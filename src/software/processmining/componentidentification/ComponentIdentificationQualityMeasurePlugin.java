package software.processmining.componentidentification;

import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.model.XLog;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import UtilityClasses.ClassClass;
import UtilityClasses.ComponentConfig;
import software.processmining.classinteractiondiscovery.ClassInteractionGraph;
import software.processmining.classinteractiondiscovery.ClassInteractionGraphPlugin;

/*
 * this plugin aims to measure the quality of a component configuration from the following perspectives:
 * 1 size: 
 * 1.1 The ratio of single class components (RSC). 
 * 1.2 The ratio of largest component (RLC). 
 * 1.3 The ratio of intermediate components (RIC). 
 * 2. coupling:
 * 3. cohesion: 
 * 4. Modularity quality = cohesion - coupling. 
 */
@Plugin(
		name = "Quality Measure of Component Configuration",// plugin name
		
		returnLabels = {"QualityMetrics"}, //return labels
		returnTypes = {String.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Event Log", "Component Configuration"},
		
		userAccessible = true,
		help = "This plugin aims to measure the quality metrics of the component configuration." 
		)
public class ComponentIdentificationQualityMeasurePlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl;liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Quality Metrics of Component Configuration, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0, 1}
			)
	public String componentIdentification(UIPluginContext context, XLog softwarelog, ComponentConfig config)
	{
		//create a classInteraction Graph
		ClassInteractionGraphPlugin cgp = new ClassInteractionGraphPlugin();
		ClassInteractionGraph cig =cgp.cigDiscovery(context, softwarelog);
		
		//compute the size of component config
		Size size =  computingSize(config);
		
		//create a mapping from component to its class set
		HashMap<String, HashSet<String>> component2ClassSet = new HashMap<>();
		
		for(String com: config.getAllComponents())
		{
			HashSet<String> classSetString = new HashSet<>();
			for(ClassClass c: config.getClasses(com))
			{
				classSetString.add(c.toString());
			}
			component2ClassSet.put(com, classSetString);
		}
		
		//compute the cohesion of component config
		double cohesion =computingCohesion(component2ClassSet, cig);
		
		//compute the coupling of component config
		double coupling =computingCoupling1(component2ClassSet, cig);
		
		//compute the modularity quality of component config
		double mq = cohesion-coupling;
		//create the out put html
		StringBuffer buffer = new StringBuffer();
		buffer.append("<html>"); 
		buffer.append("<body>");
		
		buffer.append("<h1 style=\"color:blue;\">"+"Quality Metrics of Component Configuration"+"</h1>");  
		
		buffer.append("<h2 style=\"color:#2F8AA1;\">"+"The number of components: "+config.getAllComponents().size()+"</h2>");
		buffer.append("<h2 style=\"color:#2F8AA1;\">"+"The ratio of single class components (RSC): "+size.RSC+"</h2>");
		buffer.append("<h2 style=\"color:#2F8AA1;\">"+"The ratio of largest component (RLC): "+size.RLC+"</h2>");
		buffer.append("<h2 style=\"color:#2F8AA1;\">"+"The ratio of intermediate components (RIC): "+size.RIC+"</h2>");

		buffer.append("<h2 style=\"color:#2F8AA1;\">"+"The coupling metric: "+coupling+"</h2>");
		buffer.append("<h2 style=\"color:#2F8AA1;\">"+"The cohesion metric: "+cohesion+"</h2>");
		buffer.append("<h2 style=\"color:#2F8AA1;\">"+"The modularity quality metric: "+mq+"</h2>");
		
		buffer.append("</body>");
		buffer.append("</html>");
		return buffer.toString();
	}
	
	//compute the size
	public static Size computingSize(ComponentConfig config)
	{
		Size size = new Size();
		int numberSingleClass=0;// the number of classes in single class component
		int numberLargestClass =0;// the number of classes in the largest component
		int numberAllClasses =0; // the number of all classes
		for(String com: config.getAllComponents())
		{
			int currentSize = config.getClasses(com).size();
			numberAllClasses=numberAllClasses+currentSize;
			
			if(currentSize==1)
			{
				numberSingleClass=numberSingleClass+currentSize;
			}
			if(currentSize>numberLargestClass)
			{
				numberLargestClass=currentSize;
			}
		}
		
		//compute RSC
		size.RSC = (double)numberSingleClass/numberAllClasses;
		if(numberLargestClass==1)
		{
			numberLargestClass=0;
		}
		size.RLC = (double)numberLargestClass/numberAllClasses;
		size.RIC = (numberAllClasses-numberSingleClass-numberLargestClass)/(double)numberAllClasses;
		
		return size;
	}
	
	//compute the coupling (the class interaction level)
	public static double computingCoupling1(HashMap<String, HashSet<String>> component2ClassSet, ClassInteractionGraph cig)
	{
		if(component2ClassSet.keySet().size()==1)//if the number of component is 1, no coupling. 
		{
			return 0;
		}
			
		//we use the following mapping to store the number of edges cross different component pairs (without considering the order)
		HashMap<HashSet<String>, Double> componentPair2ClassSet = new HashMap<>();
		
		for(String com1: component2ClassSet.keySet())
		{
			for(String com2: component2ClassSet.keySet())
			{
				if(!com1.equals(com2))//for any two components
				{
					HashSet<String> componentPair = new HashSet<>();
					componentPair.add(com1);
					componentPair.add(com2);
					if(!componentPair2ClassSet.keySet().contains(componentPair))//the current pair is already computed.
					{
						//count the number of edges that belonging to com1 and com2.
						int count=0;
						HashSet<String> classSetString1 = component2ClassSet.get(com1);
						HashSet<String> classSetString2 = component2ClassSet.get(com2);
						for(DefaultWeightedEdge edge:cig.getAllEdges())
						{
							//we do not consider self-loop
							if(cig.getClassInteractionGraph().getEdgeSource(edge).equals(cig.getClassInteractionGraph().getEdgeTarget(edge)))
								continue;
							//if both the source and target nodes are included in the component vertex
							if(classSetString1.contains(cig.getClassInteractionGraph().getEdgeSource(edge))&&
									classSetString2.contains(cig.getClassInteractionGraph().getEdgeTarget(edge))
									||classSetString2.contains(cig.getClassInteractionGraph().getEdgeSource(edge))&&
									classSetString1.contains(cig.getClassInteractionGraph().getEdgeTarget(edge)))
							{
								count++;
							}
						}
						componentPair2ClassSet.put(componentPair, (double)count/(classSetString1.size()*classSetString2.size()));
						
					}
				}
			}
		}
		
		double totalCoupling =0;
		for(HashSet<String> pair: componentPair2ClassSet.keySet())
		{
			System.out.print(pair+" -->" + componentPair2ClassSet.get(pair));
			totalCoupling=totalCoupling+ componentPair2ClassSet.get(pair);
		}
		
		//return the coupling metric for all components
		return totalCoupling/componentPair2ClassSet.keySet().size();	
	}
//	//compute the coupling (the component level)
//	public static double computingCoupling(HashMap<String, HashSet<String>> component2ClassSet, ClassInteractionGraph cig)
//	{
//		if(component2ClassSet.keySet().size()==1)//if the number of component is 1, no coupling. 
//		{
//			return 0;
//		}
//		double totalCoupling =0;
//		
//		//computing coupling for each component
//		for(String com: component2ClassSet.keySet())
//		{					
//			HashSet<String> classSetString = component2ClassSet.get(com);
//			
//			HashSet<String> classSet =new HashSet<>();
//			for(DefaultWeightedEdge edge:cig.getAllEdges())
//			{
//				//if both the source and target nodes are included in the component vertex
//				if(!classSetString.contains(cig.getClassInteractionGraph().getEdgeSource(edge)))
//				{
//					classSet.add(cig.getClassInteractionGraph().getEdgeSource(edge));
//				}		
//				
//				if(!classSetString.contains(cig.getClassInteractionGraph().getEdgeTarget(edge)))
//				{
//					classSet.add(cig.getClassInteractionGraph().getEdgeTarget(edge));
//				}
//			}
//			
//			//count the number of components that are coupled with the current one, i.e., existing an edge from a node in the component to the target component
//			HashSet<String> couplingComponentSet = new HashSet<>();
//			
//			//get the component from the classSet
//			for(String c: classSet)
//			{
//				for(String component: component2ClassSet.keySet())
//				{
//					if(component2ClassSet.get(component).contains(c))
//					{
//						couplingComponentSet.add(component);
//						break;
//					}
//				}
//			}
//			
//			//compute coupling  of the current component
//			totalCoupling = totalCoupling+ couplingComponentSet.size()/(component2ClassSet.keySet().size()-1);
//			
//		}
//		
//		//compute the coupling of all components
//		return totalCoupling/component2ClassSet.keySet().size();
//	}
//	
	
	//compute the cohesion. 
	//Specially, if each component only has one node, its cohesion should be 0. 
	public static double computingCohesion(HashMap<String, HashSet<String>> component2ClassSet, ClassInteractionGraph cig)
	{
		double totalCohesion =0;
		//computing cohesion for each component
		for(String com: component2ClassSet.keySet())
		{
			HashSet<String> classSetString = component2ClassSet.get(com);
						
			//the number of nodes in the current component 
			int numberNode = classSetString.size();
			
			//count the number of edges that are belonging to current component
			int count=0;
			for(DefaultWeightedEdge edge:cig.getAllEdges())
			{
				//we do not consider self-loop
				if(cig.getClassInteractionGraph().getEdgeSource(edge).equals(cig.getClassInteractionGraph().getEdgeTarget(edge)))
					continue;
				
				//if both the source and target nodes are included in the component vertex
				if(classSetString.contains(cig.getClassInteractionGraph().getEdgeSource(edge))&&
						classSetString.contains(cig.getClassInteractionGraph().getEdgeTarget(edge)))
				{
					count++;
				}
			}
			
			//compute cohesion (as directed graph) of the current component
			totalCohesion = totalCohesion+ 2*(double)count/(numberNode*numberNode);
		}
		
		//compute the cohesion of all components
		return totalCohesion/component2ClassSet.keySet().size();
	}
}

class Size{
	double RSC =0;//  ratio of single class components
	double RLC =0;//  ratio of largest component
	double RIC =0;//  ratio of intermediate components
}
