package software.designpattern.dynamicdiscovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import UtilityClasses.ClassClass;
import UtilityClasses.ClassTypeHierarchy;
import openXESsoftwareextension.XSoftwareExtension;

/**
 * this plug-in aims to discover the class type hierarchy information included in a software event log. 
 * The main idea is to identify a set of classes that have the same object. 
 * @author cliu3
 *
 */
@Plugin(
		name = "Class Type Hierarchy Discovery From Software Event Log",// plugin name
		
		returnLabels = {"ClassTypeHierarchy Patterns"}, //reture labels
		returnTypes = {ClassTypeHierarchy.class},//return class
		
		//input parameter labels
		parameterLabels = {"Software event log"},
		
		userAccessible = true,
		help = "This plugin aims to improve the Design Pattern results discovered from DPD tool." 
		)
public class DiscoverClassTypeHierarchyPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Class Type Hierarchy Discovery From Software Event Log, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	
	public ClassTypeHierarchy discoveryClassTypeHierarchyFromLog(UIPluginContext context, XLog softwareLog)
	{
		return discoveryClassTypeHierarchyNew(softwareLog);
	}
	/*
	 * the object id is not unique among different traces
	 * the same object refers to different classes in different trace. Needs to be improved. 
	 */
	public static ClassTypeHierarchy discoveryClassTypeHierarchyNew(XLog softwareLog)
	{
		ClassTypeHierarchy  cth = new ClassTypeHierarchy();
		
	
		for(XTrace trace : softwareLog)
		{
			//create a map from object id to its class set
			HashMap<String, HashSet<ClassClass>> object2ClassSet = new HashMap<>();
			
			//create a set that store all packages that has already exist in the caller and callee. 
			//then for parameters, only classes belongs to these packages will be considered. 
			HashSet<String> existingPackages = new HashSet<String>();
			for(XEvent event: trace)
			{
				//some special classes are not considered, (1) int, doubel...(2) anonymous inner class
				if(XSoftwareExtension.instance().extractClass(event).contains("$")||
						XSoftwareExtension.instance().extractCallerclass(event).contains("$"))
				{
					continue;
				}
				//for the callee object ==> callee package +callee class +callee method 
				ClassClass calleeC = new ClassClass();
				calleeC.setClassName(XSoftwareExtension.instance().extractClass(event));
				calleeC.setPackageName(XSoftwareExtension.instance().extractPackage(event));
				
				//the current object is not included in the map
				if(!XSoftwareExtension.instance().extractClassObject(event).equals("null")
						&&!XSoftwareExtension.instance().extractClassObject(event).equals("0"))//static object should not be considered. 
				{
					if(!object2ClassSet.containsKey(XSoftwareExtension.instance().extractClassObject(event)))
					{
						HashSet<ClassClass> tempH = new HashSet<>();
						tempH.add(calleeC);
						existingPackages.add(calleeC.getPackageName());
						object2ClassSet.put(XSoftwareExtension.instance().extractClassObject(event),tempH);
						
					}
					else{
						object2ClassSet.get(XSoftwareExtension.instance().extractClassObject(event)).add(calleeC);
						existingPackages.add(calleeC.getPackageName());
					}
				}
				
				
				//for the caller object==>caller package +caller class
				ClassClass callerC = new ClassClass();
				callerC.setClassName(XSoftwareExtension.instance().extractCallerclass(event));
				callerC.setPackageName(XSoftwareExtension.instance().extractCallerpackage(event));
				
				//the current object is not included in the map
				if(!XSoftwareExtension.instance().extractCallerclassobject(event).equals("null")
						&&!XSoftwareExtension.instance().extractCallerclassobject(event).equals("0"))//static method is not included
				{
					if(!object2ClassSet.containsKey(XSoftwareExtension.instance().extractCallerclassobject(event)))
					{
						HashSet<ClassClass> tempH = new HashSet<>();
						tempH.add(callerC);
						existingPackages.add(callerC.getPackageName());
						object2ClassSet.put(XSoftwareExtension.instance().extractCallerclassobject(event),tempH);
					}
					else{
						object2ClassSet.get(XSoftwareExtension.instance().extractCallerclassobject(event)).add(callerC);
						existingPackages.add(callerC.getPackageName());
					}
				}
			
			}
			
			for(XEvent event: trace)
			{
				HashMap<ClassClass,String> paras =constructParameterMapping(event);
				
				for(ClassClass c: paras.keySet())
				{
					//some anonymous inner classes are not considered
					//we only consider classes whose package is included in existingPackages
					if(!existingPackages.contains(c.getPackageName())||c.getClassName().contains("$"))
					{
						continue;
					}
					
					if(paras.get(c)!=null)
					{
						//the current object is not included in the map
						if(!object2ClassSet.containsKey(paras.get(c)))
						{
							HashSet<ClassClass> tempH = new HashSet<>();
							tempH.add(c);
							object2ClassSet.put(paras.get(c),tempH);
						}
						else{
							object2ClassSet.get(paras.get(c)).add(c);
						}
					}
					
				}
			}
			
			for(String o:object2ClassSet.keySet())
			{
//				System.out.println(o+"-->"+object2ClassSet.get(o));
				if(!o.contains("@"))
				{
//					System.out.println(o+":"+object2ClassSet.get(o));
					cth.addCTHbyMerging(object2ClassSet.get(o));// combine the hashset that share elements. 
				}
			}
			
		}

		

		

		return cth;
	}

	
	public static ClassTypeHierarchy discoveryClassTypeHierarchy(XLog softwareLog)
	{
		ClassTypeHierarchy  cth = new ClassTypeHierarchy();
		//create a map from object id to its class set
		HashMap<String, HashSet<ClassClass>> object2ClassSet = new HashMap<>();
		
		//create a set that store all packages that has already exist in the caller and callee. 
		//then for parameters, only classes belongs to these packages will be considered. 
		HashSet<String> existingPackages = new HashSet<String>();
	
		for(XTrace trace : softwareLog)
		{
			for(XEvent event: trace)
			{
				//some special classes are not considered, (1) int, doubel...(2) anonymous inner class
				if(XSoftwareExtension.instance().extractClass(event).contains("$")||
						XSoftwareExtension.instance().extractCallerclass(event).contains("$"))
				{
					continue;
				}
				//for the callee object ==> callee package +callee class +callee method 
				ClassClass calleeC = new ClassClass();
				calleeC.setClassName(XSoftwareExtension.instance().extractClass(event));
				calleeC.setPackageName(XSoftwareExtension.instance().extractPackage(event));
				
				//the current object is not included in the map
				if(!XSoftwareExtension.instance().extractClassObject(event).equals("null")
						&&!XSoftwareExtension.instance().extractClassObject(event).equals("0"))//static object should not be considered. 
				{
					if(!object2ClassSet.containsKey(XSoftwareExtension.instance().extractClassObject(event)))
					{
						HashSet<ClassClass> tempH = new HashSet<>();
						tempH.add(calleeC);
						existingPackages.add(calleeC.getPackageName());
						object2ClassSet.put(XSoftwareExtension.instance().extractClassObject(event),tempH);
						
					}
					else{
						object2ClassSet.get(XSoftwareExtension.instance().extractClassObject(event)).add(calleeC);
						existingPackages.add(calleeC.getPackageName());
					}
				}
				
				
				//for the caller object==>caller package +caller class
				ClassClass callerC = new ClassClass();
				callerC.setClassName(XSoftwareExtension.instance().extractCallerclass(event));
				callerC.setPackageName(XSoftwareExtension.instance().extractCallerpackage(event));
				
				//the current object is not included in the map
				if(!XSoftwareExtension.instance().extractCallerclassobject(event).equals("null")
						&&!XSoftwareExtension.instance().extractCallerclassobject(event).equals("0"))//static method is not included
				{
					if(!object2ClassSet.containsKey(XSoftwareExtension.instance().extractCallerclassobject(event)))
					{
						HashSet<ClassClass> tempH = new HashSet<>();
						tempH.add(callerC);
						existingPackages.add(callerC.getPackageName());
						object2ClassSet.put(XSoftwareExtension.instance().extractCallerclassobject(event),tempH);
					}
					else{
						object2ClassSet.get(XSoftwareExtension.instance().extractCallerclassobject(event)).add(callerC);
						existingPackages.add(callerC.getPackageName());
					}
				}
			}
			
		}

		for(XTrace trace : softwareLog)
		{
			for(XEvent event: trace)
			{
				HashMap<ClassClass,String> paras =constructParameterMapping(event);
				
				for(ClassClass c: paras.keySet())
				{
					//some anonymous inner classes are not considered
					//we only consider classes whose package is included in existingPackages
					if(!existingPackages.contains(c.getPackageName())||c.getClassName().contains("$"))
					{
						continue;
					}
					
					if(paras.get(c)!=null)
					{
						//the current object is not included in the map
						if(!object2ClassSet.containsKey(paras.get(c)))
						{
							HashSet<ClassClass> tempH = new HashSet<>();
							tempH.add(c);
							object2ClassSet.put(paras.get(c),tempH);
						}
						else{
							object2ClassSet.get(paras.get(c)).add(c);
						}
					}
					
				}
			}
		}			

		for(String o:object2ClassSet.keySet())
		{
			System.out.println(o+"-->"+object2ClassSet.get(o));
			if(!o.contains("@"))
			{
//				System.out.println(o+":"+object2ClassSet.get(o));
				cth.addCTHbyMerging(object2ClassSet.get(o));// combine the hashset that share elements. 
			}
		}

		return cth;
	}
	
	/*
	 * construct the mapping from parameter type (class) to object value (string)
	 */
	public static HashMap<ClassClass,String> constructParameterMapping(XEvent event)
	{
		
		HashMap<ClassClass,String> parameterType2Value = new HashMap<>();
		
		ArrayList<String> currentParameterTypeList = new ArrayList<String>();
		ArrayList<String> currentParameterValueList = new ArrayList<String>();
		
		String tempParaType = XSoftwareExtension.instance().extractParameterTypeSet(event);
		String tempParaValue = XSoftwareExtension.instance().extractParameterValueSet(event);

		if(tempParaType==null || tempParaValue==null)
		{
			return parameterType2Value; 
		}
		
		if(tempParaType.contains(","))// more than one parameters for the current method
		{
			for(String paraT: tempParaType.split("\\,"))
			{
				currentParameterTypeList.add(paraT);
			}
			if(tempParaValue!=null)
			{
				for(String paraV: tempParaValue.split("\\,"))
				{
					currentParameterValueList.add(paraV);
				}
			}
			else
			{
				for(int i =0;i<currentParameterTypeList.size();i++)
				{
					currentParameterValueList.add(null);
				}
			}
			
		}
		else
		{
			if (tempParaType.contains("."))// only one parameter for the current method
			{
				currentParameterTypeList.add(tempParaType);
				currentParameterValueList.add(tempParaValue);
			}
		}
		
		if(currentParameterTypeList.size()==0)//no parameter for the current method
		{
			return parameterType2Value;
		}
		else //one or more parameter for the current method
		{
			for(int i=0;i<currentParameterTypeList.size();i++)
			{
				ClassClass tempClass = new ClassClass();
				tempClass.setClassName(extractClass(currentParameterTypeList.get(i)));
				tempClass.setPackageName(extractPackage(currentParameterTypeList.get(i)));
				
				parameterType2Value.put(tempClass, currentParameterValueList.get(i));
			}
			return parameterType2Value;
		}
	}
	
	//get the class part, sample input "CH.ifa.draw.standard.StandardDrawingView"
	public static String extractClass(String s)
	{
		String args[]=s.split("\\.");	
		return args[args.length-1];
	}
	
	//get the package part sample input "CH.ifa.draw.standard.StandardDrawingView"
	public static String extractPackage(String s)
	{
		String args[]=s.split("\\.");	
		
		String Package = args[0];
		for (int i=1;i<args.length-1;i++)
		{
			Package=Package+"."+args[i];
		}
		return Package;
	}
	
}
