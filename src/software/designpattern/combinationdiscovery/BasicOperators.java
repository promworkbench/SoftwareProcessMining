package software.designpattern.combinationdiscovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import UtilityClasses.ClassClass;
import UtilityClasses.ClassTypeHierarchy;
import UtilityClasses.MethodClass;
import openXESsoftwareextension.XSoftwareExtension;
import software.designpattern.patterndefinition.AdapterPatternClass;
import software.designpattern.patterndefinition.CommandPatternClass;
import software.designpattern.patterndefinition.FactoryMethodPatternClass;
import software.designpattern.patterndefinition.ObserverPatternClass;
import software.designpattern.patterndefinition.PatternClass;
import software.designpattern.patterndefinition.SingletonPatternClass;
import software.designpattern.patterndefinition.StatePatternClass;
import software.designpattern.patterndefinition.StrategyPatternClass;
import software.designpattern.patterndefinition.VisitorPatternClass;

public class BasicOperators {
	
	//pattern instance construction, the statically detect roles are used. 
	public static PatternClass CreatePatternInstanceCombination(PatternClass op, HashMap<String, Object>  resulti, int logNum, int InvocationNum)
	{
		PatternClass pNew;
		if(op.getPatternName().equals("Observer Pattern"))
		{
			pNew = new ObserverPatternClass();
			pNew.setPatternName("Observer Pattern");
			pNew.setTraceNumber(logNum);
			pNew.setInvocationNumber(InvocationNum);
			
		
			//using the dynamic information
			((ObserverPatternClass)pNew).setSubjectClass(((ObserverPatternClass)op).getSubjectClass());
			((ObserverPatternClass)pNew).setListernerClass(((ObserverPatternClass)op).getListernerClass());
			((ObserverPatternClass)pNew).setNotifyMethod(((ObserverPatternClass)op).getNotifyMethod());
			
			//using the dynamic results
			((ObserverPatternClass)pNew).setUpdateMethod((MethodClass)(resulti.get("update")));
			((ObserverPatternClass)pNew).setRegisterMethod((MethodClass)(resulti.get("register")));
			((ObserverPatternClass)pNew).setDe_registerMethod((MethodClass)(resulti.get("unregister")));
			
			return pNew;
		}
		else if(op.getPatternName().equals("State Pattern"))
		{
			pNew = new StatePatternClass();
			pNew.setPatternName("State Pattern");
			pNew.setTraceNumber(logNum);
			pNew.setInvocationNumber(InvocationNum);
			
			//using the static result
			((StatePatternClass)pNew).setContext(((StatePatternClass)op).getContext());
			((StatePatternClass)pNew).setState(((StatePatternClass)op).getState());
			((StatePatternClass)pNew).setRequest(((StatePatternClass)op).getRequest());
			
			//using the dynamic results
			((StatePatternClass)pNew).setHandle((MethodClass)(resulti.get("handle")));
			((StatePatternClass)pNew).setSetState((MethodClass)(resulti.get("setState")));

			return pNew;
		}
		else if(op.getPatternName().equals("Strategy Pattern"))
		{
			pNew = new StrategyPatternClass();
			pNew.setPatternName("Strategy Pattern");
			pNew.setTraceNumber(logNum);
			pNew.setInvocationNumber(InvocationNum);
			
			//using the static result
			((StrategyPatternClass)pNew).setContext(((StrategyPatternClass)op).getContext());
			((StrategyPatternClass)pNew).setStrategy(((StrategyPatternClass)op).getStrategy());
			((StrategyPatternClass)pNew).setContextInterface(((StrategyPatternClass)op).getContextInterface());
			
			//using the dynamic results
			((StrategyPatternClass)pNew).setAlgorithmInterface((MethodClass)(resulti.get("algorithmInterface")));
			((StrategyPatternClass)pNew).setSetStrategy((MethodClass)(resulti.get("setStrategy")));

			return pNew;
		}
		else if((op.getPatternName().equals("(Object)Adapter Pattern")))
		{
			pNew = new AdapterPatternClass();
			pNew.setPatternName("(Object)Adapter Pattern");
			pNew.setTraceNumber(logNum);
			pNew.setInvocationNumber(InvocationNum);
			
			//using the static result
			((AdapterPatternClass)pNew).setAdapterClass(((AdapterPatternClass)op).getAdapterClass());
			((AdapterPatternClass)pNew).setAdapteeClass(((AdapterPatternClass)op).getAdapteeClass());
			((AdapterPatternClass)pNew).setRequestMethod(((AdapterPatternClass)op).getRequestMethod());
			
			//using the dynamic results
			((AdapterPatternClass)pNew).setSpecificRequestMethod((MethodClass)resulti.get("specificRequest"));

			return pNew;
		}
		else if((op.getPatternName().equals("Factory Method Pattern")))
		{
			pNew = new FactoryMethodPatternClass();
			pNew.setPatternName("Factory Method Pattern");
			pNew.setTraceNumber(logNum);
			pNew.setInvocationNumber(InvocationNum);
			
			//using the static result
			((FactoryMethodPatternClass)pNew).setCreator(((FactoryMethodPatternClass)op).getCreator());
			((FactoryMethodPatternClass)pNew).setFactoryMethod(((FactoryMethodPatternClass)op).getFactoryMethod());

			return pNew;
		}
		else if((op.getPatternName().equals("Command Pattern")))
		{
			pNew = new CommandPatternClass();
			pNew.setPatternName("Command Pattern");
			pNew.setTraceNumber(logNum);
			pNew.setInvocationNumber(InvocationNum);
			
			//using the static result
			((CommandPatternClass)pNew).setCommand(((CommandPatternClass)op).getCommand());
			((CommandPatternClass)pNew).setReceiver(((CommandPatternClass)op).getReceiver());
			((CommandPatternClass)pNew).setExecute(((CommandPatternClass)op).getExecute());
			
			//using the dynamic results
			((CommandPatternClass)pNew).setInvoker((ClassClass)resulti.get("Invoker"));
			((CommandPatternClass)pNew).setCall((MethodClass)resulti.get("call"));
			((CommandPatternClass)pNew).setAction((MethodClass)resulti.get("action"));
			
			return pNew;
		}
		else if((op.getPatternName().equals("Visitor Pattern")))
		{
			pNew = new VisitorPatternClass();
			pNew.setPatternName("Visitor Pattern");
			pNew.setTraceNumber(logNum);
			pNew.setInvocationNumber(InvocationNum);
			
			//using the static result
			((VisitorPatternClass)pNew).setElement(((VisitorPatternClass)op).getElement());
			((VisitorPatternClass)pNew).setVisitor(((VisitorPatternClass)op).getVisitor());
			((VisitorPatternClass)pNew).setAccept(((VisitorPatternClass)op).getAccept());
			
			//using the dynamic results
			((VisitorPatternClass)pNew).setVisit((MethodClass)resulti.get("visit"));
			
			return pNew;
		}
		else if(op.getPatternName().equals("Singleton Pattern"))
		{
			pNew = new SingletonPatternClass();
			pNew.setPatternName("Singleton Pattern");
			pNew.setTraceNumber(logNum);
			pNew.setInvocationNumber(InvocationNum);
			
			//using the static result
			((SingletonPatternClass)pNew).setSingletonClass(((SingletonPatternClass)op).getSingletonClass());
			//using the dynamic results
			((SingletonPatternClass)pNew).setGetInstanceMethod((MethodClass)resulti.get("getInstance"));
			
			return pNew;
		}
		
		return null;
		//else patterns
	}
	/*
	 * check if a class is included in the log
	 */
	public static boolean classIncludedInLog(ClassClass c, XLog log)
	{
		if(c==null)
		{
			return false;
		}
		for(XTrace trace: log)
		{
			for(XEvent event: trace)
			{
				if(c.toString().equals(XSoftwareExtension.instance().extractPackage(event)+
						"."+XSoftwareExtension.instance().extractClass(event)))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * get the typehierarchyClass
	 * @param cth
	 * @param log
	 * @param c: the class that is not included in the log
	 * @return a set of classes
	 */
	public static HashSet<ClassClass> typeHierarchyClassSetInLog(ClassTypeHierarchy cth, XLog log, ClassClass c)
	{
		ArrayList<HashSet<ClassClass>> classTypeHierarchyList = cth.getAllCTH();
		HashSet<ClassClass> classSet = new HashSet<>();
		
		for(HashSet<ClassClass> cc: classTypeHierarchyList)
		{
			if(cc.contains(c))
			{
				for(ClassClass candidateC: cc)
				{
					if(classIncludedInLog(candidateC,log))
					{
						classSet.add(candidateC);
					}
				}
			}
		}
		
		return classSet;
	}
	
	/*
	 * check is a method is included in the log
	 */
	public static boolean methodIncludedInLog(MethodClass m, XLog log)
	{
		if(m==null)
		{
			return false;
		}
		for(XTrace trace: log)
		{
			for(XEvent event: trace)
			{
				if(m.toString().equals(XSoftwareExtension.instance().extractPackage(event)+
						"."+XSoftwareExtension.instance().extractClass(event)+"."+XConceptExtension.instance().extractName(event)))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param cth
	 * @param log
	 * @param m: the method that is not included in the log
	 * @return a set of methods
	 */
	public static HashSet<MethodClass> typeHierarchyMethodSetInLog(ClassTypeHierarchy cth, XLog log, MethodClass m)
	{
		ArrayList<HashSet<ClassClass>> classTypeHierarchyList = cth.getAllCTH();
		HashSet<MethodClass> candidateMethodSet = new HashSet<>();
		
		for(HashSet<ClassClass> cc: classTypeHierarchyList)
		{
			if(cc.contains(MethodToClass(m)))
			{
				for(ClassClass candidateC: cc)
				{
					MethodClass candidateM = ClassToMethod(candidateC);
					candidateM.setMethodName(m.getMethodName());
					if(methodIncludedInLog(candidateM,log))
					{
						candidateMethodSet.add(candidateM);
					}
				}
			}
		}
		
		return candidateMethodSet;
	}
	
	public static ClassClass MethodToClass(MethodClass m)
	{
		ClassClass c = new ClassClass();
		c.setClassName(m.getClassName());
		c.setPackageName(m.getPackageName());
		
		return c;
	}
	
	public static MethodClass ClassToMethod(ClassClass c)
	{
		MethodClass m = new MethodClass();
		m.setClassName(c.getClassName());
		m.setPackageName(c.getPackageName());
		
		return m;
	}
	
	
	/*
	 * Given a class, return the set of Methods according to the log. 
	 */
	public static HashSet<MethodClass> MethodSetofClass(ClassClass cc, XLog log)
	{	
		HashSet<MethodClass> methodSet = new HashSet<>();
		for(XTrace trace: log)
		{
			for(XEvent event: trace)
			{
				if(XSoftwareExtension.instance().extractClass(event).equals(cc.getClassName())&&
						XSoftwareExtension.instance().extractPackage(event).equals(cc.getPackageName()))
				{
					MethodClass currentMethod = new MethodClass();
					currentMethod.setMethodName(XConceptExtension.instance().extractName(event));
					currentMethod.setClassName(XSoftwareExtension.instance().extractClass(event));
					currentMethod.setPackageName(XSoftwareExtension.instance().extractPackage(event));
					//currentMethod.setParameterSet(XSoftwareExtension.instance().extractParameterTypeSet(event));
					//currentMethod.setLineNumber(XSoftwareExtension.instance().extractLineNumber(event));
					methodSet.add(currentMethod);
				}				
			}
		}
		return methodSet;
	}
	
	/*
	 * Given a method return its invoked method set according to the log. 
	 */
	
	public static HashSet<MethodClass> MethodSetofMethod(MethodClass m, XLog log)
	{
		HashSet<MethodClass> methodSet = new HashSet<>();
		
		for(XTrace trace: log)
		{
			for(XEvent event: trace)
			{
				if(XSoftwareExtension.instance().extractCallerpackage(event).equals(m.getPackageName())&&
						XSoftwareExtension.instance().extractCallerclass(event).equals(m.getClassName())&&
						XSoftwareExtension.instance().extractCallermethod(event).equals(m.getMethodName()))
				{
					MethodClass tempMethod = new MethodClass();
					tempMethod.setPackageName(XSoftwareExtension.instance().extractPackage(event));
					tempMethod.setClassName(XSoftwareExtension.instance().extractClass(event));
					tempMethod.setMethodName(XConceptExtension.instance().extractName(event));
					
					methodSet.add(tempMethod);
				}
			}
		}
		
		return methodSet;
	}
	
	/*
	 * Given a method, return the parameter set according to the log. Essentially it is a set of classes
	 */
	public static HashSet<ClassClass> ParameterSetofMethod(MethodClass m, XLog log)
	{
		HashSet<ClassClass> parameterSet = new HashSet<>();
		
		for(XTrace trace: log)
		{
			for(XEvent event: trace)
			{
				if(XSoftwareExtension.instance().extractClass(event).equals(m.getClassName())&&
						XConceptExtension.instance().extractName(event).equals(m.getMethodName()))
				{
					Set<String> currentParameterSet = new HashSet<String>();
					
					String tempPara = XSoftwareExtension.instance().extractParameterTypeSet(event);
					//System.out.println(tempPara);
					if(tempPara.contains(","))
					{
						for(String para: tempPara.split("\\,"))
						{
							currentParameterSet.add(para);
						}
					}
					else
					{
						if (tempPara.contains("."))
						{
							currentParameterSet.add(tempPara);
						}
						
					}
					
					for(String para:currentParameterSet)
					{
						ClassClass tempClass = new ClassClass();
						tempClass.setClassName(extractClass(para));
						tempClass.setPackageName(extractPackage(para));
						parameterSet.add(tempClass);
					}
					return parameterSet;// return the once get 
				}
				
			}
		}
		
		return parameterSet;
	}
	
	/*
	 * Get the return value set of a method
	 */
	public static HashSet<String> getReturnValueSet(XTrace invocation, MethodClass method)
	{
		HashSet<String> retureValueSet = new HashSet<>();
		
		for(XEvent event: invocation)
		{
			if(method.toString().equals(XSoftwareExtension.instance().extractPackage(event)+"."+
					XSoftwareExtension.instance().extractClass(event)+"."+XConceptExtension.instance().extractName(event)))
			{
				retureValueSet.add(XSoftwareExtension.instance().extractReturnValue(event));
			}
		}
		
		retureValueSet.remove("0");
		return retureValueSet;
	}
	
	/*
	 * get the callee class object of a class
	 */
	
	public static HashSet<String> getCalleeObjectSet(XTrace invocation, ClassClass c) 
	{
		HashSet<String > calleeObjectSet = new HashSet<>();
		
		for(XEvent event: invocation){
			if(c.toString().equals(XSoftwareExtension.instance().extractPackage(event)+"."+
					XSoftwareExtension.instance().extractClass(event)))
			{
				calleeObjectSet.add(XSoftwareExtension.instance().extractClassObject(event));
			}
		}
		
		calleeObjectSet.remove("0");
		return calleeObjectSet;
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
