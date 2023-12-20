package DelftLogTransformation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(
		name = "XES Log Transformation Plugin for Automaton Learning (.csv)",// plugin name
		returnLabels = {},
		returnTypes = {},
		parameterLabels = {"Exported Log","file" },
		userAccessible = true
	)
@UIExportPlugin(
		description = "Export XES Log for Automaton Learning (.csv)",//show the type in window for recording files
		extension = "csv" // the suffix name
	)
public class TransformXESLogForAutomatonLearning {
	@PluginVariant(
			variantLabel = "Export XES Log for Automaton Learning (.csv)",
			requiredParameterLabels = {0, 1}// the input has two para, one the the log object and one for the file.
		) 
public void exportComponent2Classes(PluginContext context, XLog log, File file) throws Exception {
		
		context.log("XES Log for Automaton Learning Export Starts..."); 
		XLogInfo summary = XLogInfoFactory.createLogInfo(log);

		
		FileOutputStream fos = new FileOutputStream(file);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		
		//get the number of trace of the log. 
		int numberOfTrace =summary.getNumberOfTraces();
		
		//get the number of the alphabet
		HashSet<String> eventTypeSet =new HashSet<String>();
		for(XTrace trace: log)
		{
			for (XEvent event: trace)
			{
				eventTypeSet.add(XConceptExtension.instance().extractName(event));
			}
		}
		
		bw.write(numberOfTrace+" "+eventTypeSet.size());
		bw.newLine();
		
		for(XTrace trace: log)
		{
			//the lenth of the trace
			
			StringBuffer sequence =new StringBuffer();
			sequence.append(trace.size()+" ");
			for (XEvent event: trace)
			{
				sequence.append(XConceptExtension.instance().extractName(event)+",");
			}
			//remove the last , from the string buffer
			sequence.deleteCharAt(sequence.lastIndexOf(","));
			bw.write("1 "+sequence);
			bw.newLine();
		}
		
		bw.close();
		context.log("XES Log for Automaton Learning Export Completes!");
	} 
}
