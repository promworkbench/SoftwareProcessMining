package software.processmining.interfacediscoveryevaluation;

import java.util.HashSet;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;

import UtilityClasses.ClassClass;
import UtilityClasses.ComponentConfig;

/**
 * this plugin aims to provide a set of approaches to discovery interfaces:
 * Input 1: a software event log (original data, each trace refers to one software execution);
 * Input 2: configuration file indicating the mapping from component to classes, with suffix .conf.
 * 
 * Output: a set of components, each with a set of interface descriptions.
 * 
 * Approach1: each component has only one interface which includes all methods. 
 * Approach2: each interface includes only one method. 
 * Approach3: clustering methods of each component by its belonging class. 
 * Approach4: clustering methods of each component by its caller component. 
 * Approach5: clustering methods of each component by its caller method. 
 * 
 * @author cliu3 2017-4-13
 *
**/

@Plugin(
		name = "Integrated Software Interface Discovery Tool",// plugin name
		
		returnLabels = {"Software Description"}, //return labels
		returnTypes = {SoftwareDescription.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Event Log", "Component Configuration"},
		
		userAccessible = true,
		help = "This plugin aims to provide a set of approaches to discovery interfaces." 
		)
public class IntegratedInterfaceDiscoveryPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl;liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Discovering interface, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0, 1}
			)
	
	public SoftwareDescription interactionDiscovery(UIPluginContext context, XLog originalLog, ComponentConfig comconfig) throws NumberFormatException, UserCancelledException
	{
		SoftwareDescription softwareDescription = new SoftwareDescription();
		
		//repair the component configuration to include "Null"-->null.null. 
		//this will help to remove the case that the caller component of some interface do not exist. 
		ClassClass tempC = new ClassClass();
		tempC.setClassName("null");
		tempC.setPackageName("null");
		HashSet<ClassClass> classSet = new HashSet<>();
		classSet.add(tempC);
		comconfig.add("NULLCOM", classSet);
		
		//select the approach for discovering interfaces. 
		String [] discoveryApproach = new String[5];
		discoveryApproach[0]="Naive Interface Discovery (all methods for an interface)";
		discoveryApproach[1]="Naive Interface Discovery (single method per interface)";
		discoveryApproach[2]="Clustering Methods By Belonging Class";
		discoveryApproach[3]="Clustering Methods By Calling Component";
		discoveryApproach[4]="Clustering Methods By Calling Method";
		String selectedDiscoveryApproach = ProMUIHelper.queryForObject(context, "Select one approach for discovering interfaces", discoveryApproach);
		context.log("The selected approach to disocvery interface is: "+discoveryApproach, MessageLevel.NORMAL);	
		System.out.println("The selected approach to disocvery interface is: "+selectedDiscoveryApproach);
						
		
		//create factory to create Xlog, Xtrace and Xevent.
		XFactory factory = new XFactoryNaiveImpl();
		InterfaceDiscoveryNaive idn = new InterfaceDiscoveryNaive();
		switch (selectedDiscoveryApproach) {
	         case "Naive Interface Discovery (all methods for an interface)"://Naive Interface Discovery Approach
	        	 softwareDescription = idn.naiveDiscoverySingleInterface(originalLog, comconfig, factory);
	        	 break;
	         case "Naive Interface Discovery (single method per interface)"://Naive Interface Discovery Approach
	        	 softwareDescription = idn.naiveDiscoverySingleMethodPerInterface(originalLog, comconfig, factory);
	        	 break;
	         case "Clustering Methods By Belonging Class"://Discover interface based belonging class
	        	 InterfaceDiscoveryUsingBelongingClass idbc = new InterfaceDiscoveryUsingBelongingClass();
	        	 softwareDescription = idbc.belongingClassBasedInterfaceDiscovery(originalLog, comconfig, factory);
	             break;
	         case "Clustering Methods By Calling Component"://Discover interface based calling component	        	 
	        	 InterfaceDiscoveryUsingCallingComponent idcc = new InterfaceDiscoveryUsingCallingComponent();
	        	 softwareDescription = idcc.callingComponentBasedInterfaceDiscovery(originalLog, comconfig, factory);
	            break;
	            
	         case "Clustering Methods By Calling Method"://Discover interface based calling method	 
	        	 InterfaceDiscoveryUsingCallingMethod idcm = new InterfaceDiscoveryUsingCallingMethod();
	        	 softwareDescription = idcm.callingMethodBasedInterfaceDiscovery(originalLog, comconfig, factory);
	        		
	        	break;
	         default:
	            throw new IllegalArgumentException("Invalid discovery approach: " + selectedDiscoveryApproach);
		}
		
		System.out.println("the number of components: "+ softwareDescription.getNumberofComponent());
		return softwareDescription;
	}
		
}
