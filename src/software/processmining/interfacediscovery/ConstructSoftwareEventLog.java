package software.processmining.interfacediscovery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import UtilityClasses.MethodClass;
import UtilityFunctions.InitializeSoftwareEventLog;
import openXESsoftwareextension.XSoftwareExtension;

public class ConstructSoftwareEventLog {
	/*
	 * Given a software event log and component (a set of classes), construct the component event log. 
	 */
	public static XLog generatingComponentEventLog(String component, Set<String> classSet, XLog originalLog, XFactory factory)
	{
		// create log
		XLog componentLog =InitializeSoftwareEventLog.initialize(factory, component);
		
		for(XTrace trace: originalLog)
		{
			XTrace tempTrace = factory.createTrace();
			for(XEvent event: trace)
			{
				// filtering the trace according to the classes
				if(classSet.contains(XSoftwareExtension.instance().extractPackage(event)+"."+XSoftwareExtension.instance().extractClass(event)))
				{
					tempTrace.add(event);
				}
			}
			if(tempTrace.size()>0)// we do not add empty traces
			{
				componentLog.add(tempTrace);
			}
		}
		return componentLog;
	}
	
	/*
	 * Given a component log, the maximal nesting depth,  the top-level method set, the caller method set of each interface
	 * then construct the event log of an interface in a cursive way. 
	 */
	
	public static XLog constructInterfaceLog(XLog comLog, int maximalNesting, HashSet<MethodClass> methodSet, HashSet<MethodClass> callerMethods, XFactory factory)
	{		
		HashMap<Integer, XTrace> nestedTraces = new HashMap<>();
		
		// create log
		XLog interfaceLog =InitializeSoftwareEventLog.initialize(factory, "interface");
		
		//construct top-level method set of the current interface 
		HashSet<String> TopLevelMethodSet = new HashSet<>();
		for(MethodClass c: methodSet)
		{
			TopLevelMethodSet.add(c.getPackageName()+"."+c.getClassName()+"."+c.getMethodName());
		}
				
		//construct caller method set of the top-level method set
		HashSet<String> CallerMethodSet = new HashSet<>();
		for(MethodClass c: callerMethods)
		{
			CallerMethodSet.add(c.getPackageName()+"."+c.getClassName()+"."+c.getMethodName());
		}
		
		for(XTrace trace: comLog)
		{		
			XTrace temptrace = factory.createTrace();
			XTrace topLevelTrace = factory.createTrace();
			
			//get the top-level events
			for(XEvent event: trace)
			{
				//for event (1) included in the top-level method set (2) the caller also satisfied 
				if(TopLevelMethodSet.contains(XSoftwareExtension.instance().extractPackage(event)+"."
						+XSoftwareExtension.instance().extractClass(event)+"."+
						XConceptExtension.instance().extractName(event)) && 
						CallerMethodSet.contains(XSoftwareExtension.instance().extractCallerpackage(event)+"."
								+XSoftwareExtension.instance().extractCallerclass(event)+"."
								+XSoftwareExtension.instance().extractCallermethod(event)))
				{
					topLevelTrace.add(event);
				}
			}
			nestedTraces.put(1, topLevelTrace);
			
			//for each top-level events, if it is a nested event, then add 
			for(int i=1;i<maximalNesting;i++)
			{	
				nestedTraces.put(i+1, factory.createTrace());
				//get the current level traces, start from the top-level (i =1)
				for(XEvent event: nestedTraces.get(i))
				{
					if(checkNesting(event,trace))
					{
						for(XEvent e:trace)
						{
							if(XSoftwareExtension.instance().extractClassObject(event).equals(XSoftwareExtension.instance().extractCallerclassobject(e))
								&&XConceptExtension.instance().extractName(event).equals(XSoftwareExtension.instance().extractCallermethod(e)))
							{
								nestedTraces.get(i+1).add(e);
							}
						}	
					}
				}
			}
			
			//add all traces of different nesting levels to the current trace
			for(int i: nestedTraces.keySet())
			{
				if(nestedTraces.get(i).size()>0)
				temptrace.addAll(nestedTraces.get(i));
			}
			
			interfaceLog.add(temptrace);
		}
		
		return interfaceLog;		
	}
	
	/*
	 * checking if an event is a nested event in a trace
	 */
	public static boolean checkNesting(XEvent event, XTrace trace)
	{
		//get the index of the current event. 
		int index  = trace.indexOf(event);
		System.out.println("caller: "+XConceptExtension.instance().extractName(event));
		for(int i = index; i<trace.size();i++)
		{
			if  (XSoftwareExtension.instance().extractClassObject(event).equals(XSoftwareExtension.instance().extractCallerclassobject(trace.get(i)))
					&&XConceptExtension.instance().extractName(event).equals(XSoftwareExtension.instance().extractCallermethod(trace.get(i)))
					&&!XConceptExtension.instance().extractName(event).equals(XConceptExtension.instance().extractName(trace.get(i))))
			{
				System.out.println("added nesting: "+XConceptExtension.instance().extractName(trace.get(i)));
				return true;
			}
		}
		return false;
	}
	
	
//	/*
//	 * refactoring the interface event log by identifying interface instances, such that each instance form a new trace in the log
//	 */
//	public static XLog constructInterfaceInstanceLog(XLog interfaceLog, XFactory factory, Set<String> classSet)
//	{
//		// create log
//		XLog interfaceInstanceLog =InitializeSoftwareEventLog.initialize(factory, "interface");
//		
//		//for each trace, we first construct its instance objects, return its connected graph. 
//		for(XTrace trace: interfaceLog)
//		{
//			//get the interface instance set
//			List connected = InterfaceInstances(trace,classSet);
//			for (int i=0;i<connected.size();i++)
//	        {
//				XTrace instanceTrace = factory.createTrace();
//				for(XEvent event: trace)
//				{
//					if(((Set<String>)connected.get(i)).contains(XSoftwareExtension.instance().extractClassObject(event)))
//					{
//						instanceTrace.add(event);
//					}
//				}
//				interfaceInstanceLog.add(instanceTrace);
//	        }	
//		}
//		
//		return interfaceInstanceLog;
//				
//	}
	
	//construct the instance for each interface
	public static List InterfaceInstances(XTrace trace, Set<String> classSet)
	{	
		// we first conctruct a connected graph
		 DirectedGraph<String, DefaultEdge> directedGraph =
		            new DefaultDirectedGraph<String, DefaultEdge>
		            (DefaultEdge.class);
		 
		 // traverse through each event in the case
		 for (XEvent event :trace)
		 {
			 directedGraph.addVertex(XSoftwareExtension.instance().extractClassObject(event));
			
			 //if the caller of this recording belongs to the component/interface.
			if (classSet.contains(XSoftwareExtension.instance().extractCallerpackage(event)+"."+XSoftwareExtension.instance().extractCallerclass(event)))
			{
				directedGraph.addVertex(XSoftwareExtension.instance().extractCallerclassobject(event));
				// add an arc from caller to callee
				directedGraph.addEdge(XSoftwareExtension.instance().extractClassObject(event), 
						XSoftwareExtension.instance().extractCallerclassobject(event));
			}
		 }	
		//compute all weakly connected component
        ConnectivityInspector ci = new ConnectivityInspector(directedGraph);
        
        //Returns a list of Set s, where each set contains all vertices that are in the same maximally connected component.
        java.util.List connected = ci.connectedSets();
        return connected;        
	}
}
