package SpecificationMining;

public class testSSM {

	public static void main (String [] args)
	{
		String s ="<START>";
		System.out.println(s);
		s.replaceAll("<", "");
		System.out.println(s.replaceAll("<", "").replaceAll(">", ""));
	}
}
