package DelftLogTransformation;
/*
 * this plug-in transforms Qin's sensor data to XES. 
 * 
 *  Revise @1: add the organization information. 
 */
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.csv.CSVFile;

import TwenteLogTransformation.TransformPlugin;

@Plugin(
		name = "CSV Log Transform Plugin for Delft DataSet",// plugin name
		
		returnLabels = {"XES Log"}, //return labels
		returnTypes = {XLog.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"csv file"},
		
		userAccessible = true,
		help = "This plugin aims to transform a csv file to xes." 
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
		XLog lifecycleLog = TransformPlugin.initializeSoftwareLifecycleLog(factory, "");
		
		
		
		//read the csv file line by line. get the input stream of the csv file
		BufferedReader in = new BufferedReader(new InputStreamReader(csvfile.getInputStream()));
		String line = null;
		int runCount=1;
		while((line = in.readLine()) != null) {
			//create trace for each line
			lifecycleLog.add(createTrace(line,factory,runCount));
			runCount++;
		}
		
		return lifecycleLog;
	}
	
	
	public static XTrace createTrace(String line, XFactory factory, int runCount)
	{
		//create a trace for each line
		XTrace trace = factory.createTrace();
		XConceptExtension.instance().assignName(trace, "Run"+runCount);

		
		String [] frag = line.split(";");
		//each item refers to an event (Start or  Complete)
		for(int i=0;i<frag.length;i++)
		{
			XEvent event = factory.createEvent();
			String [] Eventfrag=frag[i].split(",");
			XConceptExtension.instance().assignName(event, Eventfrag[0]);//event name
			
			if(Eventfrag[1].equals("start"))
			{
				XLifecycleExtension.instance().assignTransition(event, "Start");
			}
			else{
				XLifecycleExtension.instance().assignTransition(event, "Complete");
			}
			
			XOrganizationalExtension.instance().assignResource(event, Eventfrag[2]);
			
			trace.add(event);
		}
		
		return trace;
	}
	

}
