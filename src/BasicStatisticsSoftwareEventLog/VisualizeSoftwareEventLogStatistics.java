package BasicStatisticsSoftwareEventLog;

public class VisualizeSoftwareEventLogStatistics {

	//visualize the SoftwareEventLogStatistics as a string
	public static String visualizeSoftwareLog(SoftwareEventLogStatistics ses)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("<html>"); 
		buffer.append("<body>");
		
		buffer.append("<h1 style=\"color:blue;\">"+"Software Event Log Statistics: "+ ses.getLogName()+"</h1>"); //style=\"color:blue;\"
		
		buffer.append("<h2 style=\"color:#2F8AA1;\">"+"General Statistics</h2>");

		//the first table of general statistics
		buffer.append("<table bgcolor=\"#ABE8E0\" align=\"left\">");
		
		buffer.append("<tr><td style=\"font-size:120%;\">"+"The Number of Executions: </td><td style=\"font-size:120%;\">"+ses.getTraceNumber()+"</td></tr>");
		buffer.append("<tr><td style=\"font-size:120%;\">"+"The Number of Method Calls: </td><td style=\"font-size:120%;\">"+ses.getEventNumber()+"</td></tr>");
		buffer.append("<tr><td style=\"font-size:120%;\">"+"The Average Number of Method Calls: </td><td style=\"font-size:120%;\">"+ses.getAverageEventNumber()+"</td></tr>");
		buffer.append("<tr><td style=\"font-size:120%;\">"+"The Number of Packages: </td><td style=\"font-size:120%;\">"+ses.getPackageNumber()+"</td></tr>");
		buffer.append("<tr><td style=\"font-size:120%;\">"+"The Number of Classes: </td><td style=\"font-size:120%;\">"+ses.getClassNumber()+"</td></tr>");
		buffer.append("<tr><td style=\"font-size:120%;\">"+"The Number of Methods: </td><td style=\"font-size:120%;\">"+ses.getMethodNumber()+"</td></tr>");

		// for the main methods
		buffer.append("<tr><td style=\"font-size:120%;\">"+"The Main Methods: </td>");
		for(String mainMethod: ses.getMainSet())
		{
			buffer.append("<td style=\"font-size:120%;\">" + mainMethod+"</td>");
		}
			
		buffer.append("</tr></table>");
		
		
		buffer.append("<h2 style=\"color:#2F8AA1;\">"+"Detailed Statistics</h2>");

		//the second table of packages, classes and methods
		buffer.append("<table bgcolor=\"#ABE8E0\" align=\"left\">");
		buffer.append("<tr><th style=\"font-size:120%;\"> Package </th><th style=\"font-size:120%;\"> Class </th> <th style=\"font-size:120%;\"> Method </th></tr>");

		for(String p: ses.getPackageSet())
		{	
			buffer.append("<tr><td style=\"font-size:120%;\">" + p + "</td><td></td><td></td></tr>"); 
			for(String c: ses.getClassSet())
			{
				if(getPackageFromClass(c).equals(p))
				{
					buffer.append("<tr><td></td><td style=\"font-size:120%;\">" + getClassFromClass(c) + "</td><td></td></tr>"); 
					for(String m: ses.getMethodSet())
					{
						if(getClassFromMethod(m).equals(c))
						{
							buffer.append("<tr><td></td><td></td><td style=\"font-size:120%;\">" + getMethodFromMethod(m) + "</td></tr>"); 
						}
					}
				}
			}
		}
		
		buffer.append("</table>");
		buffer.append("</body>");
		buffer.append("</html>");
		return buffer.toString();
	}
	
	/*
	 * Example: p1.p.c return p1.p
	 */
	public static String getPackageFromClass(String c)
	{
		String args[]=c.split("\\.");	
		
		String Package = args[0];
		for (int i=1;i<args.length-1;i++)
		{
			Package=Package+"."+args[i];
		}
		
		return Package;
	}
	
	/*
	 * Example: p1.p.c return c
	 */
	public static String getClassFromClass(String c)
	{
		String args[]=c.split("\\.");	
				
		return args[args.length-1];
	}
	
	/*
	 * Example: p1.p.c.m1() return p1.p.c
	 */
	public static String getClassFromMethod(String m)
	{
		String args[]=m.split("\\.");	
		
		String Package = args[0];
		for (int i=1;i<args.length-1;i++)
		{
			Package=Package+"."+args[i];
		}
		
		return Package;
	}
	
	/*
	 * Example: p1.p.c.m1() return m1()
	 */
	public static String getMethodFromMethod(String m)
	{
		String args[]=m.split("\\.");	
				
		return args[args.length-1];
	}
	
}
