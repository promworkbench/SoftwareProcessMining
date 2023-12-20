package software.processmining.componentidentification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;

import org.jgrapht.graph.DefaultWeightedEdge;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import UtilityClasses.ClassClass;
import UtilityClasses.ComponentConfig;
import UtilityFunctions.ExtractMethodClassPackage;
import software.processmining.classinteractiondiscovery.ClassInteractionGraph;

public class WrapperClassInteractionGraph2TabDelimitedFile {
	private HashMap<String, Integer> Class2ID = new HashMap<>();//from class to its id
	private HashMap<Integer, String> ID2Class = new HashMap<>();//from id to its class
	
	public HashMap<String, Integer> getClass2ID() {
		return Class2ID;
	}


	public void setClass2ID(HashMap<String, Integer> class2id) {
		Class2ID = class2id;
	}


	public WrapperClassInteractionGraph2TabDelimitedFile(ClassInteractionGraph cig) throws IOException
	{
		HashSet<String> allClasses = new HashSet<>();//all classes/nodes in the cig
		for(DefaultWeightedEdge edge:cig.getAllEdges())
		{			
			allClasses.add(cig.getClassInteractionGraph().getEdgeSource(edge));
			allClasses.add(cig.getClassInteractionGraph().getEdgeTarget(edge));
		}
	
		//map each class to id, starts from 0
		int id =0;
		for(String c: allClasses)
		{
			Class2ID.put(c, id);
			ID2Class.put(id, c);
			id++;
		}
		 
		//map the cig to a hashmap, we use multi map here. 
		
		Multimap<Integer,Integer> myMultimap = ArrayListMultimap.create();
		for(DefaultWeightedEdge edge:cig.getAllEdges())
		{
			myMultimap.put(Class2ID.get(cig.getClassInteractionGraph().getEdgeSource(edge)), 
					Class2ID.get(cig.getClassInteractionGraph().getEdgeTarget(edge)));
		}
				
		
		//create the input file. unweigeted and undirected. 
		BufferedWriter bufferedWriter;
        bufferedWriter = new BufferedWriter(new FileWriter("src\\software\\processmining\\componentidentification\\input_tab_delimited_text_file.txt"));

        for (int i = 0; i < allClasses.size(); i++)
        	for(int j = i+1; j<allClasses.size(); j++)   	
        {
        	if(myMultimap.containsEntry(i, j)||myMultimap.containsEntry(j, i))// we do not consider direction of edges
        	{
        		bufferedWriter.write(i +"\t"+j);
                bufferedWriter.newLine();
        	}
        }
        bufferedWriter.close();
	}
	
	public ComponentConfig constructComponentConfigFromCommunityResult(String outputFile, ComponentConfig comconfig) throws IOException
	{
		
		//mapping from component id to class id
		HashMap<Integer, HashSet<Integer>> componentID2ClassIDs = new HashMap<>();
		
		//read in the outputFile  
		FileInputStream fis = new FileInputStream(new File(outputFile));
		 
		//Construct BufferedReader from InputStreamReader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	 
		int lineNumber =0;
		String line = null;
		while ((line = br.readLine()) != null) {
			int currentComponentID = Integer.parseInt(line);
			if(componentID2ClassIDs.containsKey(currentComponentID))
			{
				componentID2ClassIDs.get(currentComponentID).add(lineNumber);//the line number is the class id. 
			}
			else{
				HashSet<Integer> tempSet = new HashSet<>();
				tempSet.add(lineNumber);
				componentID2ClassIDs.put(currentComponentID, tempSet);
			}
			lineNumber++;
		}
		br.close();
		
		//we get the class name from the id. 
		for(int componentID: componentID2ClassIDs.keySet())
		{
			HashSet<ClassClass> classes = new HashSet<>();

			for(int classID: componentID2ClassIDs.get(componentID))
			{
				ClassClass c = new ClassClass();
				c.setClassName(ExtractMethodClassPackage.getLast(ID2Class.get(classID)));
				c.setPackageName(ExtractMethodClassPackage.getFirstfrom2Parts(ID2Class.get(classID)));
				classes.add(c);
			}
			
			comconfig.add("Component"+componentID, classes);
		}		
	
		return comconfig;
	}
	
	
	
}
