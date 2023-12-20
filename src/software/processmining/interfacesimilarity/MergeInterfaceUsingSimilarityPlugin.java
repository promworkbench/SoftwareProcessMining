package software.processmining.interfacesimilarity;
/*
 * this plugin aims to merge interfaces based on the input similarity
 * Input: SoftwareDescription
 * Output: improved SoftwareDescription
 * 
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;

import UtilityClasses.MethodClass;
import software.processmining.interfacediscoveryevaluation.ComponentDescription;
import software.processmining.interfacediscoveryevaluation.InterfaceDescription;
import software.processmining.interfacediscoveryevaluation.SoftwareDescription;

@Plugin(
		name = "Merge Interface Using Similarity",// plugin name
		
		returnLabels = {"Software Description"}, //return labels
		returnTypes = {SoftwareDescription.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Description"},
		
		userAccessible = true,
		help = "This plugin aims to merge interfaces using similarity." 
		)
public class MergeInterfaceUsingSimilarityPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Discovering interface behavior models, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	
	public SoftwareDescription interactionBehaviorDiscovery(UIPluginContext context, SoftwareDescription softwareDescription) throws UserCancelledException
	{
		//select the similarity value
		double similarityThreshold = ProMUIHelper.queryForDouble(context, "Select the nesting depth", 0, 1,	0.8);		
		context.log("Interface Similarity Threshold is: "+similarityThreshold, MessageLevel.NORMAL);	
	
		//create a new software description and its component description set
		SoftwareDescription newSoftwareDescription = new SoftwareDescription();
		HashSet<ComponentDescription> newComponentDescriptionSet = new HashSet<>();
		
		
		for(ComponentDescription componentDescription: softwareDescription.getComponentSet())
		{
			//create a new component description
			ComponentDescription newComponentDescription = new ComponentDescription();
			newComponentDescription.setComponentName(componentDescription.getComponentName());
			HashSet<InterfaceDescription> newInterfaceDescriptionSet = new HashSet<>();
			newComponentDescription.setInterfaceSet(newInterfaceDescriptionSet);
			
			//we try to merge interfaces within the same component. 
			//create a method set (of interface) to interfaceDescription mapping. 
			HashMap<HashSet<MethodClass>, InterfaceDescription> methodSet2interfaceDescription = new HashMap<>();
			//create a list of method set (of interfaces). 
			ArrayList<HashSet<MethodClass>> methodSetList = new ArrayList<>();
			
			for(InterfaceDescription id: componentDescription.getInterfaceSet())// add values
			{
				if(!methodSet2interfaceDescription.keySet().contains(id.getMethodSet()))
				{
					methodSet2interfaceDescription.put(id.getMethodSet(), id);
					methodSetList.add(id.getMethodSet());
				}
				else{//merge the current two interface descriptions
					InterfaceDescription newInterfaceD =MergeInterfaceUseSimilarity.mergerInterfaceDescription(methodSet2interfaceDescription.get(id.getMethodSet()), id);
					methodSet2interfaceDescription.put(id.getMethodSet(), newInterfaceD);
				}
			}
			
			//merging similar candidate interfaces according to the threshold
			HashMap<HashSet<MethodClass>, InterfaceDescription> mergedMethodSet2interfaceDescription=MergeInterfaceUseSimilarity.recursiveMergingInterfaces(methodSet2interfaceDescription, new ArrayList<HashSet<MethodClass>>(), methodSetList, similarityThreshold); 
			
			//construct the new interfaceSet
			for(InterfaceDescription id:mergedMethodSet2interfaceDescription.values())
			{
				newInterfaceDescriptionSet.add(id);
			}
			newComponentDescriptionSet.add(newComponentDescription);
			
		}
		
		newSoftwareDescription.setComponentSet(newComponentDescriptionSet);
		return newSoftwareDescription;
		
	}
}
