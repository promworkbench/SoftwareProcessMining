package software.processmining.interfacediscovery;

import java.util.ArrayList;
import java.util.HashMap;
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
import UtilityClasses.MethodClass;
import software.processmining.componentbehaviordiscovery.ConstructSoftwareComponentLog;
import software.processmining.componentbehaviordiscovery.HSoftwareEventLog;
import software.processmining.componentbehaviordiscovery.HierarchicalPetriNet;
import software.processmining.componentbehaviordiscovery.HierarchicalSoftwareEventLogConstruction;
import software.processmining.componentbehaviordiscovery.MineHierarchicalPetriNet;

/**
 * this plugin aims to discover the interface for different components. 
 * The main steps contains: (1) group methods to form interfaces according to the similarity threshold; 
 * 		 (2) identify the interface instance and refactoring the interface log; and  
 * 		 (3) each component has multiple interfaces (each refers to a hpn). 
 * 
 * Input 1: a software event log (original data, each trace refers to one software execution);
 * Input 2: configuration file indicating the mapping from component to classes, with suffix .conf.
 * 
 * Output: a set of components, each with a set of interface models (HPN models) and interface description. 
 * @author cliu3 2017-4-13
 *
 *New improvement: 2017-5-7
 *to get the invoked method set, not only use method name, class name, package name, we also use class object information. 
 *
 *New improvement: 2017-5-29
 *remove the class name and package name information when getting the invoked method set. 
 *congliu.processminig.softwarecomponentinteractionbehaviordiscovery.ConstructHLog.getMainOtherLevels(XLog mainLog, HashSet<XEvent> eventList, XLog originalLog, XFactory factory, String componentName)
 *This will avoid mistakes caused by class inheritance. 
 *
 */

