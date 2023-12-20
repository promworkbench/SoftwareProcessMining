package software.designpattern.candidateimporter;
/*
 * this class defines the structure of the detected singleton patterns instances by DPD. 
 * it is only an intermediate data structure to store the original patterns. 
 */

public class SingletonCandidate {
	private String Singleton ="";
	//private String uniqueInstance ="";
	public String getSingleton() {
		return Singleton;
	}
	public void setSingleton(String singleton) {
		Singleton = singleton;
	}
//	public String getUniqueInstance() {
//		return uniqueInstance;
//	}
//	public void setUniqueInstance(String uniqueInstance) {
//		this.uniqueInstance = uniqueInstance;
//	}
	
	
}
