package sampling.performanceevaluation;

import java.util.HashSet;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;

import sampling.logrank.LogRankSamplingPlugin;
import sampling.sigrank.SigRankSamplingPlugin;
import sampling.simrank.SimRankSamplingPlugin;

@Plugin(
		name = "Sampling Performance Evaluation",// plugin name
		
		returnLabels = {"Sample Log"}, //return labels
		returnTypes = {XLog.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Large Event Log"},
		
		userAccessible = true,
		help = "This plugin aims to (1) evaluate the performance of different sampling techniques; "
				+ "and (2) return sample log, by taking an input large-scale example log." 
		)
public class PerformanceEvaluationPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl"
	        )
	@PluginVariant(
			variantLabel = "Sampling Evaluation, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	public static XLog SamplingEvaluation(UIPluginContext context, XLog originalLog) throws UserCancelledException
	{	
		//select two types of event logs, lifecycle event log and normal event log.  
		String [] samplingTechnique = new String[3];
		samplingTechnique[0]="SimRank-based Sampling";
		samplingTechnique[1]="SigRank-based Sampling";
		samplingTechnique[2]="LogRank-based Sampling";
		String selectedTechnique =ProMUIHelper.queryForObject(context, "Select the sampling techniques", samplingTechnique);
		context.log("The selected sampling technique is: "+selectedTechnique, MessageLevel.NORMAL);	
		System.out.println("Selected selected sampling technique is: "+selectedTechnique);
		
		//select two types of event logs, lifecycle event log and normal event log.  
		String [] logType = new String[2];
		logType[0]="Normal Event Log";
		logType[1]="Lifecycle Event Log";
		String selectedLogType =ProMUIHelper.queryForObject(context, "Select the type of event log for sampling", logType);
		context.log("The selected log type is: "+selectedLogType, MessageLevel.NORMAL);	
		System.out.println("Selected log type is: "+selectedLogType);
				
		
		//set the sampling ratio
		int queryRatio = ProMUIHelper.queryForInteger(context, "Typein the sampling ratios");		
		double samplingRatio =queryRatio/100.00;
		context.log("Sampling Ratio is: "+samplingRatio, MessageLevel.NORMAL);	
		
		XLog sampleLog =new XLogImpl(originalLog.getAttributes());
		HashSet<Long> performance = new HashSet<>();
		long startTime=0;
		long endTime=0;
		if(selectedTechnique.equals("SimRank-based Sampling"))
		{
			for(int i=0;i<5;i++)
			{
				if(i==0)
				{
					startTime=System.currentTimeMillis();
					sampleLog=SimRankSamplingPlugin.SimRankSamplingTechnique(originalLog, selectedLogType, samplingRatio);
					endTime =System.currentTimeMillis();
					performance.add(endTime-startTime);
				}
				else{
					startTime=System.currentTimeMillis();
					SimRankSamplingPlugin.SimRankSamplingTechnique(originalLog, selectedLogType, samplingRatio);
					endTime =System.currentTimeMillis();
					performance.add(endTime-startTime);
				}
				
			}
			System.out.println("SimRank-based Sampling, "+"Sampling Ration: "+ samplingRatio+" , "+performance);
		}
		
		if(selectedTechnique.equals("SigRank-based Sampling"))
		{
			for(int i=0;i<5;i++)
			{
				if(i==0)//output the sample log for the first iteration. 
				{
					startTime=System.currentTimeMillis();
					sampleLog=SigRankSamplingPlugin.SimRankSamplingTechnique(originalLog, samplingRatio);
					endTime =System.currentTimeMillis();
					performance.add(endTime-startTime);
				}
				else{
					startTime=System.currentTimeMillis();
					SigRankSamplingPlugin.SimRankSamplingTechnique(originalLog, samplingRatio);
					endTime =System.currentTimeMillis();
					performance.add(endTime-startTime);
				}
				
				
			}
			System.out.println("SigRank-based Sampling, "+"Sampling Ration: "+ samplingRatio+" , "+performance);
		}
		
		if(selectedTechnique.equals("LogRank-based Sampling"))
		{
			for(int i=0;i<5;i++)
			{
				if(i==0)//output the sample log for the first iteration. 
				{
					startTime=System.currentTimeMillis();
					sampleLog=LogRankSamplingPlugin.LogRankSamplingTechnique(originalLog, selectedLogType, samplingRatio);
					endTime =System.currentTimeMillis();
					performance.add(endTime-startTime);
				}
				else{
					startTime=System.currentTimeMillis();
					LogRankSamplingPlugin.LogRankSamplingTechnique(originalLog, selectedLogType, samplingRatio);
					endTime =System.currentTimeMillis();
					performance.add(endTime-startTime);
				}
				
				
			}
			System.out.println("LogRank-based Sampling, "+"Sampling Ration: "+ samplingRatio+" , "+performance);
		}
		
		return sampleLog;
	}
		
		
}
