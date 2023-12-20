package software.processmining.multiviewinteraction;

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
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMflc;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;

import UtilityClasses.ClassClass;
import UtilityClasses.ComponentConfig;
import UtilityClasses.MethodClass;
import UtilityFunctions.OrderingEventsNano;
import openXESsoftwareextension.XSoftwareExtension;
import software.processmining.componentbehaviordiscovery.ConstructSoftwareComponentLog;
import software.processmining.componentbehaviordiscovery.HSoftwareEventLog;
import software.processmining.componentbehaviordiscovery.HierarchicalPetriNet;
import software.processmining.componentbehaviordiscovery.HierarchicalSoftwareEventLogConstruction;
import software.processmining.componentbehaviordiscovery.MineHierarchicalPetriNet;
import software.processmining.interfacediscovery.ConstructSoftwareEventLog;
import software.processmining.interfacediscoveryevaluation.ComponentDescription;
import software.processmining.interfacediscoveryevaluation.InterfaceDescription;
import software.processmining.interfacediscoveryevaluation.SoftwareDescription;

/**
 * this plugin aims to discover an architectural model by taking:
 * (1) software execution data
 * (2) Software description: it contains which methods are included in which interface
 * (3) component configuration
 * The architectural model contains: (1) (top-level) method to connector; 
 * 		and (2) the instance level cardinality (multiplicity) relation, i.e. 1...1, and 1...n.
 * 		and (3) interface behavior model, top-level method only.  
 * 
 * Assumptions: 
 * (1) interface model only 1-level nesting. 
 * (2) interface cardinality are determined by the component instance. Therefore, the interface log are constructed based on the component log.  
 * 
 * @author cliu3
 *
 */
