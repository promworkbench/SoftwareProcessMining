package software.processmining.interaction;
/*
 * this plugin aims to get the top-level(interface) methods for each component. 
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
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
import software.processmining.interfacediscovery.ConstructSoftwareEventLog;

@Plugin(
		name = "Top-level Methods Per Component",// plugin name
		
		returnLabels = {"Top-level Method Report"}, //return labels
		returnTypes = {String.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Event Log", "Component Configuration"},
		
		userAccessible = true,
		help = "This plugin aims to get the top level methods for each component." 
		)
public class GetToplLevelMethodForComponentPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "get top level methods per component, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0, 1}
			)
	
	public String toplevelMethodPerComponent(UIPluginContext context, XLog softwareEventLog, ComponentConfig comconfig) 
	{
		System.out.println(comconfig.getAllComponents());
		
		//create factory to create Xlog, Xtrace and Xevent.
		XFactory factory = new XFactoryNaiveImpl();
		
		//transform the format of component configuration file
		HashMap<String, HashSet<String>> component2ClassSet = new HashMap<>();
		for(String com: comconfig.getAllComponents())
		{
			HashSet<String> classSet = new HashSet<>();//class set of the current component
			for(ClassClass c: comconfig.getClasses(com))
			{
				classSet.add(c.getPackageName()+"."+c.getClassName());// both the package and class names are used to describe each class
			}
			component2ClassSet.put(com, classSet);
		}
		
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("<html>"); 
		buffer.append("<body>");
		
		buffer.append("<h1 style=\"color:blue;\">"+"Top Level Methods Per Component"+"</h1>"); //style=\"color:blue;\"
		
		
		for(String component: comconfig.getAllComponents())
		{
			buffer.append("<h2 style=\"color:#2F8AA1;\">"+"Top Level Method of: "+ component +"</h2>");
			
			//the top level method of current component. 
			HashSet<String> toplevelMethods = getTopLevelEventPerComponent(ConstructSoftwareEventLog.generatingComponentEventLog(component, component2ClassSet.get(component), softwareEventLog, factory), component2ClassSet.get(component));
		
			//the first table of general statistics
			buffer.append("<table bgcolor=\"#ABE8E0\" align=\"left\">");
			for(String methodName:toplevelMethods)
			{
				buffer.append("<tr><td style=\"font-size:120%;\">"+methodName+"</td></tr>");
			}
			buffer.append("</tr></table>");
			
		}

		buffer.append("</table>");
		buffer.append("</body>");
		buffer.append("</html>");
		return buffer.toString();
	}
	
	
	//this method tries to get the top level method of each component, i.e., the methods whose caller do not belong to the current component. 
	public static HashSet<String> getTopLevelEventPerComponent(XLog componentLog, Set<String> classSet)
	{
		HashSet<String> topLevelMethods = new HashSet<>();
		
		if(componentLog.size()>0)//the current component should contains trace. 
		{
			for(XTrace trace: componentLog)
			{
				for(XEvent event: trace)
				{
					if(!classSet.contains(XSoftwareExtension.instance().extractCallerpackage(event)+"."+XSoftwareExtension.instance().extractCallerclass(event)))
					{
						topLevelMethods.add(XSoftwareExtension.instance().extractPackage(event)+"."+
								XSoftwareExtension.instance().extractClass(event)+"."+XConceptExtension.instance().extractName(event));
					}
				}
			}
		}
				
		return topLevelMethods;
	}
}
