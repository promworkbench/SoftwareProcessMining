package software.processmining.interfacediscoveryevaluation;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.pnanalysis.metrics.PetriNetMetric;
import org.processmining.pnanalysis.metrics.PetriNetMetricManager;

import UtilityClasses.ClassClass;
import UtilityClasses.ComponentConfig;
import UtilityFunctions.OrderingEventsNano;
import openXESsoftwareextension.XSoftwareExtension;
import software.processmining.interfacediscovery.ConstructSoftwareEventLog;

/**
 * This plugin aims to evaluate the complexity of the discovered interface behavioral model. 
 * we use two complexity metrics, "Extended Cardoso metric" and "Extended Cyclomatic metric", from paper "Complexity metric for workflow nets".  
 * These two metrics are implemented in "Show Petri-net Metrics" plugin, the PNAnalysis package. 
 * 
 * Input: ComponentModelsSet;
 * Output: a complexity metrics for each component, by taking the average of the interface model. 
 * Note that we use flat workflow net to represent interface model, i.e., the top-level model of hpn. 
 * @author cliu3
 *
 */

@Plugin(
		name = "Interface Complexity Metrics",// plugin name
		
		returnLabels = {"String"}, //reture labels
		returnTypes = {String.class},//return class
		
		//input parameter labels
		parameterLabels = {"Software Description", "Software Event Log", "Component Configuration"},
		
		userAccessible = true,
		help = "This plugin aims to discovery design patterns from software event data." 
		)
public class InterfaceModelComplexityEvaluationPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Interface Complexity Metrics, default",
			// the number of required parameters, {0} means the first input parameter, {0, 1} means the second input parameter, {0, 1, 2} means the third input parameter
			requiredParameterLabels = {0, 1, 2} 
			)
	public String computeMetrics(UIPluginContext context, SoftwareDescription softwareDescription, XLog originalLog, 
			ComponentConfig comconfig) throws ConnectionCannotBeObtained, IOException 
	{
		//set the inductive miner parameters, the original log is used to set the classifier
		IMMiningDialog dialog = new IMMiningDialog(originalLog);
		InteractionResult result = context.showWizard("Configure Parameters for Inductive Miner (used for all interface models)", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		// the mining parameters are set here 
		MiningParameters IMparameters = dialog.getMiningParameters(); //IMparameters.getClassifier()
				
		//create factory to create Xlog, Xtrace and Xevent.
		XFactory factory = new XFactoryNaiveImpl();
		XLogInfo Xloginfo = XLogInfoFactory.createLogInfo(originalLog, IMparameters.getClassifier());
				
		//create .xls and create a worksheet.
		int rowNumber=0;
        FileOutputStream fos = new FileOutputStream("D:\\XPort\\data2excel.xls");
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet worksheet = workbook.createSheet("Interface Quality Metrics");
        //Create ROW-1
        HSSFRow row1 = worksheet.createRow(rowNumber);

        //Create COL-A from ROW-1 and set data
        HSSFCell cellA1 = row1.createCell((short) 0);
        cellA1.setCellValue("Component");
        
        //Create COL-B from ROW-1 and set data
        HSSFCell cellB1 = row1.createCell((short) 1);
        cellB1.setCellValue("Extended Cardoso metric");

        //Create COL-C from row-1 and set data
        HSSFCell cellC1 = row1.createCell((short) 2);
        cellC1.setCellValue("Extended Cyclomatic metric");

        //Create COL-D from row-1 and set data
        HSSFCell cellD1 = row1.createCell((short) 3);
        cellC1.setCellValue("Complexity"); //the harmonic mean of Cardoso and Cyclomatic
        
		//for each component, we fisrt get its component log
		for(ComponentDescription componentDescription: softwareDescription.getComponentSet())
		{
			rowNumber++;
			double CardosoMetric=0;
			double CyclomaticMetric=0;
			// create class set of the current component
			Set<String> classSet = new HashSet<>();
			for(ClassClass c: comconfig.getClasses(componentDescription.getComponentName()))
			{
				classSet.add(c.getPackageName()+"."+c.getClassName());// both the package and class names are used to describe each class
			}
			//obtain the software event log for each component.
			XLog comLog = ConstructSoftwareEventLog.generatingComponentEventLog(componentDescription.getComponentName(), classSet, originalLog, factory);
			
			//for each component, we construct the component model 
			for(InterfaceDescription id: componentDescription.getInterfaceSet())// add values
			{
				//get the event log of each interface, when construct log we need also take the caller methods as input.
				XLog interfaceLog = ConstructSoftwareEventLog.constructInterfaceLog(comLog, 1 , id.getMethodSet(), 
						id.getCallerMethodSet(), factory);
						
				//call the ordering log before mining and use the inductive miner for discovering. 
				Object[] objs =IMPetriNet.minePetriNet(OrderingEventsNano.ordering(interfaceLog, XSoftwareExtension.KEY_STARTTIMENANO), 
						IMparameters, new Canceller() {
					public boolean isCancelled() {
						return false;
					}
				});
				
//				// use Petri net reduction rules, based on Murata rules, i.e., Reduce Silent Transitions, Preserve Behavior
//				Murata  murata = new Murata ();
//				MurataParameters paras = new MurataParameters();
//				paras.setAllowFPTSacredNode(false);
//				Petrinet pn =(Petrinet) murata.run(context, (Petrinet)objs[0], (Marking)objs[1], paras)[0];
				
				CardosoMetric=CardosoMetric+computeCardoso(context, (Petrinet)objs[0], (Marking)objs[1]);
				CyclomaticMetric=CyclomaticMetric+computeCyclomatic(context, (Petrinet)objs[0], (Marking)objs[1]);
				
			}
			
			//write the average value to excel. 
			//Create rowi
            HSSFRow rowi = worksheet.createRow(rowNumber);
            
            //Create COL-A from rowi and set data
            HSSFCell cellAi = rowi.createCell(0);
            cellAi.setCellValue(componentDescription.getComponentName());

            double EcaM =  CardosoMetric/componentDescription.getInterfaceSet().size();
            //Create COL-B from row-1 and set data
            HSSFCell cellBi = rowi.createCell(1);
            cellBi.setCellValue(EcaM);

            double EcyM = CyclomaticMetric/componentDescription.getInterfaceSet().size();
            //Create COL-C from row-1 and set data
            HSSFCell cellCi = rowi.createCell(2);
            cellCi.setCellValue(EcyM);
            
			double complexity = (2*EcaM*EcyM)/(EcaM+EcyM);// it id defined as 2*CardosoMetric*CyclomaticMetric/CardosoMetric+CyclomaticMetric
			 
			//Create COL-D from row-1 and set data
            HSSFCell cellDi = rowi.createCell(3);
            cellDi.setCellValue(complexity);
			
		}
		
		
		//Save the workbook in .xls file
        workbook.write(fos);
        fos.flush();
        fos.close();
		
		return "";
	}
	
	public static double computeCardoso(PluginContext context, Petrinet net, Marking marking)
	{
		double value =0;
		for (PetriNetMetric metric : PetriNetMetricManager.getInstance().getMetrics()) {
//			//create a marking for the net
//			Marking marking = new Marking();
//			for (Place place : net.getPlaces()) {
//				if (net.getInEdges(place).isEmpty()) {
//					marking.add(place);
//				}
//			}
			
			String metricName = metric.getName();
			if(metricName.equals("Extended Cardoso metric"))
			{
				value = metric.compute(context, net, marking);
//				System.out.println(metricName+" is "+value);	
			}
		}
		return value;
	}
	
	public static double computeCyclomatic(PluginContext context, Petrinet net, Marking marking)
	{
		double value =0;
		for (PetriNetMetric metric : PetriNetMetricManager.getInstance().getMetrics()) {
//			//create a marking for the net
//			Marking marking = new Marking();
//			for (Place place : net.getPlaces()) {
//				if (net.getInEdges(place).isEmpty()) {
//					marking.add(place);
//				}
//			}
			
			String metricName = metric.getName();
			
			if(metricName.equals("Extended Cyclomatic metric"))
			{
				value = metric.compute(context, net, marking);
//				System.out.println(metricName+" is "+value);
			}
		}
		return value;
	}
	
	public static void main (String [] args)
	{
		//test the excel writer of Apache POI.... https://stackoverflow.com/questions/3454975/writing-to-excel-in-java
		try {
            //create .xls and create a worksheet.
            FileOutputStream fos = new FileOutputStream("D:\\XPort\\data2excel.xls");
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet worksheet = workbook.createSheet("Interface Quality Metrics");

            //Create ROW-1
            HSSFRow row1 = worksheet.createRow((short) 0);

            //Create COL-A from ROW-1 and set data
            HSSFCell cellA1 = row1.createCell((short) 0);
            cellA1.setCellValue("Extended Cardoso metric");

            //Create COL-B from row-1 and set data
            HSSFCell cellB1 = row1.createCell((short) 1);
            cellB1.setCellValue("Extended Cyclomatic metric");

            //Create COL-C from row-1 and set data
            HSSFCell cellC1 = row1.createCell((short) 2);
            cellC1.setCellValue(true);

            //Save the workbook in .xls file
            workbook.write(fos);
            fos.flush();
            fos.close();
       
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