@Plugin(
		name = "Software Architecture Discovery Plugin",// plugin name
		
		returnLabels = {"Software Architecture"}, //return labels
		returnTypes = {SoftwareArchitectureModel.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Event Log", "Component Configuration", "Software Desciption"},
		
		userAccessible = true,
		help = "This plugin aims to discover an architecture model of a piece of software." 
		)
public class SoftwareArchitectureDiscoveryPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl;liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Discovering software architecture model, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0, 1, 2}
			)
	public SoftwareArchitectureModel architectureDiscovery(UIPluginContext context, XLog originalLog, 
			ComponentConfig comconfig, SoftwareDescription softwareDescription) throws ConnectionCannotBeObtained
	{
		//config the following setting 
		//get the log name from the original log. it is shown as the title of returned results. 
		context.getFutureResult(0).setLabel("Software Architecture Discovery");
		
		//set the inductive miner parameters, the original log is used to set the classifier
		IMMiningDialog dialog = new IMMiningDialog(originalLog);
		InteractionResult result = context.showWizard("Configure Parameters for Inductive Miner (used for all interface models)", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		// the mining parameters are set here 
		MiningParameters IMparameters = dialog.getMiningParameters(); //IMparameters.getClassifier()
		
		//create factory to create Xlog, Xtrace and Xevent.
		XFactory factory = new XFactoryNaiveImpl();
		XLogInfo Xloginfo = XLogInfoFactory.createLogInfo(originalLog, IMparameters.getClassifier());
		
		//----------------------------------------------------------------------------------------------------discovery starts here!
		//define the architecture that we are going to discover. 
		SoftwareArchitectureModel architecture = new SoftwareArchitectureModel();
		
		//the component models
		ComponentModelSet componentModelSet = new ComponentModelSet();
		architecture.setComponentModelSet(componentModelSet);
		
		//the interface cardinalities, all interfaces of the same component have the same cardinality. 
		HashMap<InterfaceID, Integer> interfaceCardinality = new HashMap<InterfaceID, Integer>();
		architecture.setInterfaceCardinality(interfaceCardinality);
		
		//interaction method to connector model
		HashMap<String, Petrinet> interactionMethod2connector= new HashMap<>();
		
		//interaction method to connector model
		HashMap<String, XLog> interactionMethod2log= new HashMap<>();
				
		//all top-level <methods and interface name>, a small portion of them are interaction methods
		HashMap<MethodInterface, String> topLevelMethod2Component = new HashMap();
		
		//InterfaceID set of the whole software
		HashSet<InterfaceID> InterfaceIDSet = new HashSet<>();
		
//		//a mapping from component name to component log
//		HashMap<String, XLog> component2Log = new HashMap<>();
		
//		//for each interface, we create a set of traces from the component log. each trace contains two events, i.e., the start and component of each interface. 
//		HashMap<InterfaceID, HashSet<XTrace>> abstractedInterfaceEvents = new HashMap<>();
		
		//(step1) we first construct a set of component models, each one involves a set of interface models. 
		for(ComponentDescription componentDescription: softwareDescription.getComponentSet())
		{
			int num=0;//create interface name. ComponentName+id. 

			// create class set of the current component
			Set<String> classSet = new HashSet<>();
			for(ClassClass c: comconfig.getClasses(componentDescription.getComponentName()))
			{
				classSet.add(c.getPackageName()+"."+c.getClassName());// both the package and class names are used to describe each class
			}
			
			//get the interface (component) cardinality information
			int cardinality = ConstructSoftwareComponentLog.componentInstanceCardinality(componentDescription.getComponentName(), classSet, originalLog, factory);
						
			ComponentModel componenModel = new ComponentModel();//create component model
			componentModelSet.setComponentModel(componenModel);
			componenModel.setComponentName(componentDescription.getComponentName());// set component name
			
			//obtain the software event log for each component.we have the instance identification at the component level. 
			// input:(1) class set of the current component; and (2) the original software log
			XLog comLog =ConstructSoftwareComponentLog.generatingComponentSoftwareEventLog(componentDescription.getComponentName(), classSet, originalLog, factory);

			HashSet<InterfaceID2HierarchicalPetriNet> interface2hpnSet = new HashSet<InterfaceID2HierarchicalPetriNet>();//for each component, it has a set of <interfaces->interface model>
			//for each component, we construct the component model 
			for(InterfaceDescription interdes: componentDescription.getInterfaceSet())// add values
			{		
				//get the event log of each interface, when construct log we need also take the caller methods as input.
				XLog interfaceLog = ConstructSoftwareEventLog.constructInterfaceLog(comLog, 1, interdes.getMethodSet(), 
						interdes.getCallerMethodSet(), factory);
				
				// hierarchical software event log, null means start from Top-level
				HSoftwareEventLog hseLog = HierarchicalSoftwareEventLogConstruction.constructHierarchicalLog(1, factory, classSet, 
						interfaceLog, Xloginfo, componentDescription.getComponentName());
				
				//hierarchical mining, HPN
				HierarchicalPetriNet hpn = MineHierarchicalPetriNet.mineHierarchicalPetriNet(context, hseLog, IMparameters);
				
				InterfaceID interID= new InterfaceID();
				interID.setInterfaceDescription(interdes);
				interID.setName(componentDescription.getComponentName()+(num++));
				
				InterfaceIDSet.add(interID);
				InterfaceID2HierarchicalPetriNet inter2hpn = new InterfaceID2HierarchicalPetriNet(interID, hpn);
				interface2hpnSet.add(inter2hpn);
				interfaceCardinality.put(interID, cardinality);
				
				//add to the top-level methods
				for(MethodClass m: interdes.getMethodSet())
				{
					MethodInterface mi = new MethodInterface();
					mi.setMethod(m);
					mi.setInterName(interID.getName());
					topLevelMethod2Component.put(mi, componentDescription.getComponentName());
				}
				
			}
			
			componenModel.setI2hpn(interface2hpnSet);// the interface part of the componentModel
		}
		
		//obtain the mapping from <interaction method, interfaceName> to its interface set. 
		HashMap<MethodInterface, HashSet<InterfaceID>> interactionMethod2InterfaceIDSet = new HashMap<>();
		
		//create the methodcalling graph
		MethodCallingGraph mcg = MethodCallConnectivity.methodCallGraph(originalLog);

		context.getProvidedObjectManager().createProvidedObject("Method calling graph", mcg, MethodCallingGraph.class, context);
		//for each top-level method, we first check if it is a interaction method
		for(MethodInterface interactionMethod: topLevelMethod2Component.keySet())
		{
			HashSet<InterfaceID> invokedInterfaceSet = InteractionModelDiscovery.invokedInterfaceSet(
					topLevelMethod2Component.get(interactionMethod),interactionMethod.getMethod(), mcg, InterfaceIDSet);
			//reture the invoked interface set of interactionMethod
			
			if(invokedInterfaceSet.size()>0)
			{
				interactionMethod2InterfaceIDSet.put(interactionMethod, invokedInterfaceSet);
			}			
		}
//		for(MethodClass m: interactionMethod2InterfaceIDSet.keySet())
//		{
//			for(InterfaceID i: interactionMethod2InterfaceIDSet.get(m))
//			{
//				System.out.println(m+"--->"+i.getName());
//			}
//		}
		
		//discover the connector model for each interaction method. 
		HashMap<MethodInterface, Petrinet> method2connectorModel = new HashMap<>();
		architecture.setMethod2connectorModel(method2connectorModel);
		
		for(MethodInterface interactionMethod: interactionMethod2InterfaceIDSet.keySet())
		{
			//get the interaction log for each interaction method, pay atterntion to the classifier
			XLog interactionLog = InteractionModelDiscovery.createInteractionLog(originalLog,interactionMethod2InterfaceIDSet.get(interactionMethod), factory, interactionMethod.getMethod());
			
			//discover the connector model for each interaction method
			MiningParameters IMparameterNew = new MiningParametersIMflc();
			IMparameterNew.setClassifier(interactionLog.getClassifiers().get(0));// it is a bit tricky here for the classifier setting. 
			IMparameterNew.setNoiseThreshold((float) 0.2);

			Object[] objs =IMPetriNet.minePetriNet(OrderingEventsNano.ordering(interactionLog, XSoftwareExtension.KEY_STARTTIMENANO), 
					IMparameterNew, new Canceller() {
				public boolean isCancelled() {
					return false;
				}
			});
			
			method2connectorModel.put(interactionMethod, (Petrinet)objs[0]);
			
		}
		return architecture;
	}
}
