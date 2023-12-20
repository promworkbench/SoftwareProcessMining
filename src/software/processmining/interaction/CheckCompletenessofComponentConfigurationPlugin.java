package software.processmining.interaction;
/*
 * this plugin aims to check if the input component configuration contains all classes executed in the log. 
 */

import java.util.HashSet;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import UtilityClasses.ClassClass;
import UtilityClasses.ComponentConfig;
import openXESsoftwareextension.XSoftwareExtension;

@Plugin(
		name = "Component Configuration Completeness Checking",// plugin name
		
		returnLabels = {"Completeness Report"}, //return labels
		returnTypes = {String.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Event Log", "Component Configuration"},
		
		userAccessible = true,
		help = "This plugin aims to check if all classes in the software log are included in the component configuration." 
		)
public class CheckCompletenessofComponentConfigurationPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Check completeness, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0, 1}
			)
	
	public String completenessChecking(UIPluginContext context, XLog softwareEventLog, ComponentConfig comconfig) 
	{
		//get the class set of the componentConfiguration
		HashSet<String> classSetConfiguration = new HashSet<>();
		for(String component: comconfig.getAllComponents())
		{
			for(ClassClass c: comconfig.getClasses(component))
			{
				classSetConfiguration.add(c.toString());
			}
		}
				
		//get the class set that are included in the log but not in the configuration
		HashSet<String> classSetInLogNotInConfig = new HashSet<>();
		for(XTrace t: softwareEventLog)
		{
			for(XEvent e: t)
			{
				String tempClass = XSoftwareExtension.instance().extractPackage(e)+"."+XSoftwareExtension.instance().extractClass(e);
				if(!classSetConfiguration.contains(tempClass))
				{
					classSetInLogNotInConfig.add(tempClass);
				}		
			}
		}
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("<html>"); 
		buffer.append("<body>");
		
		buffer.append("<h1 style=\"color:blue;\">"+"The Number of Classes Not Included in the Software Event Log: "+ classSetInLogNotInConfig.size()+"</h1>"); //style=\"color:blue;\"
		buffer.append("<h2 style=\"color:#2F8AA1;\">"+"Detailed Class Information</h2>");
		
		//create a new table
		buffer.append("<table bgcolor=\"#ABE8E0\" align=\"left\">");
		
		for(String className:classSetInLogNotInConfig)
		{
			buffer.append("<tr><td style=\"font-size:120%;\">"+className+"</td></tr>");
		}
		
		buffer.append("</table>");
		buffer.append("</body>");
		buffer.append("</html>");
		return buffer.toString();
	}
}
