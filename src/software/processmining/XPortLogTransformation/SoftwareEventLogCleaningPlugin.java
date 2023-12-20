package software.processmining.XPortLogTransformation;
/*
 * By taking a software event log as input, this plugin aim to clear the anonymous class and method. 
 * 
 */

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import openXESsoftwareextension.XSoftwareExtension;

@Plugin(
		name = "Software Event Log Cleaning (Handling Anonymous Classes)",// plugin name
		
		returnLabels = {"A Software Event Log"}, //return labels
		returnTypes = {XLog.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Event Log"},
		
		userAccessible = true,
		help = "This plugin aims to pre-process software event log collected by Maikel Leemans XPort instrumentation." 
		)
public class SoftwareEventLogCleaningPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Software Event Log Cleaning (Handling Anonymous Classes)",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	
	public static XLog preprocessing(UIPluginContext context, XLog originalLog)
	{
		String regex = "[0-9]+";
		//String regex = "\\d+";
		
		for(XTrace trace: originalLog)
		{
			for(XEvent event: trace)
			{
				if(XSoftwareExtension.instance().extractClass(event).contains("$"))
				{
					String temp = XSoftwareExtension.instance().extractClass(event);
					String arr[] =temp.split("\\$");
					if(arr[arr.length-1].matches(regex))//only digit in the last part. 
					{
						XSoftwareExtension.instance().assignClass(event, arr[arr.length-2]);
					}
					else{
						XSoftwareExtension.instance().assignClass(event, arr[arr.length-1]);
					}
				}
				
				if(XSoftwareExtension.instance().extractCallerclass(event).contains("$"))
				{
					String temp = XSoftwareExtension.instance().extractCallerclass(event);
					String arr[] =temp.split("\\$");
					if(arr[arr.length-1].matches(regex))//only digit in the last part. 
					{
						XSoftwareExtension.instance().assignCallerclass(event, arr[arr.length-2]);
					}
					else{
						XSoftwareExtension.instance().assignCallerclass(event, arr[arr.length-1]);
					}
				}
			}
		}

		return originalLog;
	}
	
}
