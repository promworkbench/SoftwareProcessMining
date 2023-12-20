package software.designpattern.combinationdiscovery;

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

import UtilityClasses.ClassTypeHierarchy;
import software.designpattern.behavioralchecking.BasicOperators;
import software.designpattern.patterndefinition.PatternClass;
import software.designpattern.patterndefinition.PatternSet;
import software.designpattern.patterndefinition.PatternSetImpl;

/**
 * this plug-in aims to take as inputs: (1) software execution log; (2) candidate pattern instances (incomplete); and (3) class type hierarchy 
 * and returns a set of validated pattern instances by
 * (1) discover missing roles for the candidate pattern instances (detected from DPD tools), based on some structural constraints;
 * (2) identify pattern instance invocations;
 * (3) check behavioral and structural constraints. 
 * 
 * Now it supports the following patterns:2017-11-6
 * Observer pattern
 * State pattern
 * Strategy pattern
 * Singleton pattern
 * @author cliu3
 *
 */
@Plugin(
		name = "Combination Design Pattern Discovery Framework",// plugin name
		
		returnLabels = {"Design Patterns"}, //reture labels
		returnTypes = {PatternSetImpl.class},//return class
		
		//input parameter labels
		parameterLabels = {"Design Pattern Candidates", "Software event log", "Class Type Hierarchy"},
		
		userAccessible = true,
		help = "This plugin aims to improve the Design Pattern results discovered from DPD tool based on execution data." 
		)
public class CombinationalDesignPatternDetectionFramework {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Combination Design Pattern Discovery and Checking, default",
			// the number of required parameters, {0} means the first input parameter, {1} means the second input parameter, {2} means the third input parameter
			requiredParameterLabels = {0, 1, 2}
			)
	public PatternSet DiscoveryandChecking(UIPluginContext context, PatternSet patternSet, XLog softwareLog, ClassTypeHierarchy cth)
	{
		//the input patternSet is derived from the static tool, which may be not complete. 
		//context.log("plugin starts", MessageLevel.NORMAL);
		PatternSet completeCandidateInstanceSet = new PatternSetImpl(); // intermediate results
				
		for(PatternClass p: patternSet.getPatternSet())
		{
			context.log("pattern instance: "+p.toString(), MessageLevel.NORMAL);
					
			//these methods differs from design patterns. 
			HashMap<String, ArrayList<Object>> role2values = BasicOperators.Role2Values(p);
			
			//create factory to create Xlog, Xtrace and Xevent.
			XFactory factory = new XFactoryNaiveImpl();
			
			//Discover missing values for those roles, and for those values that are not in the log, use the class type hierarchy information.
			ObserverPatternDiscoveryAndChecking discoverObserverPattern = new ObserverPatternDiscoveryAndChecking();
			StatePatternDiscoveryAndChecking discoverStatePattern = new StatePatternDiscoveryAndChecking();
			StrategyPatternDiscoveryAndChecking discoverStrategyPattern = new StrategyPatternDiscoveryAndChecking();
			SingletonPatternDiscoveryAndChecking discoverSingletonPattern = new SingletonPatternDiscoveryAndChecking();
			
//			CommandPatternDiscoveryAndChecking discoverCommandPattern = new CommandPatternDiscoveryAndChecking();
//			VisitorPatternDiscoveryAndChecking discoverVisitorPattern = new VisitorPatternDiscoveryAndChecking();
			PatternSet validatedPatterns;
			if (p.getPatternName().equals("Observer Pattern"))//for observer pattern candidates.
			{
				// discover missing role for the current candidate observer pattern instance. 
				ArrayList<HashMap<String, Object>> result=discoverObserverPattern.DiscoverCompleteObserverPattern(context, p, softwareLog, cth, role2values);
				//parse through all complete candidates and check the invocation level constraints. 
				validatedPatterns =discoverObserverPattern.ObserverPatternBehavioralConstraintsChecking(factory, p, softwareLog, cth, result);
				if(validatedPatterns!=null)
				{
					completeCandidateInstanceSet.addPatternSet(validatedPatterns.getPatternSet());
				}
			}
			else if(p.getPatternName().equals("State Pattern"))// for state pattern candidates
			{
				//discover missing role for the current candidate state pattern instance. 
				ArrayList<HashMap<String, Object>> result=discoverStatePattern.DiscoverCompleteStatePattern(context, p, softwareLog, cth, role2values);
				//parse through all complete candidates and check the invocation level constraints. 
				validatedPatterns =discoverStatePattern.StatePatternInvocationConstraintsChecking(context, factory, p, softwareLog, cth, result);
				if(validatedPatterns!=null)
				{
					completeCandidateInstanceSet.addPatternSet(validatedPatterns.getPatternSet());
				}
			}
			else if(p.getPatternName().equals("Strategy Pattern"))//for strategy patterns
			{
				//discover missing role for the current candidate strategy pattern instance. 
				ArrayList<HashMap<String, Object>> result=discoverStrategyPattern.DiscoverCompleteStrategyPattern(context, p, softwareLog, cth, role2values);
				//parse through all complete candidates and check the invocation level constraints. 
				validatedPatterns =discoverStrategyPattern.StrategyPatternInvocationConstraintsChecking(context, factory, p, softwareLog, cth, result);
				if(validatedPatterns!=null)
				{
					completeCandidateInstanceSet.addPatternSet(validatedPatterns.getPatternSet());
				}
			}
			else if (p.getPatternName().equals("Singleton Pattern"))//for the singleton pattern
			{
				//discover missing role for the current candidate singleton pattern instance. 
				ArrayList<HashMap<String, Object>> result=discoverSingletonPattern.DiscoverCompleteSinglePattern(context, p, softwareLog, cth, role2values);
				
				//parse through all complete candidates and check the invocation level constraints. 
				validatedPatterns =discoverSingletonPattern.SingletonPatternBehavioralConstraintsChecking(context, factory, p, softwareLog, cth, result);
				if(validatedPatterns!=null)
				{
					completeCandidateInstanceSet.addPatternSet(validatedPatterns.getPatternSet());
				}
			}
//			else if (p.getPatternName().equals("Command Pattern"))//for command pattern candidates.
//			{
//				// discover missing role for the current candidate command pattern instance. 
//				ArrayList<HashMap<String, Object>> result=discoverCommandPattern.DiscoverCompleteCommandPattern(context, p, softwareLog, cth, role2values);
//				//parse through all complete candidates and check the invocation level constraints. 
//				validatedPatterns =discoverCommandPattern.CommandPatternInvocationConstraintsChecking(context, factory, p, softwareLog, cth, result);
//				if(validatedPatterns!=null)
//				{
//					completeCandidateInstanceSet.addPatternSet(validatedPatterns.getPatternSet());
//				}
//			}
			
//			else if (p.getPatternName().equals("Visitor Pattern"))//for observer pattern candidates.
//			{
//				// discover missing role for the current candidate observer pattern instance. 
//				ArrayList<HashMap<String, Object>> result=discoverVisitorPattern.DiscoverCompleteVisitorPattern(context, p, softwareLog, cth, role2values);
//				//parse through all complete candidates and check the invocation level constraints. 
//				validatedPatterns =discoverVisitorPattern.VisitorPatternInvocationConstraintsChecking(context, factory, p, softwareLog, cth, result);
//				if(validatedPatterns!=null)
//				{
//					completeCandidateInstanceSet.addPatternSet(validatedPatterns.getPatternSet());
//				}
//			}
//			else if (p.getPatternName().equals("Strategy Pattern"))
//			{
//				
//			}

		}
		
		
		return completeCandidateInstanceSet;

	}
}
