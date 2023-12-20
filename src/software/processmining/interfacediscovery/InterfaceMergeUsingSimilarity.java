package software.processmining.interfacediscovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import UtilityClasses.MethodClass;

public class InterfaceMergeUsingSimilarity {
	
	/*
	 * return the interface list by merging similar candidates
	 * oldInters is first assigned as empty and newInters is first as the candidates. 
	 */
	
	public static HashMap<HashSet<MethodClass>, HashSet<MethodClass>> recursiveComputing 
	(HashMap<HashSet<MethodClass>, HashSet<MethodClass>> interface2callerSet, 
			ArrayList<HashSet<MethodClass>> oldInters, 
			ArrayList<HashSet<MethodClass>> newInters, 
			double threshold)
	{		
		System.out.println("recursion");
		System.out.println(interface2callerSet);
		System.out.println("old interface: "+oldInters);
		System.out.println("new interface: "+newInters);


		oldInters.clear();
		oldInters.addAll(newInters);
		newInters.clear();
		System.out.println("old interface: "+oldInters);
		System.out.println("new interface: "+newInters);	
		for(int i=0;i<oldInters.size();i++)
		{
			for(int j=i+1; j<oldInters.size();j++)
			{
				System.out.println("i="+i+",j="+j);
				//if the similarity is greater than the threshold, them merge them and add to new list
				double sim =similarityTwoInterfaceCandidate(oldInters.get(i),oldInters.get(j));
				System.out.println("minus"+(sim-threshold));

				if(sim-threshold>0)
				{
					//combine the ith and jth interface and add them to newInters.
					HashSet<MethodClass> mergeInterface = new HashSet<MethodClass>();
					mergeInterface.addAll(oldInters.get(i));
					mergeInterface.addAll(oldInters.get(j));
					newInters.add(mergeInterface);
					
					//combine the ith and jth caller method set and add to interface2callerset
					HashSet<MethodClass> mergeCaller = new HashSet<>();
					mergeCaller.addAll(interface2callerSet.get(oldInters.get(i)));
					mergeCaller.addAll(interface2callerSet.get(oldInters.get(j)));
					
					//remove the merged interface from the interface to caller mapping
					interface2callerSet.remove(oldInters.get(i));
					interface2callerSet.remove(oldInters.get(j));
					
//						System.out.println("simi satisfied merge interface: "+mergeInterface);
//						System.out.println("simi satisfied merge caller: "+mergeCaller);
					interface2callerSet.put(mergeInterface, mergeCaller);
					
					// also add the rest candidates (except ith and jth) to the newInters
					ArrayList<HashSet<MethodClass>> tempInters =new ArrayList<>();
					tempInters.addAll(oldInters);
					tempInters.remove(oldInters.get(i));
					tempInters.remove(oldInters.get(j));

					newInters.addAll(tempInters);
//						System.out.println("simi satisfied old interface: "+oldInters);
//						System.out.println("simi satisfied new interface: "+newInters);
//						System.out.println("simi satisfied mapping: "+interface2callerSet);
					recursiveComputing(interface2callerSet, oldInters, newInters, threshold);
				}
			}
		}// the recursion will stop when the similarity of any two candidate interfaces is less than the threshold.  
		
		return interface2callerSet;	
	}
	
	/*
	 * compute the similarity of two interface candidate
	 */
	public static double similarityTwoInterfaceCandidate(HashSet<MethodClass> group1, HashSet<MethodClass> group2)
	{	
		double sim= (double)interactionNumber(group1, group2)/(double)unionNumber(group1,group2);
		return sim;
	}
	
	/*
	 * the union number of elements of two hashset
	 */
	
	public static int unionNumber(HashSet<MethodClass> group1, HashSet<MethodClass> group2)
	{
		HashSet<MethodClass> temp1 = new HashSet<MethodClass>();
		temp1.addAll(group1);
		temp1.addAll(group2);
		return temp1.size();		
	}
	/*
	 * the interact number of elements  of two hashset
	 */
	public static int interactionNumber(HashSet<MethodClass> group1, HashSet<MethodClass> group2)
	{
		HashSet<MethodClass> temp1 = new HashSet<MethodClass>();
		temp1.addAll(group1);
		temp1.retainAll(group2);
		
		return temp1.size();
	}
}
