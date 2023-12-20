package software.processmining.interfacesimilarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import UtilityClasses.MethodClass;
import software.processmining.interfacediscovery.InterfaceMergeUsingSimilarity;
import software.processmining.interfacediscoveryevaluation.InterfaceDescription;
import software.processmining.interfacediscoveryevaluation.Method2CallerMethods;

public class MergeInterfaceUseSimilarity {
	/*
	 * return the interface list by merging similar candidates
	 * oldInters is first assigned as empty and newInters is first as the candidates. 
	 */
	
	public static HashMap<HashSet<MethodClass>, InterfaceDescription> recursiveMergingInterfaces 
	(HashMap<HashSet<MethodClass>, InterfaceDescription> methodSet2interfaceDescription, 
			ArrayList<HashSet<MethodClass>> oldInters, 
			ArrayList<HashSet<MethodClass>> newInters, 
			double threshold)
	{		
		System.out.println("recursion");
		//System.out.println(methodSet2interfaceDescription);
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
				double sim =InterfaceMergeUsingSimilarity.similarityTwoInterfaceCandidate(oldInters.get(i),oldInters.get(j));
				System.out.println("minus"+(sim-threshold));

				if(sim-threshold>0)
				{
					//combine the ith and jth interface and add them to newInters.
					HashSet<MethodClass> mergeInterfaceMethodSet = new HashSet<MethodClass>();
					mergeInterfaceMethodSet.addAll(oldInters.get(i));
					mergeInterfaceMethodSet.addAll(oldInters.get(j));
					newInters.add(mergeInterfaceMethodSet);
					
					//combine the ith and jth interface descriptions and add to methodSet2interfaceDescription
					InterfaceDescription mergedInterfaceDescription = mergerInterfaceDescription(methodSet2interfaceDescription.get(oldInters.get(i)), 
							methodSet2interfaceDescription.get(oldInters.get(j)));
					
					//remove the merged interfaces from the methodSet to interface description mapping
					methodSet2interfaceDescription.remove(oldInters.get(i));
					methodSet2interfaceDescription.remove(oldInters.get(j));
					
					//add the new(merged) interface description 
					methodSet2interfaceDescription.put(mergeInterfaceMethodSet, mergedInterfaceDescription);
					
					// also add the rest candidates (except ith and jth) to the newInters
					ArrayList<HashSet<MethodClass>> tempInters =new ArrayList<>();
					tempInters.addAll(oldInters);
					tempInters.remove(oldInters.get(i));
					tempInters.remove(oldInters.get(j));

					newInters.addAll(tempInters);

					recursiveMergingInterfaces(methodSet2interfaceDescription, oldInters, newInters, threshold);
				}
			}
		}// the recursion will stop when the similarity of any two candidate interfaces is less than the threshold.  
		
		return methodSet2interfaceDescription;	
	}
	
	
	/*
	 * merge two interface descriptions. 
	 */
	public static InterfaceDescription mergerInterfaceDescription(InterfaceDescription interfaceDescription1, InterfaceDescription interfaceDescription2)
	{
		InterfaceDescription mergedInterface = new InterfaceDescription();
		
		//method2CallerMethodSet
		HashSet<Method2CallerMethods> mergedMethod2CallerMethodSet = new HashSet<>();
		mergedMethod2CallerMethodSet.addAll(interfaceDescription1.getMethod2CallerMethodSet());
		mergedMethod2CallerMethodSet.addAll(interfaceDescription2.getMethod2CallerMethodSet());
		mergedInterface.setMethod2CallerMethodSet(mergedMethod2CallerMethodSet);
		
		//callerComponentSet
		HashSet<String> mergedCallerComponentSet = new HashSet<>();
		mergedCallerComponentSet.addAll(interfaceDescription1.getCallerComponentSet());
		mergedCallerComponentSet.addAll(interfaceDescription2.getCallerComponentSet());
		mergedInterface.setCallerComponentSet(mergedCallerComponentSet);
		
		mergedInterface.generateCallerMethodSet();
		mergedInterface.generateMethodSet();
		
		return mergedInterface;
		
	}
	
}