@Plugin(
		name = "Software Interface Behavior Discovery (Similarity)",// plugin name
		
		returnLabels = {"Software Interface Behavior Models"}, //return labels
		returnTypes = {ComponentModelsSet.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Event Log", "Component Configuration"},
		
		userAccessible = true,
		help = "This plugin aims to discover the interfaces as well as their behavior models by using the interaction information in the log." 
		)
public class InterfaceBehaviorDiscoveryPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Discovering interface behavior models, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0, 1}
			)
	
	public ComponentModelsSet interactionBehaviorDiscovery(UIPluginContext context, XLog originalLog, 
			ComponentConfig comconfig) throws ConnectionCannotBeObtained, NumberFormatException, UserCancelledException
	{
		int num=0;
		
		//get the log name from the original log. it is shown as the title of returned results. 
		context.getFutureResult(0).setLabel("Software Interface Behavior Discovery");
		context.log("Interface Behavior Discovery Starts...", MessageLevel.NORMAL);		
		
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
		
		//
		double similarityThreshold = ProMUIHelper.queryForDouble(context, "Select the nesting depth", 0, 1,
				0.8);		
		context.log("Interface Similarity Threshold is: "+similarityThreshold, MessageLevel.NORMAL);	
		
		
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
				
		
		//create factory to create Xlog, Xtrace and Xevent.
		XFactory factory = new XFactoryNaiveImpl();
		XLogInfo Xloginfo = XLogInfoFactory.createLogInfo(originalLog, IMparameters.getClassifier());

		//for each component, we construct its top-level method set
		for(String com: comconfig.getAllComponents())
		{
			context.log("The current component is: "+com, MessageLevel.NORMAL);		
			System.out.println("The current component is: "+com);
			
			// create class set of the current component
			Set<String> componentClassSet = new HashSet<>();
			for(ClassClass c: comconfig.getClasses(com))
			{
				componentClassSet.add(c.getPackageName()+"."+c.getClassName());// both the package and class names are used to describe each class
			}
						
			ComponentModels componenModels = new ComponentModels();
			componenModels.setComponent(com);// set component name
					
			HashSet<Interface2HPN> i2hpnSet = new HashSet<Interface2HPN>();//for each component, it has a set of <interfaces->interface model>
						
			//obtain the software event log for each component, we have the instance identification at the component level. 
			// input:(1) class set of the current component; and (2) the original software log
//			XLog comLog = ConstructSoftwareEventLog.generatingComponentEventLog(com, componentClassSet, originalLog, factory);
			
			XLog comLog =ConstructSoftwareComponentLog.generatingComponentSoftwareEventLog(com, componentClassSet, originalLog, factory);
			//construct the caller method set for each component 
			HashSet<MethodClass> callerMethodSet = InterfaceCandidateIdentification.constructCallerMethodSet(componentClassSet, comLog); 
			
				
			//construct candidate interfaces, each interface contains a set of top-level methods that are invoked by one single caller method. 
			//Note that the same interface (a set of methods) can be called by multiple caller methods, after merging based on similarity. 
			HashMap<HashSet<MethodClass>, HashSet<MethodClass>> CandidateInterface2callerMethod = new HashMap<>();
			
			ArrayList<HashSet<MethodClass>> candidateInterfaceList = new ArrayList<>();// the original interface list without merging.
			for(MethodClass callerM: callerMethodSet)
			{
				//each candidate is represented by its top-level method calls
				HashSet<MethodClass> candidateInterface =InterfaceCandidateIdentification.constructCandidateInterface(comLog, callerM);
				candidateInterfaceList.add(candidateInterface);
				if(CandidateInterface2callerMethod.keySet().contains(candidateInterface))
				{
					CandidateInterface2callerMethod.get(candidateInterface).add(callerM);
				}
				else{
					HashSet<MethodClass> callerMethods=new HashSet<>();
					callerMethods.add(callerM);
					CandidateInterface2callerMethod.put(candidateInterface, callerMethods);		
				}		
				//System.out.println(callerM+"----->"+candidateInterface);
			}
			
			//merging similar candidate interfaces according to the threshold
			HashMap<HashSet<MethodClass>, HashSet<MethodClass>> interface2CallerSet=InterfaceMergeUsingSimilarity.recursiveComputing(CandidateInterface2callerMethod, new ArrayList<HashSet<MethodClass>>(), candidateInterfaceList, similarityThreshold); 
			
			System.out.println("----->all interfaces"+interface2CallerSet.keySet());
			
			//for each interface, we construct its event log 
			for(HashSet<MethodClass> interfaceM: interface2CallerSet.keySet())
			{
				System.out.println("The current interface is: "+interfaceM);

				//get the event log of each interface, when construct log we need also take the caller methods as input.
				XLog interfaceLog = ConstructSoftwareEventLog.constructInterfaceLog(comLog, maximalNestingDepth, interfaceM, interface2CallerSet.get(interfaceM), factory);
				
//				//refactoring the event log by identifying interface instances
//				XLog interfaceInstanceLog = ConstructSoftwareEventLog.constructInterfaceInstanceLog(interfaceLog, factory, classSet);
						
				// hierarchical software event log, null means start from Top-level
				
				HSoftwareEventLog hseLog = HierarchicalSoftwareEventLogConstruction.constructHierarchicalLog(maximalNestingDepth, factory, componentClassSet, 
						interfaceLog, Xloginfo, com);
				
				//hierarchical mining, HPN
				HierarchicalPetriNet hpn = MineHierarchicalPetriNet.mineHierarchicalPetriNet(context, hseLog, IMparameters);
				
				/*
				 * this part need to be improved...
				 */
				Interface i= new Interface("", com);
				i.setId(com+(num++));
				Interface2HPN i2hpn = new Interface2HPN(i, hpn);
				i2hpnSet.add(i2hpn);
			}
			
			componenModels.setI2hpn(i2hpnSet);// the interface part of the component
			componentModelSet.addComponent2HPNSet(componenModels);
		}
		
		return componentModelSet;
	}// plug-in method
}
