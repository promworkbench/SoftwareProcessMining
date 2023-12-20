package crossorganization.processmining;

import java.util.HashSet;

//this class defines the cross-organization interaction
//e.g., a, b->c each interaction refers to a places. 
public class CrossOrganizationInteraction {
	
	private HashSet<OrgActivity> sourceActivities;
	private HashSet<OrgActivity> targetActivities;
	
	public CrossOrganizationInteraction(HashSet<OrgActivity> source, HashSet<OrgActivity> target)
	{
		sourceActivities=source;
		targetActivities=target;
	}

	public HashSet<OrgActivity> getSourceActivities() {
		return sourceActivities;
	}

	public void setSourceActivity(HashSet<OrgActivity> sourceActivities) {
		this.sourceActivities = sourceActivities;
	}

	public HashSet<OrgActivity> getTargetActivities() {
		return targetActivities;
	}

	public void setTargetActivity(HashSet<OrgActivity> targetActivities) {
		this.targetActivities = targetActivities;
	}
	
	public int hashCode() {
		int sourceHashcode = 0;
		int targetHashcode = 0;
		for(OrgActivity orgA: sourceActivities)
		{
			sourceHashcode=sourceHashcode+orgA.hashCode();
		}
		for(OrgActivity orgB: targetActivities)
		{
			targetHashcode=targetHashcode+orgB.hashCode();
		}
        return sourceHashcode*2+targetHashcode*3;
    }  
	
	
	public boolean equals(Object other)
	{
		if (this==other)
		{
			return true;
		}
		if (other==null)
		{
			return false;
		}
		if (!(other instanceof CrossOrganizationInteraction))
		{
			return false;
		}
		if (this.hashCode()==((CrossOrganizationInteraction)other).hashCode())  
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public String toString() 
	{
		return this.sourceActivities.toString()+":"+this.targetActivities.toString();
		
	}
}
