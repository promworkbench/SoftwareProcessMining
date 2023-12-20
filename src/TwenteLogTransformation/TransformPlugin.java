package TwenteLogTransformation;
/*
 * specially for twente data set that are used in the EuroVis 2018 paper
 * D:\EventLogs\Twente dataset
 */
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.csv.CSVFile;

import UtilityFunctions.OrderingEventsNano;
import openXESsoftwareextension.XSoftwareExtension;

@Plugin(
		name = "CSV Log Transform Plugin",// plugin name
		
		returnLabels = {"XES Log"}, //return labels
		returnTypes = {XLog.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"csv file"},
		
		userAccessible = true,
		help = "This plugin aims to transform a csv file to xes." 
		)
public class TransformPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl;liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "CSV Log Transform, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	public XLog architectureDiscovery(UIPluginContext context, CSVFile csvfile) throws FileNotFoundException, IOException  
	{
		XFactory factory = new XFactoryNaiveImpl();
		XLog lifecycleLog = initializeSoftwareLifecycleLog(factory, "");
		XTrace trace = factory.createTrace();
		
		
		//read the csv file line by line. get the input stream of the csv file
		BufferedReader in = new BufferedReader(new InputStreamReader(csvfile.getInputStream()));
		String line = null;
		while((line = in.readLine()) != null) {
		    //create event 
			trace.add(createEvent(line,factory));
		}
		
		XConceptExtension.instance().assignName(trace, "run1");

		//order the trace by 
		OrderingEventsNano.orderEventLogwithTimestamp(trace, XSoftwareExtension.KEY_STARTTIMENANO);
		lifecycleLog.add(trace);
		return lifecycleLog;
	}
	
	public static XEvent createEvent(String line, XFactory factory)
	{
		String [] frag = line.split(",");
		//frag[0] is the timestamp in nano
		//frag[1] is thread
		//frag[2] name and lifecycle
		String [] frag2 = frag[2].split("_");
		//frag2[0] is the name
		//frag2[1] is the lifecycle
				
		StringBuffer functionName = new StringBuffer(frag2[0]);
		for(int i=1;i<=frag2.length-2;i++)
		{
			functionName.append("_");
			functionName.append(frag2[i]);
		}
		
		XEvent event = factory.createEvent();
		
		XConceptExtension.instance().assignName(event, functionName.toString());
		XSoftwareExtension.instance().assignStarttimenano(event, frag[0]);

		
		XSoftwareExtension.instance().assignThreadID(event, Integer.parseInt(frag[1]));
		
		if(frag2[frag2.length-1].equals("START"))
		{
			XLifecycleExtension.instance().assignTransition(event, "Start");
		}
		else{
			XLifecycleExtension.instance().assignTransition(event, "Complete");
		}
		return event;
	}
	
	// initialize main software event log
	public static XLog initializeSoftwareLifecycleLog(XFactory factory, String logName)
	{
		//add the log name		
		XLog lifecycleLog = factory.createLog();
		XConceptExtension.instance().assignName(lifecycleLog, logName);
		//log.getAttributes().put(XConceptExtension.KEY_NAME, new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, logName));
		
		//create standard extension
		XExtension conceptExtension = XConceptExtension.instance();
		//XExtension organizationalExtension = XOrganizationalExtension.instance();
		//XExtension timeExtension = XTimeExtension.instance();
		XExtension lifecycleExtension=XLifecycleExtension.instance();
		XExtension softwareExtension=XSoftwareExtension.instance();
		
		// create extensions
		lifecycleLog.getExtensions().add(conceptExtension);
		lifecycleLog.getExtensions().add(softwareExtension);
		lifecycleLog.getExtensions().add(lifecycleExtension);
		//log.getExtensions().add(timeExtension); 
		
		// create trace level global attributes
		XAttribute xtrace = new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, "DEFAULT"); 
		lifecycleLog.getGlobalTraceAttributes().add(xtrace);

		// create event level global attributes		
		lifecycleLog.getGlobalEventAttributes().add(XConceptExtension.ATTR_NAME);
		lifecycleLog.getGlobalEventAttributes().add(XSoftwareExtension.ATTR_STARTTIMENANO);
		lifecycleLog.getGlobalEventAttributes().add(XLifecycleExtension.ATTR_TRANSITION);
		
		
		// create classifiers based on global attribute		
		XEventAttributeClassifier methodCallClassifer = new XEventAttributeClassifier("Function Identifier", 
				 XConceptExtension.KEY_NAME, XLifecycleExtension.KEY_TRANSITION);
		//log.getClassifiers().add(classifierActivityObject);
		lifecycleLog.getClassifiers().add(methodCallClassifer);
		
		return lifecycleLog;
	}
}
