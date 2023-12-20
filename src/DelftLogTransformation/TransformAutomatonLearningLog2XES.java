package DelftLogTransformation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
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
		name = "Automaton Learning Log Transformation Plugin to XES Log ",// plugin name
		returnLabels = {"XES Log"}, //return labels
		returnTypes = {XLog.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"csv file"},
		
		userAccessible = true,
		help = "This plugin aims to transform a csv file to xes." 
	)
public class TransformAutomatonLearningLog2XES {
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
		int runCount=0;
		while((line = in.readLine()) != null) {
			if(runCount!=0)// we do not consider the first line as trace
			{
				String [] frag = line.split(" ");
				//create trace for each line
				XTrace trace = factory.createTrace();
				XConceptExtension.instance().assignName(trace, "Run"+runCount);
				
				for(int i=1;i<frag.length;i++)// event starts from the second one. 
				{
					XEvent event = factory.createEvent();
					XConceptExtension.instance().assignName(event, frag[i]);//event name
					
					XLifecycleExtension.instance().assignTransition(event, "Complete");
					trace.add(event);
				}
				
				lifecycleLog.add(trace);
			}
			
			runCount++;
		}
		
		return lifecycleLog;
	}
	
}
