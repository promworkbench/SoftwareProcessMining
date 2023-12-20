package software.processmining.interfacediscoveryevaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import UtilityClasses.ClassClass;
import UtilityClasses.ComponentConfig;
import UtilityClasses.MethodClass;
import software.processmining.interfacediscovery.InterfaceMergeUsingSimilarity;

/**
 * this plugin aims to evaluate the quality of an interface discovery approach. 
 * It takes the output of "Integrated Software Interface Discovery Tool" plugin as input 
 *
 * Use the following three metrics:
 * Metric1: number of interfaces per component; NOI, we normalize them. 
 * Metric2: The proportion of methods that not used by the caller component; POM
 * Metric3: the number of caller methods per interface; NOC
 * Metric4: the number of methods per interface; NOM
 * 
 * @author cliu3 2017-4-13
 *
**/

@Plugin(
		name = "Interface Quality Evaluation Tool",// plugin name
		
		returnLabels = {"Quality Description"}, //return labels
		returnTypes = {String.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Description", "Component Configuration"},
		
		userAccessible = true,
		help = "This plugin aims to evaluate the quality of the discovered interfaces." 
		)
public class InterfaceQualityEvaluationPlugin {

	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Discovering interface, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0, 1}
			)
	public String qualityEvaluation(UIPluginContext context, SoftwareDescription softwareDescription, ComponentConfig comconfig)
	{
		//transform the format of component configuration file
		HashMap<String, HashSet<String>> component2ClassSet = new HashMap<>();
		for(String com: comconfig.getAllComponents())
		{
			HashSet<String> classSet = new HashSet<>();//class set of the current component
			for(ClassClass c: comconfig.getClasses(com))
			{
				classSet.add(c.getPackageName()+"."+c.getClassName());// both the package and class names are used to describe each class
			}
			component2ClassSet.put(com, classSet);
		}
				
		StringBuffer buffer = new StringBuffer();
		buffer.append("<html>"); 
		buffer.append("<body");// style=\"background-color:C6FFF8;\">
		buffer.append("<h1 style=\"color:blue;\">"+"Interface Quality Metrics</h1>"); //
		buffer.append("<table bgcolor=\"#979A9A\">");
		//for each component
		for(ComponentDescription cd: softwareDescription.getComponentSet())
		{
			buffer.append("<tr><th style=\"color:green;\" style=\"font-size:120%;\">"+cd.getComponentName()+"</th></tr>");
			System.out.println("-------------------------------------------------------------Component name: "+cd.getComponentName());
			//compute the number of interface of this component
			System.out.println("the number of interface per component " +" is: "+computeNOI(cd));
			
			buffer.append("<tr><td></td><td style=\"font-size:120%;\">"+"NOI"+"</td><td style=\"font-size:120%;\">"+computeNOI(cd)+"</td></tr>");
			
			buffer.append("<tr><td></td><td style=\"font-size:120%;\">"+"ANM"+"</td><td style=\"font-size:120%;\">"+computeANM(cd)+"</td></tr>");

			//compute the portion of methods that not used by the caller component, use the average for the component
			System.out.println("the portion of methods that are used by the caller component "+ "is: "+computePMC(cd, component2ClassSet));
			buffer.append("<tr><td></td><td style=\"font-size:120%;\">"+"PMC"+"</td><td style=\"font-size:120%;\">"+computePMC(cd, component2ClassSet)+"</td></tr>");
			buffer.append("<tr><td></td><td style=\"font-size:120%;\">"+"PMM"+"</td><td style=\"font-size:120%;\">"+computePMM(cd)+"</td></tr>");

			buffer.append("<tr><td></td><td style=\"font-size:120%;\">"+"PCC"+"</td><td style=\"font-size:120%;\">"+computePCC(cd, component2ClassSet)+"</td></tr>");
			buffer.append("<tr><td></td><td style=\"font-size:120%;\">"+"PCM"+"</td><td style=\"font-size:120%;\">"+computePCM(cd)+"</td></tr>");

			buffer.append("<tr><td></td><td style=\"font-size:120%;\">"+"PM"+"</td><td style=\"font-size:120%;\">"+computePM(computePCM(cd), computePMM(cd))+"</td></tr>");
			buffer.append("<tr><td></td><td style=\"font-size:120%;\">"+"PC"+"</td><td style=\"font-size:120%;\">"+computePC(computePMC(cd, component2ClassSet), computePCC(cd, component2ClassSet))+"</td></tr>");
			
			buffer.append("<tr><td></td><td style=\"font-size:120%;\">"+"P"+"</td><td style=\"font-size:120%;\">"+computeP(computePM(computePCM(cd), computePMM(cd)), computePC(computePMC(cd, component2ClassSet), computePCC(cd, component2ClassSet)))+"</td></tr>");
		
			//duplication ratio of component 
			buffer.append("<tr><td></td><td style=\"font-size:120%;\">"+"Duplication Ratio"+"</td><td style=\"font-size:120%;\">"+computeDuplicationRatio(cd)+"</td></tr>");

		}
		buffer.append("</table>");
		buffer.append("</body>");
		buffer.append("</html>");
		return buffer.toString();
	}
	

