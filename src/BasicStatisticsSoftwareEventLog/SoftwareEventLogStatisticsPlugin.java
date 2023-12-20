package BasicStatisticsSoftwareEventLog;

import java.util.HashSet;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import openXESsoftwareextension.XSoftwareExtension;

/**
 * this plugin gives basic statistic number to have a general understanding of software event log. 
 * @author cliu3
 */
@Plugin(
		name = "Software Event Log Basic Statistics",// plugin name
		
		returnLabels = {"Basic Statistics"}, //return labels
		returnTypes = {String.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Event Log"},
		
		userAccessible = true,
		help = "This plugin provides basic statistic number to have a general understanding of software event log." 
		)
public class SoftwareEventLogStatisticsPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Software Statistics, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	public String statisticsCalculation(UIPluginContext context, XLog softwareEventLog) 
	{			
		return VisualizeSoftwareEventLogStatistics.visualizeSoftwareLog(computeStatistics(softwareEventLog));	
	}
	
	/*
	 * compute the basis statistics of software event log. 
	 */
	public static SoftwareEventLogStatistics computeStatistics(XLog softwareEventLog)
	{
		SoftwareEventLogStatistics ses = new SoftwareEventLogStatistics();
		
		//set the log name
		ses.setLogName(XConceptExtension.instance().extractName(softwareEventLog));
		
		//set the number of executions
		ses.setTraceNumber(softwareEventLog.size());
	
		//get the packages, classes, methods, we consider both the callee and caller part
		HashSet<String> packageSet = new HashSet<>();
		HashSet<String> classSet = new HashSet<>();
		HashSet<String> methodSet = new HashSet<>();
		HashSet<String> mainMethodSet = new HashSet<>();
		int methodCallNum = 0;
		
		for(XTrace t: softwareEventLog)
		{
			methodCallNum=methodCallNum+t.size();
			for(XEvent e: t)
			{
				//the main method: caller method is null
				if(XConceptExtension.instance().extractName(e).equals("main()")
						&&XSoftwareExtension.instance().extractCallermethod(e).equals("null"))
				{
					mainMethodSet.add(XSoftwareExtension.instance().extractPackage(e)+"."+XSoftwareExtension.instance().extractClass(e)+"."+
							XConceptExtension.instance().extractName(e));
				}
				// the pacakge null and class null.null, and method null.null.null are included. 
				packageSet.add(XSoftwareExtension.instance().extractPackage(e));//callee
				packageSet.add(XSoftwareExtension.instance().extractCallerpackage(e));//callee
				classSet.add(XSoftwareExtension.instance().extractPackage(e)+"."+XSoftwareExtension.instance().extractClass(e));
				classSet.add(XSoftwareExtension.instance().extractCallerpackage(e)+"."+XSoftwareExtension.instance().extractCallerclass(e));
				methodSet.add(XSoftwareExtension.instance().extractPackage(e)+"."+XSoftwareExtension.instance().extractClass(e)+"."+XConceptExtension.instance().extractName(e));
				methodSet.add(XSoftwareExtension.instance().extractCallerpackage(e)+"."+XSoftwareExtension.instance().extractCallerclass(e)+"."+XSoftwareExtension.instance().extractCallermethod(e));

			}
		}
		
		//set the number of method calls
		ses.setEventNumber(methodCallNum);
		
		//set the average number of method calls per execution
		double averageMethodCallPerTrace = methodCallNum/(double) softwareEventLog.size();
		ses.setAverageEventNumber(averageMethodCallPerTrace);
		
		//set the number of packages, and packages
		if(packageSet.contains("null")|packageSet.contains("NULL"))// the null/NULL should not be recognized as a package
		{
			packageSet.remove("null");
			packageSet.remove("NULL");
		}
//		else if(packageSet.contains("NULL"))
//		{
//			packageSet.remove("NULL");
//		}
		
		ses.setPackageNumber(packageSet.size());
		ses.setPackageSet(packageSet);
		
		//set the number of classes, and classes
		if(classSet.contains("null.null")|classSet.contains("NULL.NULL"))
		{
			classSet.remove("null.null");
			classSet.remove("NULL.NULL");
		}
//		else if(classSet.contains("NULL.NULL"))
//		{
//			classSet.remove("NULL.NULL");
//		}
		
		ses.setClassNumber(classSet.size());
		ses.setClassSet(classSet);
		
		//set the number of methods
		if(methodSet.contains("null.null.null")|methodSet.contains("NULL.NULL.NULL"))
		{
			methodSet.remove("null.null.null");
			methodSet.remove("NULL.NULL.NULL");
		}
//		else if(methodSet.contains("NULL.NULL.NULL"))
//		{
//			methodSet.remove("NULL.NULL.NULL");
//		}
		
		ses.setMethodNumber(methodSet.size());
		ses.setMethodSet(methodSet);
		
		//set the main method
		ses.setMainSet(mainMethodSet);
		
		return ses;
	}
}
