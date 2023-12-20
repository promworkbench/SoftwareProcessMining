package crossorganization.processmining;

import java.util.HashMap;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import UtilityFunctions.InitializeEventLog;

public class OrganizationLogConstruction {

	/*
	 * this class construct the event log for each organization. 
	 */
	public static HashMap<String, XLog> contructOrganizationLog(XLog originalLog, OrganizationConfig orgConfig) 
	{
		HashMap<String, XLog> org2Log = new HashMap<>();
		XFactory factory = new XFactoryNaiveImpl();
		for(String org: orgConfig.getAllOrganizations())
		{
			//the activity set of the current organization
			Set<String> activitySet = orgConfig.getActivities(org);
			XLog orgLog = InitializeEventLog.initializeEventLog(factory, org);
			for(XTrace trace: originalLog)
			{
				XTrace orgTrace = factory.createTrace();
				XConceptExtension.instance().assignName(orgTrace, XConceptExtension.instance().extractName(trace));//assign the original name
				for(XEvent event: trace)
				{
					if(activitySet.contains(XConceptExtension.instance().extractName(event)))//if the activity belong to the org
					{
						orgTrace.insertOrdered(event);
					}
				}
				if(orgTrace.size()!=0)
				{
					orgLog.add(orgTrace);
				}
			}
			if(orgLog.size()!=0)
			{
				org2Log.put(org, orgLog);
			}
		}
		return org2Log;
		
	}
}
