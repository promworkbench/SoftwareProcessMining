package software.processmining.interfacediscoveryevaluation;
/*
 * this plugin aims to discovery hierarchical interface behavioral models
 * Input1: SoftwareDescription
 * Input2: component Configuration
 * Input3: event log. 
 * Output: interface behavioral model
 * 
 * 
 * New feature, let the user to select the component that he wants to discover its interface behavior model. 
 * 
 */

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;

import UtilityClasses.ClassClass;
import UtilityClasses.ComponentConfig;
import software.processmining.componentbehaviordiscovery.ConstructSoftwareComponentLog;
import software.processmining.componentbehaviordiscovery.HSoftwareEventLog;
import software.processmining.componentbehaviordiscovery.HierarchicalPetriNet;
import software.processmining.componentbehaviordiscovery.HierarchicalSoftwareEventLogConstruction;
import software.processmining.componentbehaviordiscovery.MineHierarchicalPetriNet;
import software.processmining.interfacediscovery.ComponentModels;
import software.processmining.interfacediscovery.ComponentModelsSet;
import software.processmining.interfacediscovery.ConstructSoftwareEventLog;
import software.processmining.interfacediscovery.Interface;
import software.processmining.interfacediscovery.Interface2HPN;

@Plugin(
		name = "Interface Behavioral Model Discovery (Hierarchical Petri Net)",// plugin name
		
		returnLabels = {"Interface Behavioral Models"}, //return labels
		returnTypes = {ComponentModelsSet.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Description", "Software Event Log", "Component Configuration"},
		
		userAccessible = true,
		help = "This plugin aims to discover the interface behavioral models." 
		)
public class InterfaceBehavioralModelDiscovery {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Discovering interface behavior models, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0, 1, 2}
			)
	
	public ComponentModelsSet interfaceBehaviorModelDiscovery(UIPluginContext context, SoftwareDescription softwareDescription, XLog originalLog, 
			ComponentConfig comconfig) throws NumberFormatException, UserCancelledException, ConnectionCannotBeObtained 
	{
		//the component model 
		ComponentModelsSet componentModelSet = new ComponentModelsSet();
				
		//set the inductive miner parameters, the original log is used to set the classifier
		IMMiningDialog dialog = new IMMiningDialog(originalLog);
		InteractionResult result = context.showWizard("Configure Parameters for Inductive Miner (used for all interface models)", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		// the mining parameters are set here 
		MiningParameters IMparameters = dialog.getMiningParameters(); //IMparameters.getClassifier()
		
		//select the maximal nesting depth for the discovered hierarchical petri net. 
		String [] depth = new String[6];
		depth[0]="1";
		depth[1]="2";
		depth[2]="3";
		depth[3]="4";
		depth[4]="5";
		depth[5]="6";
		int maximalNestingDepth =Integer.parseInt(ProMUIHelper.queryForObject(context, "Select the maximal nesting depth", depth));
		context.log("Maximal Interface Behaioval Model Depth is: "+maximalNestingDepth, MessageLevel.NORMAL);	
		System.out.println("Selected nesting depth: "+maximalNestingDepth);
			
		//select the component to discover its interface behavior model. 
		String [] components = new String[softwareDescription.getComponentSet().size()];
		int i =0;
		for(ComponentDescription componentDescription: softwareDescription.getComponentSet())
		{
			components[i]=componentDescription.getComponentName();
			i++;
		}
		List<String> selectedComponents = ProMUIHelper.queryForStrings(context, "Select the components", components);
				
		//create factory to create Xlog, Xtrace and Xevent.
		XFactory factory = new XFactoryNaiveImpl();
		XLogInfo Xloginfo = XLogInfoFactory.createLogInfo(originalLog, IMparameters.getClassifier());
		
		int num=0;
		for(ComponentDescription componentDescription: softwareDescription.getComponentSet())
		{
			if(!selectedComponents.contains(componentDescription.getComponentName()))
			{
				continue;
			}
			// create class set of the current component
			Set<String> classSet = new HashSet<>();
			for(ClassClass c: comconfig.getClasses(componentDescription.getComponentName()))
			{
				classSet.add(c.getPackageName()+"."+c.getClassName());// both the package and class names are used to describe each class
			}
						
			ComponentModels componenModels = new ComponentModels();//create component model
			componenModels.setComponent(componentDescription.getComponentName());// set component name
			
			HashSet<Interface2HPN> i2hpnSet = new HashSet<Interface2HPN>();//for each component, it has a set of <interfaces->interface model>

			//obtain the software event log for each component.we have the instance identification at the component level. 
			// input:(1) class set of the current component; and (2) the original software log
//			XLog comLog = ConstructSoftwareEventLog.generatingComponentEventLog(componentDescription.getComponentName(), classSet, originalLog, factory);
			XLog comLog =ConstructSoftwareComponentLog.generatingComponentSoftwareEventLog(componentDescription.getComponentName(), classSet, originalLog, factory);

			//for each component, we construct the component model 
			for(InterfaceDescription id: componentDescription.getInterfaceSet())// add values
			{
				//get the event log of each interface, when construct log we need also take the caller methods as input.
				XLog interfaceLog = ConstructSoftwareEventLog.constructInterfaceLog(comLog, maximalNestingDepth, id.getMethodSet(), 
						id.getCallerMethodSet(), factory);
				
//				//refactoring the event log by identifying interface instances
//				XLog interfaceInstanceLog = ConstructSoftwareEventLog.constructInterfaceInstanceLog(interfaceLog, factory, classSet);
						
				// hierarchical software event log, null means start from Top-level
				HSoftwareEventLog hseLog = HierarchicalSoftwareEventLogConstruction.constructHierarchicalLog(maximalNestingDepth, factory, classSet, 
						interfaceLog, Xloginfo, componentDescription.getComponentName());
				
				//hierarchical mining, HPN
				HierarchicalPetriNet hpn = MineHierarchicalPetriNet.mineHierarchicalPetriNet(context, hseLog, IMparameters);
				
				Interface in= new Interface("", componentDescription.getComponentName());
				in.setId(componentDescription.getComponentName()+(num++));
				Interface2HPN i2hpn = new Interface2HPN(in, hpn);
				i2hpnSet.add(i2hpn);
			}
			
			componenModels.setI2hpn(i2hpnSet);// the interface part of the component
			componentModelSet.addComponent2HPNSet(componenModels);
		}
		
		return componentModelSet;
	}
}
