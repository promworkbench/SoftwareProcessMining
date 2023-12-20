package software.processmining.interaction;

import java.util.HashSet;
import java.util.Set;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import UtilityClasses.ClassClass;
import UtilityClasses.ComponentConfig;

/*
 * this plugin aims to check if the classes included in different components overlap.
 * Our work requires that different components do not share classes.   
 */
@Plugin(
		name = "Overlap Checking of Component Configuration",// plugin name
		
		returnLabels = {"Overlapping Report"}, //return labels
		returnTypes = {String.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Component Configuration"},
		
		userAccessible = true,
		help = "This plugin aims to check whether different components share classes or not." 
		)
public class CheckOverlappingofComponentConfigurationPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Check overlap, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	public String overlapChecking(UIPluginContext context, ComponentConfig comconfig) 
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("<html>"); 
		buffer.append("<body>");
		
		buffer.append("<h1 style=\"color:blue;\">"+"Class Overlapping among Different Components: "+ "</h1>"); //style=\"color:blue;\"
		for(String com1: comconfig.getAllComponents())
		{
			buffer.append("<h2 style=\"color:#2F8AA1;\">"+"Compoennt: "+com1+"</h2>");

			for(String com2: comconfig.getAllComponents())
			{
				if(!com1.equals(com2))
				{
					//check if the class set of com1 share elements with com2
					Set<ClassClass> sharedClasses=getSharedClasses(comconfig.getClasses(com1), comconfig.getClasses(com2));
					if(sharedClasses.size()>0)
					{
						buffer.append("<h3 style=\"color:#2F8AA1;\">"+"Shared with Compoennt: "+com2+"</h3>");

						//create a new table
						buffer.append("<table bgcolor=\"#ABE8E0\" align=\"left\">");
						for(ClassClass c:sharedClasses)
						{
							buffer.append("<tr><td style=\"font-size:120%;\">"+c+"</td></tr>");
						}
						buffer.append("</table>");
					}
				}
			}
		}
		
		
		buffer.append("</body>");
		buffer.append("</html>");
		return buffer.toString();
	}
	
	public static Set<ClassClass> getSharedClasses(Set<ClassClass> set1, Set<ClassClass> set2)
	{
		Set<ClassClass> clonedSet1 = new HashSet<>(set1);
		Set<ClassClass> clonedSet2 = new HashSet<>(set2);
		//get the shared elements.
		clonedSet1.retainAll(clonedSet2);
		
		return clonedSet1;
	}
}
