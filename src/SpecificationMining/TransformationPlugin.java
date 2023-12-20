package SpecificationMining;
/*
 * this plug-in transforms the csv log used for Deep Specification Minig to XES. 
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

@Plugin(
		name = "CSV Log Transform Plugin for Software Speficication Mining Data",// plugin name
		
		returnLabels = {"XES Log"}, //return labels
		returnTypes = {XLog.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"csv file"},
		
		userAccessible = true,
		help = "This plugin aims to transform a csv file (software execution traces) to xes." 
		)
public class TransformationPlugin {
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
	
	public XLog createXESLog(UIPluginContext context, CSVFile csvfile) throws FileNotFoundException, IOException  
	{
		XFactory factory = new XFactoryNaiveImpl();
		XLog xesLog = initializeSoftwareEventLog(factory, "");
		
		
		
		//read the csv file line by line. get the input stream of the csv file
		BufferedReader in = new BufferedReader(new InputStreamReader(csvfile.getInputStream()));
		String line = null;
		int runCount=1;
		while((line = in.readLine()) != null) {
			//create trace for each line
			xesLog.add(createTrace(line,factory,runCount));
			runCount++;
		}
		
		return xesLog;
	}
	
	
	public static XTrace createTrace(String line, XFactory factory, int runCount)
	{
		//create a trace for each line
		XTrace trace = factory.createTrace();
		XConceptExtension.instance().assignName(trace,"Case"+runCount);

		//each event is separate by space
		String [] frag = line.split(" ");
		//each item refers to an event (Start or  Complete)
		for(int i=0;i<frag.length;i++)
		{
			XEvent event = factory.createEvent();
			if(i==0||i==frag.length-1)//for the first event and last event. 
			{
				XConceptExtension.instance().assignName(event, frag[i].replaceAll("<", "").replaceAll(">", ""));//event name
				XLifecycleExtension.instance().assignTransition(event, "Complete");	
			}
			else{
				XConceptExtension.instance().assignName(event, frag[i]);//event name
				XLifecycleExtension.instance().assignTransition(event, "Complete");	
			}

			trace.add(event);
		}
		
		return trace;
	}
	
	// initialize main software event loglog
	public static XLog initializeSoftwareEventLog(XFactory factory, String logName)
	{
		//add the log name		
		XLog xesLog = factory.createLog();
		XConceptExtension.instance().assignName(xesLog, logName);
		//log.getAttributes().put(XConceptExtension.KEY_NAME, new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, logName));
		
		//create standard extension
		XExtension conceptExtension = XConceptExtension.instance();
		//XExtension organizationalExtension = XOrganizationalExtension.instance();
		//XExtension timeExtension = XTimeExtension.instance();
		XExtension lifecycleExtension=XLifecycleExtension.instance();
		//XExtension softwareExtension=XSoftwareExtension.instance();
		
		// create extensions
		xesLog.getExtensions().add(conceptExtension);
		//lifecycleLog.getExtensions().add(softwareExtension);
		xesLog.getExtensions().add(lifecycleExtension);
		//log.getExtensions().add(timeExtension); 
		
		// create trace level global attributes
		XAttribute xtrace = new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, "DEFAULT"); 
		xesLog.getGlobalTraceAttributes().add(xtrace);

		// create event level global attributes		
		xesLog.getGlobalEventAttributes().add(XConceptExtension.ATTR_NAME);
		//lifecycleLog.getGlobalEventAttributes().add(XSoftwareExtension.ATTR_STARTTIMENANO);
		xesLog.getGlobalEventAttributes().add(XLifecycleExtension.ATTR_TRANSITION);
		
		
		// create classifiers based on global attribute		
		XEventAttributeClassifier methodNameClassifer = new XEventAttributeClassifier("Method Name", 
				 XConceptExtension.KEY_NAME);
		//log.getClassifiers().add(classifierActivityObject);
		xesLog.getClassifiers().add(methodNameClassifer);
		
		return xesLog;
	}

}
