package software.processmining.MethodCallingGraphDiscovery;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import software.processmining.multiviewinteraction.MethodCallConnectivity;
import software.processmining.multiviewinteraction.MethodCallingGraph;

@Plugin(
		name = "Method Calling Graph Discovery",// plugin name
		
		returnLabels = {"Software Component Interaction Behavior"}, //return labels
		returnTypes = {MethodCallingGraph.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Event Log"},
		
		userAccessible = true,
		help = "This plugin aims to discover the method calling graph." 
		)
public class MethodCallingGraphDiscoveryPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Discovering method calling graph, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	public MethodCallingGraph methodCallingDiscovery(UIPluginContext context, XLog originalLog)
	{
		//get the log name from the original log. it is shown as the title of returned results. 
		context.getFutureResult(0).setLabel("Method Calling Graph Discovery");
		
		MethodCallingGraph mcg = MethodCallConnectivity.methodCallGraph(originalLog);
		return mcg;
	}
}
