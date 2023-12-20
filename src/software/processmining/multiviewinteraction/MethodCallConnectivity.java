package software.processmining.multiviewinteraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import UtilityClasses.ClassClass;
import UtilityClasses.ClassTypeHierarchy;
import UtilityClasses.MethodClass;
import openXESsoftwareextension.XSoftwareExtension;
import software.designpattern.dynamicdiscovery.DiscoverClassTypeHierarchyPlugin;

public class MethodCallConnectivity {

	//given a software event log, we first construct a directed graph, such that 
	//vertex are method, and edges represent method calling relation. 
	
	/*
	 * 2017-11-30 the most tricky thing. 
	 * For the same method call, the same method may be recorded as different pac.class name. 
	 * Our solution: we use the classtype hierarchy information discovered from the log. 
	 * Connect those methods that (1) has the same method name and (2) belongs to the same class type hierarchy. 
	 */

	public static MethodCallingGraph methodCallGraph(XLog softwarelog)
	{
		// vertex set
		HashSet<MethodClass> vertexSet = new HashSet<MethodClass>();
		// edge set
		HashSet<Edge> edgeSet = new HashSet<Edge>();
		
		//we discover a cig for each trace, and finally merge them together
		for (XTrace trace: softwarelog)
		{
			for(XEvent event:trace)
			{
				if (XSoftwareExtension.instance().extractCallerclass(event).equals("null"))
				{
					//for each node, we use the packagename.classname to represent
					MethodClass m = new MethodClass();
					m.setPackageName(XSoftwareExtension.instance().extractPackage(event));
					m.setClassName(XSoftwareExtension.instance().extractClass(event));
					m.setMethodName(XConceptExtension.instance().extractName(event));
					vertexSet.add(m);
				}
				else
				{				
					//for source method
					MethodClass sourceM = new MethodClass();
					sourceM.setPackageName(XSoftwareExtension.instance().extractCallerpackage(event));
					sourceM.setClassName(XSoftwareExtension.instance().extractCallerclass(event));
					sourceM.setMethodName(XSoftwareExtension.instance().extractCallermethod(event));
					//for target method
					MethodClass targetM = new MethodClass();
					targetM.setPackageName(XSoftwareExtension.instance().extractPackage(event));
					targetM.setClassName(XSoftwareExtension.instance().extractClass(event));
					targetM.setMethodName(XConceptExtension.instance().extractName(event));
					vertexSet.add(sourceM);
					vertexSet.add(targetM);
					
					Edge e = new Edge(sourceM, targetM);
					edgeSet.add(e);
				}
			}
		}
		
		//add the more edges based on the class type hierarchy information. 
		ClassTypeHierarchy  cth =DiscoverClassTypeHierarchyPlugin.discoveryClassTypeHierarchyNew(softwarelog);
		ArrayList<HashSet<ClassClass>> classTypeHierarchies = new ArrayList<>();
		for(HashSet<ClassClass> h: cth.getAllCTH())
		{
			if(h.size()>1)// we only care about classes with inheritance relations. 
			{
				classTypeHierarchies.add(h);
			}
		}
		
		HashMap<String, HashSet<MethodClass>> methodName2MethoSet = new HashMap<>();
		for(MethodClass m: vertexSet)
		{
			if(!methodName2MethoSet.keySet().contains(m.getMethodName()))
			{
				HashSet<MethodClass> ms = new HashSet<>();
				ms.add(m);
				methodName2MethoSet.put(m.getMethodName(), ms);
			}
			else{
				methodName2MethoSet.get(m.getMethodName()).add(m);
			}
		}
		
		for(String m:methodName2MethoSet.keySet())
		{
			System.out.println(m+"-->"+methodName2MethoSet.get(m));
		}
		
		//for any method with the same method name and their pac+class belongs to the same class type hierarchy, we add two directional arcs to make sure they are reachable from each other. 
		for(String m: methodName2MethoSet.keySet())
		{
			if(methodName2MethoSet.get(m).size()>1)
			{
				for (MethodClass m1:methodName2MethoSet.get(m))
				{
					for (MethodClass m2:methodName2MethoSet.get(m))
					{
						if(m1!=m2) //remove self loop
						{
							//if their class belongs to the same hierarchy
							for(HashSet<ClassClass> cs: classTypeHierarchies){
								if(cs.contains(getClassFromMethod(m1)) && cs.contains(getClassFromMethod(m2)))
								{
									//add two directional arcs
									Edge e1 = new Edge(m1, m2);
									Edge e2 = new Edge(m2, m1);
									edgeSet.add(e1);
									edgeSet.add(e2);
								}
							}
						}
					}
				}
			}
		}
		
		
		//create class interaction graph
		DefaultDirectedGraph<MethodClass, DefaultEdge> Newg = 
				new DefaultDirectedGraph<MethodClass, DefaultEdge>(DefaultEdge.class);
		MethodCallingGraph mcg = new MethodCallingGraph(Newg);
		
		// add vertexs
		for (MethodClass vertex:vertexSet)
		{
			mcg.addVertex(vertex);
		}
		
		//add edges with weights
		for(Edge ed: edgeSet)
		{
			mcg.addEdge(ed.source,ed.target);
		}
		
		return mcg;
	}
	
	
	//check connectivity between two methods, it returns true if there is a path. 
	/*
	 * unfortunately, pathiExists support undirected pathe. ref: http://jgrapht.org/javadoc/org/jgrapht/alg/ConnectivityInspector.html
	 */
	public static boolean connectivityChecking(MethodCallingGraph mcg, MethodClass sourceMethod, MethodClass targetMethod)
	{
		if(DijkstraShortestPath.findPathBetween(mcg.getMethodCallingGraph(), sourceMethod, targetMethod)!=null)
		{
			System.out.println(sourceMethod+"-->"+targetMethod+": connect!");
			return true;
		}
		else 
		{
			System.out.println(sourceMethod+"-->"+targetMethod+": XXXXX!");
			return false;
		}
		//return ci.pathExists(sourceMethod, targetMethod);
	}
	
	public static ClassClass getClassFromMethod(MethodClass method)
	{
		ClassClass c = new ClassClass();
		c.setClassName(method.getClassName());
		c.setPackageName(method.getPackageName());
		return c;
	}
	
}
