package software.processmining.componentmodelevaluation;

import javax.swing.JLabel;

import org.deckfour.xes.model.XLog;
import org.processmining.alignment.plugin.IterativeAStarPlugin;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.petrinets.EvClassLogPetrinetConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.ui.PNReplayerUI;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGen;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGenRes;

import nl.tue.astar.AStarException;

/*
 * this plugin aims to measure the quality of a hierarchical Petri againt an event log
 * 
 * Input1:Hierarchical Petri net
 * Input2:Event Log (lifecycle information)+ corresponding classifier
 * 
 * Output: fitness/precision/generalization
 * 
 * Step1:transform a hpn to a flat pn, note that the transition name should be consistent with the xeventclass in the log. 
 * Step2: compute the alignment (using Boudewijn IteractiveAStarPlugin)
 * Step3: compute the precison and generalization (using Arya AlignemntPreGen plugin)
 */

@Plugin(
		name = "Quality Measure of Petri net",// plugin name
		
		returnLabels = {"Quality Measure Description"}, //return labels
		returnTypes = {String.class},//return class
		//returnTypes = {PNRepResult.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Petri net", "Event Log"},
		
		userAccessible = true,
		help = "This plugin aims to measure the quality of a flat Petri net." 
		)
public class QualityMetricsHierarchicalPetriNetPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl;liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Identifying Component, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0, 1}
			)
	public String qualityMetricHPN(final UIPluginContext context, Petrinet pn, XLog log) throws AStarException, ConnectionCannotBeObtained
	{
		HPNQualityMetrics qualityMetrics = new HPNQualityMetrics();
		
		if (pn.getTransitions().isEmpty()) {
			context.showConfiguration("Error", new JLabel("Cannot replay on a Hierarchical Petri net that does not contain transitions. Select Cancel or Continue to continue."));
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		PNReplayerUI pnReplayerUI = new PNReplayerUI();
		Object[] resultConfiguration = pnReplayerUI.getConfiguration(context, pn, log);
		if (resultConfiguration == null) {
			context.getFutureResult(0).cancel(true);
			return null;
		}

		// if all parameters are set, replay log
		if (resultConfiguration[PNReplayerUI.MAPPING] != null) {
			context.log("replay is performed. All parameters are set.");

			// This connection MUST exists, as it is constructed by the configuration if necessary
			context.getConnectionManager().getFirstConnection(EvClassLogPetrinetConnection.class, context, pn, log);

			// get all parameters
//			IPNReplayAlgorithm selectedAlg = (IPNReplayAlgorithm) resultConfiguration[PNReplayerUI.ALGORITHM];
			IPNReplayParameter algParameters = (IPNReplayParameter) resultConfiguration[PNReplayerUI.PARAMETERS];

			// since based on GUI, create connection
			algParameters.setCreateConn(true);
			algParameters.setGUIMode(true);
//			PNRepResult res = replayLogPrivate(context, pn, log,
//					(TransEvClassMapping) resultConfiguration[PNReplayerUI.MAPPING], selectedAlg, algParameters);

			//we use the more efficient algorithm to compute the alignment @Boudewijn. 
			IterativeAStarPlugin iterativeAStart = new IterativeAStarPlugin();
			PNRepResult res = iterativeAStart.replayLog(context, pn, log, (TransEvClassMapping) resultConfiguration[PNReplayerUI.MAPPING], algParameters);

			context.getProvidedObjectManager().createProvidedObject("Petri net replay result", res, PNRepResult.class, context);

			qualityMetrics.setFitness((double)res.getInfo().get(PNRepResult.TRACEFITNESS));
			//how to get the fitness from the res???
			System.out.println("Trace fitness: "+res.getInfo().get(PNRepResult.TRACEFITNESS));
			
			//through the res to the 
			AlignmentPrecGen pregen= new AlignmentPrecGen();
			AlignmentPrecGenRes pregenResult=pregen.measurePrecision(context, pn, log, res);
			qualityMetrics.setPrecision(pregenResult.getPrecision());
			qualityMetrics.setGeneralization(pregenResult.getGeneralization());
			System.out.println("Precision : "+pregenResult.getPrecision());
			System.out.println("Generalization : "+pregenResult.getGeneralization());
			
			double fmeasure=2*qualityMetrics.getFitness()*qualityMetrics.getPrecision()/(qualityMetrics.getFitness()+qualityMetrics.getPrecision());
			qualityMetrics.setFmeasure(fmeasure);

			return VisualizeHPNQualityMetrics.visualizeQualityMetrics(qualityMetrics);

		} else {
			context.log("replay is not performed because not enough parameter is submitted");
			context.getFutureResult(0).cancel(true);
			return null;
		}
	}
	
	
	public static HPNQualityMetrics QualityMeasure(UIPluginContext context, Petrinet pn, XLog log) throws AStarException, ConnectionCannotBeObtained
	{
		HPNQualityMetrics qualityMetrics = new HPNQualityMetrics();
		
		PNReplayerUI pnReplayerUI = new PNReplayerUI();
		Object[] resultConfiguration = pnReplayerUI.getConfiguration(context, pn, log);
		if (resultConfiguration == null) {
			context.getFutureResult(0).cancel(true);
			return null;
		}

		// if all parameters are set, replay log
		if (resultConfiguration[PNReplayerUI.MAPPING] != null) {
			context.log("replay is performed. All parameters are set.");

			// This connection MUST exists, as it is constructed by the configuration if necessary
			context.getConnectionManager().getFirstConnection(EvClassLogPetrinetConnection.class, context, pn, log);

			// get all parameters
//			IPNReplayAlgorithm selectedAlg = (IPNReplayAlgorithm) resultConfiguration[PNReplayerUI.ALGORITHM];
			IPNReplayParameter algParameters = (IPNReplayParameter) resultConfiguration[PNReplayerUI.PARAMETERS];

			// since based on GUI, create connection
			algParameters.setCreateConn(true);
			algParameters.setGUIMode(true);
//			PNRepResult res = replayLogPrivate(context, pn, log,
//					(TransEvClassMapping) resultConfiguration[PNReplayerUI.MAPPING], selectedAlg, algParameters);

			//we use the more efficient algorithm to compute the alignment @Boudewijn. 
			IterativeAStarPlugin iterativeAStart = new IterativeAStarPlugin();
			PNRepResult res = iterativeAStart.replayLog(context, pn, log, (TransEvClassMapping) resultConfiguration[PNReplayerUI.MAPPING], algParameters);

			context.getProvidedObjectManager().createProvidedObject("Petri net replay result", res, PNRepResult.class, context);

			qualityMetrics.setFitness((double)res.getInfo().get(PNRepResult.TRACEFITNESS));
			//how to get the fitness from the res???
			System.out.println("Trace fitness: "+res.getInfo().get(PNRepResult.TRACEFITNESS));
			
			//through the res to the 
			AlignmentPrecGen pregen= new AlignmentPrecGen();
			AlignmentPrecGenRes pregenResult=pregen.measurePrecision(context, pn, log, res);
			qualityMetrics.setPrecision(pregenResult.getPrecision());
			qualityMetrics.setGeneralization(pregenResult.getGeneralization());
			System.out.println("Precision : "+pregenResult.getPrecision());
			System.out.println("Generalization : "+pregenResult.getGeneralization());
			
			double fmeasure=2*qualityMetrics.getFitness()*qualityMetrics.getPrecision()/(qualityMetrics.getFitness()+qualityMetrics.getPrecision());
			qualityMetrics.setFmeasure(fmeasure);
		}
		return qualityMetrics;
	}
}