//	// return the number of method of this component
//	public static int computeMethodNumber(ComponentDescription cd)
//	{
//		HashSet<MethodClass> methodSet = new HashSet<>();
//		for(InterfaceDescription id: cd.getInterfaceSet())
//		{
//			methodSet.addAll(id.getMethodSet());
//		}
//		return methodSet.size();
//	}
	
	//NOI metric, i.e., number of interfaces per component
	public static int computeNOI(ComponentDescription cd)
	{
		return cd.getInterfaceSet().size();
	}
	
	
	//NOM metric, i.e., the average number of methods per interface
	public static double computeANM(ComponentDescription cd)
	{
		double numberofMethod = 0;
		for(InterfaceDescription id: cd.getInterfaceSet())
		{
			numberofMethod=numberofMethod+id.getMethodSet().size();
		}
		return numberofMethod/cd.getInterfaceSet().size();
	}
	
	//
	public static double computeDuplicationRatio(ComponentDescription cd)
	{
		//if the number of interfaces of the current component is less than 1
		if(cd.getInterfaceSet().size()<=1)
			return 0;
		
		double totalDuplication = 0;
		for(InterfaceDescription i1: cd.getInterfaceSet())
		{
			for(InterfaceDescription i2: cd.getInterfaceSet())
			{
				if(!i1.equals(i2))//if i1 and i2 are different interfaces. 
				{
					totalDuplication = totalDuplication+InterfaceMergeUsingSimilarity.similarityTwoInterfaceCandidate(i1.getMethodSet(), i2.getMethodSet());
				}
			}
		}
		
		//the average duplication ratio
		return totalDuplication/(cd.getInterfaceSet().size()*(cd.getInterfaceSet().size()-1));
	}
	
	//PM metric, i.e., the harmonic mean of PCM and PMM
	public static double computePM(double PCM, double PMM)
	{
		return (2*PCM*PMM)/(PCM+PMM);
	}
	
	//PC metric, i.e., the harmonic mean of PCC and PMC
	public static double computePC(double PMC, double PCC)
	{
		return (2*PMC*PCC)/(PMC+PCC);
	}

	public static double computeP(double PM, double PC)
	{
		return (2*PM*PC)/(PM+PC);
	}
	
	//PMC metric, i.e., the portion of methods that are used by the caller component, use the average for the component
	public static double computePMC(ComponentDescription component, HashMap<String, HashSet<String>> component2ClassSet)
	{
		double averagePerInterface =0;
		//for each interface, we get the caller component. 
		for(InterfaceDescription inter: component.getInterfaceSet())
		{
			int countPerInterface =0;
			//for each caller component of this interface, we count the number of method that are not used by this component
			for(String callerComponent: inter.getCallerComponentSet())
			{
				int countPerCallerComponent =0;
				for(Method2CallerMethods m2m: inter.getMethod2CallerMethodSet())
				{
					if(!checkingComponentCallingRelation(m2m, component2ClassSet.get(callerComponent)))
					{
						countPerCallerComponent++;
					}
				}
				countPerInterface=countPerInterface+countPerCallerComponent;
			}
			//compute the average per interface
			averagePerInterface = averagePerInterface+ (double)countPerInterface/(double)(inter.getCallerComponentSet().size()*inter.getMethod2CallerMethodSet().size());
		}
		
		//compute the average for the component
		return 1- averagePerInterface/component.getInterfaceSet().size();
	}
	
	//PMM metric, i.e., the portion of methods that are used by the caller method, use the average for the component
	public static double computePMM(ComponentDescription component)
	{
		double averagePerInterface =0;
		//for each interface, we get the caller component. 
		for(InterfaceDescription inter: component.getInterfaceSet())
		{
			int countPerInterface =0;
			//for each caller component of this interface, we count the number of method that are not used by this component
			for(MethodClass callerMethod: inter.getCallerMethodSet())
			{
				int countPerCallerMethod =0;
				for(Method2CallerMethods m2m: inter.getMethod2CallerMethodSet())
				{
					if(!m2m.getCallerMethodSet().contains(callerMethod))
					{
						countPerCallerMethod++;
					}
				}
				countPerInterface=countPerInterface+countPerCallerMethod;
			}
			//compute the average per interface
			averagePerInterface = averagePerInterface+ (double)countPerInterface/(double)(inter.getCallerMethodSet().size()*inter.getMethod2CallerMethodSet().size());
		}
		
		//compute the average for the component
		return 1-averagePerInterface/component.getInterfaceSet().size();
	}
	
	//PCM metric, proportion of methods that are correctly grouped as interfaces according to the caller method 
	public static double computePCM(ComponentDescription component)
	{				
		double proportion =0;
		
		//get the caller method set of the component. 
		HashSet<MethodClass> callerMethodSet =getCallerMethodSet(component);
		for(MethodClass callerMethod: callerMethodSet)//for each caller method
		{
			ArrayList<Integer> arrayList = new ArrayList<Integer>();// record the number of methods that are called by method in each interface
			int countMethodInterface =0;// interfaces that are not caller by the current method
			for(InterfaceDescription inter: component.getInterfaceSet())
			{
				if(inter.getCallerMethodSet().contains(callerMethod))
				{
					//we get the method that are called by the method
					arrayList.add(getMethodsCalledbyMethod(inter, callerMethod).size());
					countMethodInterface=countMethodInterface+getMethodsCalledbyMethod(inter, callerMethod).size();
				}
			}
			proportion=proportion+ Collections.max(arrayList)/(double) countMethodInterface;
		}
		return proportion/callerMethodSet.size();
	}
	
	//PCC metric, proportion of methods that are correctly grouped as interfaces according to the caller component 
	public static double computePCC(ComponentDescription component, HashMap<String, HashSet<String>> component2ClassSet)
	{
		double proportion =0;
		
		//get the caller method set of the component. 
		HashSet<String> callerComponentSet =getCallerComponentSet(component);
		for(String callerComponent: callerComponentSet)//for each caller component
		{
			ArrayList<Integer> arrayList = new ArrayList<Integer>();// record the number of methods that are called by method in each interface
			int countMethodInterface =0;// interfaces that are not caller by the current component
			for(InterfaceDescription inter: component.getInterfaceSet())
			{
				if(inter.getCallerComponentSet().contains(callerComponent))
				{
					//we get the method that are called by the component
					arrayList.add(getMethodsCalledbyComponent(inter, component2ClassSet.get(callerComponent)).size());
					countMethodInterface=countMethodInterface+getMethodsCalledbyComponent(inter, component2ClassSet.get(callerComponent)).size();
				}
			}
			proportion=proportion+ Collections.max(arrayList)/(double)countMethodInterface;
		}
		return proportion/callerComponentSet.size();
	}
	
	// return the caller method set of this component
	public static HashSet<MethodClass> getCallerMethodSet(ComponentDescription cd)
	{
		HashSet<MethodClass> callerMethodSet = new HashSet<>();
		for(InterfaceDescription id: cd.getInterfaceSet())
		{
			callerMethodSet.addAll(id.getCallerMethodSet());
		}
		
		return callerMethodSet;
	}
	
	// return the caller component set of this component
	public static HashSet<String> getCallerComponentSet(ComponentDescription cd)
	{
		HashSet<String> callerComponentSet = new HashSet<>();
		for(InterfaceDescription id: cd.getInterfaceSet())
		{
			callerComponentSet.addAll(id.getCallerComponentSet());
		}
		
		return callerComponentSet;
	}
	
	//return the methodset that are called by a method
	public static HashSet<MethodClass> getMethodsCalledbyMethod(InterfaceDescription id, MethodClass callerMethod)
	{
		HashSet< MethodClass> methodSet = new HashSet<>();

		for(Method2CallerMethods m2m: id.getMethod2CallerMethodSet())
		{
			if(m2m.getCallerMethodSet().contains(callerMethod))
			{
				methodSet.add(m2m.getMethod());
			}
		}
		return methodSet;
	}
	
	//return the merthodset that are called by a component
	public static HashSet<MethodClass> getMethodsCalledbyComponent(InterfaceDescription id, HashSet<String> classSet)	
	{
		HashSet< MethodClass> methodSet = new HashSet<>();

		for(Method2CallerMethods m2m: id.getMethod2CallerMethodSet())
		{
			if(checkingComponentCallingRelation(m2m, classSet))
			{
				methodSet.add(m2m.getMethod());
			}
		}
		
		return methodSet;
	}
	
	//check if a method is called by its caller component. 
	/**
	 * 
	 * @param m2m
	 * @param classSet of the caller component
	 * @return
	 */
	public static boolean checkingComponentCallingRelation(Method2CallerMethods m2m, HashSet<String> classSet) 
	{
		for(MethodClass callerMethod: m2m.getCallerMethodSet())
		{
			if(classSet.contains(callerMethod.getPackageName()+"."+callerMethod.getClassName()))
			{
				return true;
			}
		}
		
		return false;
	}
		
	
}
