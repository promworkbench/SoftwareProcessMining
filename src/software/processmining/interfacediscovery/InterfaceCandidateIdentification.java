package software.processmining.interfacediscovery;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import UtilityClasses.MethodClass;
import openXESsoftwareextension.XSoftwareExtension;

public class InterfaceCandidateIdentification {
	/*
	 * Given a component event log, return its caller method set. 
	 * The caller methods set are those whose caller do not belong to the current classSet.
	 */
	public static HashSet<MethodClass> constructCallerMethodSet(Set<String> classSet, XLog componentLog)
	{
		HashSet<MethodClass> callerMethodSet = new HashSet<>();
		
		for(XTrace trace: componentLog)
		{
			for(XEvent event: trace)
			{
				if(!classSet.contains(XSoftwareExtension.instance().extractCallerpackage(event)+"."+XSoftwareExtension.instance().extractCallerclass(event)))
				{
					MethodClass m =new MethodClass();
					m.setMethodName(XSoftwareExtension.instance().extractCallermethod(event));
					m.setClassName(XSoftwareExtension.instance().extractCallerclass(event));
					m.setPackageName(XSoftwareExtension.instance().extractCallerpackage(event));
					if(!callerMethodSet.contains(m))
					{
						callerMethodSet.add(m);
					}
				}				
			}
		}
		
		return callerMethodSet;
	}
	
	/*
	 * Given a component event log and a caller method, return its corresponding candidate interface, 
	 * each interface is represented by its invoked top-level method calls. 
	 */
	public static HashSet<MethodClass> constructCandidateInterface(XLog componentLog, MethodClass callerM)
	{
		HashSet<MethodClass> InterfaceMethodSet = new HashSet<>();
		
		for(XTrace trace: componentLog)
		{
			for(XEvent event: trace)
			{
				if(callerM.getPackageName().equals(XSoftwareExtension.instance().extractCallerpackage(event))
						&&callerM.getClassName().equals(XSoftwareExtension.instance().extractCallerclass(event))
								&&callerM.getMethodName().equals(XSoftwareExtension.instance().extractCallermethod(event)))
				{
					MethodClass m =new MethodClass();
					m.setMethodName(XConceptExtension.instance().extractName(event));
					m.setClassName(XSoftwareExtension.instance().extractClass(event));
					m.setPackageName(XSoftwareExtension.instance().extractPackage(event));
					if(!InterfaceMethodSet.contains(m))
					{
						InterfaceMethodSet.add(m);
					}
				}				
			}
		}
		
		return InterfaceMethodSet;
	}
	
	
}
