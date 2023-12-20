package software.processmining.multiviewinteraction;

import java.util.HashMap;
import java.util.HashSet;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

/**
 * this plugin aims to measure the quality of an architectural model based on different interfaces part. 
 * 
 * Metric1: the coupling metrics of each component. 
 * @author cliu3
 *
 */

@Plugin(
		name = "Simple Software Architecture Quality Evaluation",// plugin name
		
		returnLabels = {"String"}, //reture labels
		returnTypes = {String.class},//return class
		
		//input parameter labels
		parameterLabels = {"Software Architecture"},
		
		userAccessible = true,
		help = "This plugin aims to evaluate the quality of a discovered architectural model." 
		)
public class ArchitecturalModelQualityEvaluationPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl;liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Architecture Quality Metrics, default",
			// the number of required parameters, {0} means the first input parameter, {0, 1} means the second input parameter, {0, 1, 2} means the third input parameter
			requiredParameterLabels = {0} 
			)
	public String computeArchitectureMetrics(UIPluginContext context, SoftwareArchitectureModelSimple simpleSoftwareArchitecture)
	{
		//get the interface set of all components 
		HashSet<String> interfaceSet = new HashSet<>();
		for(String component: simpleSoftwareArchitecture.getComponent2Interfaces().keySet())
		{
			interfaceSet.addAll(simpleSoftwareArchitecture.getComponent2Interfaces().get(component));
		}
		
//		//visualize interation
//		for(String callingInterface: simpleArchitectureModel.getInterfaceInteractions().keySet())
//		{
//			System.out.println(callingInterface+" --> "+simpleArchitectureModel.getInterfaceInteractions().get(callingInterface));
//		}
		
		//for each interface, we get all its interacted interfaces
		HashMap<String, HashSet<String>> interface2InteractedInterfaces = new HashMap<>();
		for(String inter: interfaceSet)
		{
			HashSet<String> interactedInterfaces = new HashSet<>();
			
			for(String callerInterface: simpleSoftwareArchitecture.getInterfaceInteractions().keySet())
			{
				//get the callee interfaces
				if(callerInterface.equals(inter))
				{
					interactedInterfaces.addAll(simpleSoftwareArchitecture.getInterfaceInteractions().get(callerInterface));
				}
				
				//get the caller interface
				if(simpleSoftwareArchitecture.getInterfaceInteractions().get(callerInterface).contains(inter))
				{
					interactedInterfaces.add(callerInterface);
				}
			}
			
			interface2InteractedInterfaces.put(inter, interactedInterfaces);
		}
		
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("<html>"); 
		buffer.append("<body");// style=\"background-color:C6FFF8;\">
		buffer.append("<h1 style=\"color:blue;\">"+"Architecture Quality Metrics</h1>"); //
		buffer.append("<table bgcolor=\"#979A9A\">");
		
		buffer.append("<tr><th style=\"color:green;\" style=\"font-size:120%;\">"+" Number of Components"+"</th></tr>");
		buffer.append("<tr><td></td><td style=\"font-size:120%;\">"+"</td><td style=\"font-size:120%;\">"+simpleSoftwareArchitecture.getComponent2Interfaces().size()+"</td></tr>");
		
		buffer.append("<tr><th style=\"color:green;\" style=\"font-size:120%;\">"+" Number of Interfaces"+"</th></tr>");
		buffer.append("<tr><td></td><td style=\"font-size:120%;\">"+"</td><td style=\"font-size:120%;\">"+interfaceSet.size()+"</td></tr>");
		
		buffer.append("<tr><th style=\"color:green;\" style=\"font-size:120%;\">"+" Number of Connectors"+"</th></tr>");
		buffer.append("<tr><td></td><td style=\"font-size:120%;\">"+"</td><td style=\"font-size:120%;\">"+simpleSoftwareArchitecture.getInterfaceInteractions().size()+"</td></tr>");
		
		
		double softwareCoupling =0;
		int componentCount =0;
		//for each component, we compute the coupling. 
		for(String component: simpleSoftwareArchitecture.getComponent2Interfaces().keySet())
		{
			System.out.println("Component name: "+ component);
			double couplingSum = 0;
			for(String inter: simpleSoftwareArchitecture.getComponent2Interfaces().get(component))
			{
				//compute the coupling for each interfaces
				double interCoupling = (double)interface2InteractedInterfaces.get(inter).size()/(interfaceSet.size()-1);
				couplingSum=couplingSum+interCoupling;
			}
			
			double averageCoupling = couplingSum/simpleSoftwareArchitecture.getComponent2Interfaces().get(component).size();
			
			softwareCoupling=softwareCoupling+averageCoupling;
			componentCount++;
			
			buffer.append("<tr><th style=\"color:green;\" style=\"font-size:120%;\">"+component+"</th></tr>");
			buffer.append("<tr><td></td><td style=\"font-size:120%;\">"+"</td><td style=\"font-size:120%;\">"+averageCoupling+"</td></tr>");
		}
		
		buffer.append("<tr><th style=\"color:green;\" style=\"font-size:120%;\">"+" software coupling"+"</th></tr>");
		buffer.append("<tr><td></td><td style=\"font-size:120%;\">"+"</td><td style=\"font-size:120%;\">"+softwareCoupling/componentCount+"</td></tr>");
		
		buffer.append("</table>");
		buffer.append("</body>");
		buffer.append("</html>");
		return buffer.toString();
	}
}
