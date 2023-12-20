package software.processmining.multiviewinteraction;

import UtilityClasses.MethodClass;

public class Edge {
	MethodClass source;
	MethodClass target;
	public Edge(MethodClass s, MethodClass t)
	{
		source=s;
		target=t;
	}
	public boolean equals(Edge obj) {
		// TODO Auto-generated method stub
		if (source.equals(obj.source)&&target.equals(obj.target))
		{
			return true;
		}
		else 
		{
			return false;
		}
	}
}
