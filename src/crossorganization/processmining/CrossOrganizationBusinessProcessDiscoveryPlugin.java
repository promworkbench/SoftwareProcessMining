package crossorganization.processmining;

import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.plugins.declareminer.DeclareMiner;
import org.processmining.plugins.declareminer.DeclareMinerInput;
import org.processmining.plugins.declareminer.enumtypes.DeclareProMInput;
import org.processmining.plugins.declareminer.ui.DeclareMinerConfigurationUI;
import org.processmining.plugins.declareminer.visualizing.DeclareMinerOutput;

/*
 * this plugin aims to discover cross-organization process models from event logs with organization information. 
 */
@Plugin(
		name = "Cross-organization Business Process Model Discovery",// plugin name
		
		returnLabels = {"Cross-organization Business Process Model"}, //return labels
		returnTypes = {CrossOrganizationBusinessProcessModel.class},//return class, a cross-organization process model
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Cross-organization Event Log"},
		
		userAccessible = true,
		help = "This plugin aims to discover cross-organization process models from event logs with organization information." 
		)
public class CrossOrganizationBusinessProcessDiscoveryPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl;liucongchina@gmail.com"
	        )
	@PluginVariant(
			variantLabel = "Cross-organization Business Process Model Discovery, default",
			// the number of required parameters, 0 means the first input parameter 
			requiredParameterLabels = {0})
	
	public CrossOrganizationBusinessProcessModel CrossOrganizationBehaviorDiscovery(UIPluginContext context, XLog originalLog) throws UserCancelledException
	{			
		CrossOrganizationBusinessProcessModel crossOrgModel = new CrossOrganizationBusinessProcessModel();
		
		//identify the organization to activities.
		OrganizationConfig orgConfig = OrganizationIdentification.identifyOrganizationsFromLog(originalLog);
		
		XFactory factory = new XFactoryNaiveImpl();
		
		//create separate event log for each organization
		HashMap<String, XLog> org2Log = OrganizationLogConstruction.contructOrganizationLog(originalLog, orgConfig);
				
		//we use existing discovery algorithm to discover process model for each organization. 
		HashMap<String, Petrinet> org2PN = new HashMap<String, Petrinet>();
		int count =1;//the inductive miner parameters are set only for the first time
		IMMiningDialog dialog;
		InteractionResult result;
		MiningParameters IMparameters=null;
		for(String org: org2Log.keySet())
		{
			//for the first time, we set the parameter
			if(count ==1)
			{
				//set the inductive miner parameters, the original log is used to set the classifier
				dialog = new IMMiningDialog(org2Log.get(org));
				result = context.showWizard("Configure Parameters for Inductive Miner (used for all intra-organization models)", true, true, dialog);
				if (result != InteractionResult.FINISHED) {
					return null;
				}
				// the mining parameters are set here 
				IMparameters = dialog.getMiningParameters(); //IMparameters.getClassifier()
			}
			
			//use the inductive miner to discover a pn for each organization. 
			Object[] objs =IMPetriNet.minePetriNet(org2Log.get(org), 
					IMparameters, new Canceller() {
				public boolean isCancelled() {
					return false;
				}
			});
			
			//check the single entry and single exist property
			Petrinet pn =TransformCrossOrganizationBusinessProcessModel2PetriNet.addingArtifitialSouceandTargetPlaces((Petrinet) objs[0], (Marking)objs[1], (Marking)objs[2]);
			
			org2PN.put(org, pn);
			count++;
		}
		
		crossOrgModel.setOrganizationModels(org2PN); 
		
		//discover interaction using declare mining
		//(1) use the precedence relation miner
		//(2) Transitive reduction refer to JGraphT TransitiveReduction
		//(3) select only precedence relation between activities of two different organizations

		//we provide two modes for constraints discovery, normal model and branch filtering based model. 
		String [] discoveryModes = new String[2];
		discoveryModes[0]="Normal Discovery";
		discoveryModes[1]="Handle Branch Behavior Separately";
		String selectedMode =ProMUIHelper.queryForObject(context, "Select the discovery models", discoveryModes);
		context.log("The selected discovery mode is: "+selectedMode, MessageLevel.NORMAL);	
		System.out.println("Selected discovery mode is: "+selectedMode);
						

		//use the declare miner tool to discover all precedence relations in the original log
		/*
		 * Note select (1) the precedence relation, (2) select ignoring the event types.
		 */
		DeclareMinerConfigurationUI declareMinerConfigurationUI = new DeclareMinerConfigurationUI(context,DeclareProMInput.Log_Only);
		DeclareMinerInput input = declareMinerConfigurationUI.getInput();
//		input.setReferenceEventType("complete");
		if(input.isEmpty())
			return null;
//		
//		//set AprioriKnowledgeBasedCriteria to make sure ignoring the event types.
//		Set<AprioriKnowledgeBasedCriteria> aprioriKnowledgeBasedCriteriaSet = new HashSet<>();
//		aprioriKnowledgeBasedCriteriaSet.add(AprioriKnowledgeBasedCriteria.AllActivitiesIgnoringEventTypes);
//		input.setAprioriKnowledgeBasedCriteriaSet(aprioriKnowledgeBasedCriteriaSet);
		
		//normal model selected
		if(selectedMode.equals("Normal Discovery"))
		{
			//the declare results
			DeclareMinerOutput output=DeclareMiner.mineDeclareConstraints(context, originalLog, input);
			
			//discover interactions
			HashSet<CrossOrganizationInteraction> interactions = CrossOrganizationInteractionRelationDiscovery.discoverCrossOrganizationInteractions(orgConfig, output);
			
			for(CrossOrganizationInteraction i: interactions)
			{
				System.out.println("+++++++++++++ discovered Interactions: "+i);
			}
			
			//add interactions
			crossOrgModel.setAllInteractions(interactions);		
		}
		
		/*
		 * branch mode selected. 
		 * to support the discovery of more precise dependency relations. 
		 * E.g., A->B 100 traces and C->B 10 traces, then C->B will be never detection. 
		 * we divide the original log by its activity set. 
		 */
		
		if(selectedMode.equals("Handle Branch Behavior Separately"))
		{
			//get the activity set to sub log mapping
			HashMap<HashSet<String>, XLog> ActivitySet2OrgLog = new HashMap<>();
			for(XTrace trace: originalLog)
			{
				HashSet<String> activitySet =activitySetPerTrace(trace);
				if(!ActivitySet2OrgLog.containsKey(activitySet))
				{
					//create the sub log
					XLog subLog = factory.createLog();
					subLog.add(trace);
					ActivitySet2OrgLog.put(activitySet, subLog);
					
				}
				else{
					ActivitySet2OrgLog.get(activitySet).add(trace);
				}
				
			}
			System.out.println("<<<<<<<<<<<<<<< the number of subLog"+ActivitySet2OrgLog.keySet().size()+" >>>>>>>>>>>>>>>");
			
			//the combined interactions
			final HashSet<CrossOrganizationInteraction> combinedInteractions =new HashSet<>();
			for(HashSet<String> actSet:ActivitySet2OrgLog.keySet())
			{
				//the declare results
				DeclareMinerOutput output=DeclareMiner.mineDeclareConstraints(context, ActivitySet2OrgLog.get(actSet), input);
				
				//discovered interactions from the current log
				HashSet<CrossOrganizationInteraction> interactions = CrossOrganizationInteractionRelationDiscovery.discoverCrossOrganizationInteractions(orgConfig, output);
				
				for(CrossOrganizationInteraction i: interactions)
				{
					System.out.println("+++++++++++++ discovered Interactions: "+i);
				}
				
				//obtain the combined interactions, the combination may cause some inaccuracy. 
				if(combinedInteractions.size()==0)
				{
					combinedInteractions.addAll(interactions);
				}
				else{
					//get the common interactions
					HashSet<CrossOrganizationInteraction> commonInteractions =getcommonInteraction(combinedInteractions, interactions);
					for(CrossOrganizationInteraction i: commonInteractions)
					{
						System.out.println("+++++++++++++: "+i);
					}
					HashSet<CrossOrganizationInteraction> addInteractions = new HashSet<>();
					for(CrossOrganizationInteraction inte1 : combinedInteractions)
					{
						if(!commonInteractions.contains(inte1))
						{
							for(CrossOrganizationInteraction inte2: interactions)
							{
								if(!commonInteractions.contains(inte2))
								{
									//similar targets then merge sources
									if(inte1.getTargetActivities().containsAll(inte2.getTargetActivities()))
									{
										inte1.getSourceActivities().addAll(inte2.getSourceActivities());
										continue;
									}
									//similar sources then merge targets
									if(inte1.getSourceActivities().containsAll(inte2.getSourceActivities()))
									{
										inte1.getTargetActivities().addAll(inte2.getTargetActivities());
										continue;
									}
									addInteractions.add(inte2);
								}
							}
						}
						
					}
					combinedInteractions.addAll(addInteractions);
				}
			}
			
			//add interactions
			crossOrgModel.setAllInteractions(combinedInteractions);		
		}

		
		
//		
//		//the declare results
//		DeclareMinerOutput output=DeclareMiner.mineDeclareConstraints(context, originalLog, input);
//		
//		//discover interactions
//		HashSet<CrossOrganizationInteraction> interactions = CrossOrganizationInteractionRelationDiscovery.discoverCrossOrganizationInteractions(orgConfig, output);
//		
//		//add interactions
//		crossOrgModel.setAllInteractions(interactions);		
		
		return crossOrgModel;
	}
	
	//get the activity set of a trace
	public static HashSet<String> activitySetPerTrace(XTrace trace)
	{
		HashSet<String> activitySet = new HashSet<>();
		for(XEvent event: trace)
		{
			activitySet.add(XConceptExtension.instance().extractName(event));
		}
		return activitySet;
	}
	
	/*
	 * reture the common elements of two hashset
	 */
	public static HashSet<CrossOrganizationInteraction> getcommonInteraction(HashSet<CrossOrganizationInteraction> group1, HashSet<CrossOrganizationInteraction> group2)
	{
		HashSet<CrossOrganizationInteraction> temp1 = new HashSet<CrossOrganizationInteraction>();
		temp1.addAll(group1);
		temp1.retainAll(group2);
		
		
		return temp1;
	}
}
