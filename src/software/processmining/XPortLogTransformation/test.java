package software.processmining.XPortLogTransformation;

public class test {

	public static void main(String []args)
	{
		String regex = "[0-9]+";
		//String temp="Class$122";
		String temp="Class$class2$3";
		if(temp.contains("$"))
		{
			System.out.println("contains $");
		}
		String arr[] =temp.split("\\$");
		if(arr[arr.length-1].matches(regex))//only digit in the last part. 
		{
			System.out.println(arr[arr.length-2]);
		}
		else{
			System.out.println(arr[arr.length-1]);
		}
	}
}
