package BasicStatisticsSoftwareEventLog;

import java.util.HashSet;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import UtilityClasses.ClassClass;
import UtilityClasses.ComponentConfig;

/**
 * this plugin takes a software event log as input and returns the component configuration 
 * such that classes included in each package forms a component. 
 * 
 * Note: for those classes that only occur in the caller part of a method call, we also create a component for them. 
 * This will avoid the case that some of the discovered interface do not have caller component when the caller class is not included in the component configuration. 
 * @author cliu3
 *
 */
@Plugin(
		name = "Construct Component Configuration Based on Package",// plugin name
		
		returnLabels = {"Component Configuration"}, //return labels
		returnTypes = {ComponentConfig.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Event Log"},
		
		userAccessible = true,
		help = "This plugin construct component configuration based on Package." 
		)
public class ConstructComponentByPackage {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Software Statistics, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	public ComponentConfig constructComponentConfiguration(UIPluginContext context, XLog softwareEventLog) 
	{
		ComponentConfig cc = new ComponentConfig();
		
		SoftwareEventLogStatistics sc = SoftwareEventLogStatisticsPlugin.computeStatistics(softwareEventLog);

		int count =0;
		//for each package
		for(String p: sc.getPackageSet())
		{
			count++;
			//create its class set
			HashSet<ClassClass> classSet = new HashSet<>();
			for(String c: sc.getClassSet())
			{
				if(VisualizeSoftwareEventLogStatistics.getPackageFromClass(c).equals(p))
				{
					ClassClass tempC = new ClassClass();
					tempC.setPackageName(p);
					tempC.setClassName(VisualizeSoftwareEventLogStatistics.getClassFromClass(c));
					classSet.add(tempC);
				}
			}
			cc.add("com"+count, classSet);
		}
		
		cc.removeComponent("NULLCOM");
		
		return cc;
	}
}
