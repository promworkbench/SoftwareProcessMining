package BasicStatisticsSoftwareEventLog;
/**
 * this class defines a set of basic statistic number to have a general understanding of software event log
 * @author cliu3
 *
 */

import java.util.HashSet;
public class SoftwareEventLogStatistics {
	
	
	/*
	 * simple statistics
	 */
	//name of the software event log: 
	private String logName ="";
	
	//the number of software executions:
	private int traceNumber=0;
	
	//the number of software method calls
	private int eventNumber=0;
	
	//the average number of method calls per execution
	private double averageEventNumber=0;
	
	// the number of packages
	private int packageNumber =0;
	
	// the number of classes
	private int classNumber =0;
	
	// the number of methods
	private int methodNumber =0;
	

	/*
	 * detailed information
	 */
	
	//the information of main method of this software
	//given a log, there may be multiple main. This log is generated from multiple software. 
	private HashSet<String> mainSet = new HashSet();;
	
	
	//the information of packages
	private HashSet<String> packageSet = new HashSet();
	
	//the information of classes
	private HashSet<String> classSet= new HashSet();
	
	
	//the information of methods
	private HashSet<String> methodSet = new HashSet();
	
	
	
	/**
	 * getter and setters
	 * @return
	 */
	
	public HashSet<String> getMainSet() {
		return mainSet;
	}


	public void setMainSet(HashSet<String> mainSet) {
		this.mainSet = mainSet;
	}


	public String getLogName() {
		return logName;
	}


	public void setLogName(String logName) {
		this.logName = logName;
	}


	public int getTraceNumber() {
		return traceNumber;
	}


	public void setTraceNumber(int traceNumber) {
		this.traceNumber = traceNumber;
	}


	public int getEventNumber() {
		return eventNumber;
	}


	public void setEventNumber(int eventNumber) {
		this.eventNumber = eventNumber;
	}


	public double getAverageEventNumber() {
		return averageEventNumber;
	}


	public void setAverageEventNumber(double averageEventNumber) {
		this.averageEventNumber = averageEventNumber;
	}


	public int getPackageNumber() {
		return packageNumber;
	}


	public void setPackageNumber(int packageNumber) {
		this.packageNumber = packageNumber;
	}


	public int getClassNumber() {
		return classNumber;
	}


	public void setClassNumber(int classNumber) {
		this.classNumber = classNumber;
	}


	public int getMethodNumber() {
		return methodNumber;
	}


	public void setMethodNumber(int methodNumber) {
		this.methodNumber = methodNumber;
	}



	public HashSet<String> getPackageSet() {
		return packageSet;
	}


	public void setPackageSet(HashSet<String> packageSet) {
		this.packageSet = packageSet;
	}


	public HashSet<String> getClassSet() {
		return classSet;
	}


	public void setClassSet(HashSet<String> classSet) {
		this.classSet = classSet;
	}


	public HashSet<String> getMethodSet() {
		return methodSet;
	}


	public void setMethodSet(HashSet<String> methodSet) {
		this.methodSet = methodSet;
	}
	
	
}
