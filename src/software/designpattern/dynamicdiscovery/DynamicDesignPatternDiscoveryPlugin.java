package software.designpattern.dynamicdiscovery;

import java.util.ArrayList;
import java.util.HashMap;

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

import UtilityClasses.ClassTypeHierarchy;
import software.designpattern.behavioralchecking.ObserverBehavioralChecking;
import software.designpattern.behavioralchecking.StateBehavioralChecking;
import software.designpattern.behavioralchecking.StrategyBehevioralChecking;
import software.designpattern.patterndefinition.PatternSet;
import software.designpattern.patterndefinition.PatternSetImpl;

/**
 * this plug-in aims to take as inputs:  software execution log 
 * and returns a set of validated pattern instances by
 * (1) discover class type hierarchy from the log; 
 * (2) discover candidate pattern instances (complete) based on structural constraints;
 * (3) identify pattern instance invocations;
 * (4) check behavioral and structural constraints. 
 * @author cliu3
 *
 */
@Plugin(
		name = "Dynamic Design Pattern Discovery Framework",// plugin name
		
		returnLabels = {"Design Patterns"}, //reture labels
		returnTypes = {PatternSetImpl.class},//return class
		
		//input parameter labels
		parameterLabels = { "Software event log" },
		
		userAccessible = true,
		help = "This plugin aims to discovery design patterns from software event data." 
		)
public class DynamicDesignPatternDiscoveryPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Dynamic Design Pattern Discovery, default",
			// the number of required parameters, {0} means the first input parameter, {0, 1} means the second input parameter, {0, 1, 2} means the third input parameter
			requiredParameterLabels = {0} 
			)
	
	public PatternSet DiscoveryandChecking(UIPluginContext context, XLog softwareLog) throws UserCancelledException
	{
		context.log("plugin starts", MessageLevel.NORMAL);
		PatternSet validatedPatternInstanceSet = new PatternSetImpl(); // final results
		XFactory factory = new XFactoryNaiveImpl();
		
		//using a combo box to select one pattern to be detected. 
		String [] patterns = new String[3];
		patterns[0]="Observer pattern";
		patterns[1]="State pattern";
		patterns[2]="Strategy pattern";
		String patternName =ProMUIHelper.queryForObject(context, "Select the pattern type to be detected", patterns);
		context.log("Selected pattern: "+patternName, MessageLevel.NORMAL);

		//get the class type hierarchy from log
		ClassTypeHierarchy cth =DiscoverClassTypeHierarchyPlugin.discoveryClassTypeHierarchy(softwareLog);
		context.log("Compute the Class Type Hierarchy", MessageLevel.NORMAL);
		
		//getting the system formated time		
		long timestampStart = System.currentTimeMillis(); 
		long timestampEnd = 0;  
		switch (patternName) {
	         case "Observer pattern"://to handle observer pattern
	        	//get the complete candidate pattern instances, a set of mappings from role --> value
	     		ObserverStaticDiscovery osd = new ObserverStaticDiscovery();
	     		ObserverBehavioralChecking obc = new ObserverBehavioralChecking();
	     		ArrayList<HashMap<String, Object>> ObserverResults = osd.DiscoverCompleteObserverPattern(softwareLog, cth);
	    		context.log("Number of statically detected observer pattern instances: "+ObserverResults.size(), MessageLevel.NORMAL);

	    		System.out.println("Number of statically detected observer pattern instances: "+ObserverResults.size());
	    		System.out.println("Static Discovery Time:"+  (System.currentTimeMillis()-timestampStart));

	     		//check the dynamic behavior of the identified instances
	     		if(ObserverResults.size()>0)//one more candidates are detected
	     		{
	     			validatedPatternInstanceSet.addPatternSet(obc.ObserverPatternBehavioralConstraintsChecking(factory, softwareLog, cth, ObserverResults).getPatternSet());
	     		}
	     		
//	     		// transform the statically obtained candidates
//	     		for(HashMap<String, Object> result: ObserverResults)
//	     		{
//	     			validatedPatternInstanceSet.add(BasicFunctions.CreatePatternInstance("Observer Pattern", result, 0, 0));
//	     		}
	    		
	    		context.log("Number of validated observer pattern instances: "+validatedPatternInstanceSet.size(), MessageLevel.NORMAL);
	    		System.out.println("Number of validated observer pattern instances: "+validatedPatternInstanceSet.size());
	    		timestampEnd=System.currentTimeMillis(); 
	            break;
	         case "State pattern"://to handle state pattern.
	        	//get the complete candidate pattern instances, a set of mappings from role --> value
	     		StateStaticDiscovery ssd = new StateStaticDiscovery();
	     		StateBehavioralChecking sbc = new StateBehavioralChecking();
	     		ArrayList<HashMap<String, Object>> StateResults = ssd.DiscoverCompleteStatePattern(softwareLog, cth);
	     		context.log("Number of statically detected state pattern instances: "+StateResults.size(), MessageLevel.NORMAL);
	    		System.out.println("Number of statically detected State pattern instances: "+StateResults.size());
	    		System.out.println("Static Discovery Time:"+  (System.currentTimeMillis()-timestampStart));

	     		//check the dynamic behavior of the identified instances
	     		if(StateResults.size()>0)//one more candidates are detected
	     		{
	     			validatedPatternInstanceSet.addPatternSet(sbc.StatePatternBehavioralConstraintsChecking(factory, softwareLog, cth, StateResults).getPatternSet());
	     		}

//	     		// transform the statically obtained candidates
//	     		for(HashMap<String, Object> result: StateResults)
//	     		{
//	     			validatedPatternInstanceSet.add(BasicFunctions.CreatePatternInstance("State Pattern", result, 0, 0));
//	     		}
//	    			
	    		context.log("Number of validated state pattern instances: "+validatedPatternInstanceSet.size(), MessageLevel.NORMAL);
	    		System.out.println("Number of validated State pattern instances: "+validatedPatternInstanceSet.size());
	    		timestampEnd=System.currentTimeMillis(); 
	        	break;
	         case "Strategy pattern"://to handle strategy pattern.
	        	 //get the complete candidate pattern instances, a set of mappings from role --> value
		     	 StrategyStaticDiscovery stsd = new StrategyStaticDiscovery();
		     	 StrategyBehevioralChecking stbc = new StrategyBehevioralChecking();

		     	 ArrayList<HashMap<String, Object>> StrategyResults = stsd.DiscoverCompleteStrategyPattern(softwareLog, cth);
		     	 System.out.println("Number of statically detected strategy pattern instances: "+StrategyResults.size());
		     	 context.log("Number of statically detected strategy pattern instances: "+StrategyResults.size(), MessageLevel.NORMAL);
		     	 System.out.println("Static Discovery Time:"+  (System.currentTimeMillis()-timestampStart));
		     	 
		     	// transform the statically obtained candidates
		     	if(StrategyResults.size()>0)//one more candidates are detected
	     		{
		     		validatedPatternInstanceSet.addPatternSet(stbc.StrategyPatternInvocationConstraintsChecking(factory, softwareLog, cth, StrategyResults).getPatternSet());
	     		}
		     	
//		     	// transform the statically obtained candidates
//	     		for(HashMap<String, Object> result: StrategyResults)
//	     		{
//	     			validatedPatternInstanceSet.add(BasicFunctions.CreatePatternInstance("Strategy Pattern", result, 0, 0));
//	     		}
		     	
	    		context.log("Number of validated strategy pattern instances: "+validatedPatternInstanceSet.size(), MessageLevel.NORMAL);		     	
	    		System.out.println("Number of validated Strategy pattern instances: "+validatedPatternInstanceSet.size());
	    		timestampEnd=System.currentTimeMillis(); 
	            break;
	         default:
	            throw new IllegalArgumentException("Invalid pattern type: " + patternName);
	     }
		
		
		System.out.println("Discovery and Checking Time:"+  (timestampEnd-timestampStart));
		
		return validatedPatternInstanceSet;
	}
}
