package software.processmining.componentmodelevaluation;

import java.util.HashSet;
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
import nl.tue.astar.AStarException;
import software.processmining.componentbehaviordiscovery.ConstructSoftwareComponentLog;
import software.processmining.componentbehaviordiscovery.ConvertHierarchicalPN2FlatPN;
import software.processmining.componentbehaviordiscovery.ConvertSoftwareEventLog2LogwithLifecycle;
import software.processmining.componentbehaviordiscovery.HSoftwareEventLog;
import software.processmining.componentbehaviordiscovery.HierarchicalPetriNet;
import software.processmining.componentbehaviordiscovery.HierarchicalSoftwareEventLogConstruction;
import software.processmining.componentbehaviordiscovery.MineHierarchicalPetriNet;

/**
 * this plugin aims to measure the quality of the component behavior discovery plugin. 
 * Input: software event log + component config
 * Output: quality metrics (Fitness, Precision, and Recall) for each discovered component models
 * 
 * Step1: create the event log for each component; 
 * Step2: create flat log (without lifecycle)
 * Step3: discovery hpn for each component;
 * Step4: convert hpn to pn;
 * Step5: add lifecycle for the log
 * Step6: measure the quality. 
 * @author cliu3
 *
 */

@Plugin(
		name = "Quality Measure of Component Behavioral Models (All Components)",// plugin name
		
		returnLabels = {"Quality Measure Description"}, //return labels
		returnTypes = {String.class},//return class, a set of component to hpns 
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Event Log", "Component Configuration"},
		
		userAccessible = true,
		help = "This plugin aims to measure the quality of the component behavior discovery plugin." 
		)
public class QualityMeasureComponentModelDiscoveryPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@gmail.com"
	        )
	@PluginVariant(
			variantLabel = "Quality measure of Component Behavior Model, default",
			// the number of required parameters, 0 means the first input parameter, 1 means the second input parameter
			requiredParameterLabels = { 0,1 })
	public String qualityMetricHPN(UIPluginContext context, XLog originalLog, ComponentConfig comConfig) throws NumberFormatException, UserCancelledException, ConnectionCannotBeObtained, AStarException 
	{				
		//set the inductive miner parameters, the original log is used to set the classifier
		IMMiningDialog dialog = new IMMiningDialog(originalLog);
		InteractionResult result = context.showWizard("Configure Parameters for Inductive Miner (used for all component models)", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		
		// the mining parameters are set here 
		MiningParameters IMparameters = dialog.getMiningParameters(); //IMparameters.getClassifier()
	
		//select the maximal nesting depth for the discovered hierarchical petri net. 
		String [] patterns = new String[6];
		patterns[0]="1";
		patterns[1]="2";
		patterns[2]="3";
		patterns[3]="4";
		patterns[4]="5";
		patterns[5]="6";
		int maximalNestingDepth =Integer.parseInt(ProMUIHelper.queryForObject(context, "Select the nesting depth", patterns));
		System.out.println("Selected nesting depth: "+maximalNestingDepth);
		context.log("Maximal Nesting Depth is: "+maximalNestingDepth, MessageLevel.NORMAL);		
		
		//create factory to create XLog, XTrace and XEvent 
		XFactory factory = new XFactoryNaiveImpl();	
		XLogInfo Xloginfo = XLogInfoFactory.createLogInfo(originalLog, IMparameters.getClassifier());

		//measure the quality by taking as input the hpn and flat log. 
		//convert hpn to pn. 
		ConvertHierarchicalPN2FlatPN convertHPN2PN = new ConvertHierarchicalPN2FlatPN();
		
		//add lifecycle information to component log. 
		ConvertSoftwareEventLog2LogwithLifecycle convertLogwithLifecycle = new ConvertSoftwareEventLog2LogwithLifecycle();
		
		//store the component 2 qualitymetrics mapping
		HPNQualityMetricsMap com2quality = new HPNQualityMetricsMap();
		
		//identify component instances, and construct software event log for each component.
		for (String component: comConfig.getAllComponents())
		{
			context.log("Current component name is: "+component, MessageLevel.NORMAL);		
			//obtain the software event log for each component.
			// input:(1) component name, (2) class set of this component; and (2) the original software log
			
			// create class set of the current component
			Set<String> componentClassSet = new HashSet<>();
			for(ClassClass c: comConfig.getClasses(component))
			{
				componentClassSet.add(c.toString());// both the package and class names are used to describe each class
			}
			
			//get the flat log with identifying component/interface instances. 
			XLog  flatComponentLog=ConstructSoftwareComponentLog.generatingComponentSoftwareEventLog(component, componentClassSet, originalLog, factory);
	
			//construct hierarchical software event log. 
			//the construction of N-nesting level hierarchy does not rely on the instance identification. 
			HSoftwareEventLog hseLog = HierarchicalSoftwareEventLogConstruction.constructHierarchicalLog(maximalNestingDepth, factory, componentClassSet, 
					flatComponentLog, Xloginfo, component);
			
			//software component behavior discovery, HPN
			HierarchicalPetriNet hpn = MineHierarchicalPetriNet.mineHierarchicalPetriNet(context, hseLog, IMparameters);
			
			com2quality.addComponentModel(component, QualityMetricsHierarchicalPetriNetPlugin.QualityMeasure(context, convertHPN2PN.convertHPNtoPN(context, hpn), convertLogwithLifecycle.addingLifecycleInformation(context,flatComponentLog)));
									
		}	
		return VisualizeHPNQualityMetricMaps.visualizeQualityMetricsMap(com2quality);
	}
	
}
