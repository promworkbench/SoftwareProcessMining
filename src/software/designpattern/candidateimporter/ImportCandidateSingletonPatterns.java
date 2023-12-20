package software.designpattern.candidateimporter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.xml.sax.SAXException;

import UtilityClasses.ClassClass;
import software.designpattern.patterndefinition.SingletonPatternClass;
import software.designpattern.patterndefinition.SingletonPatternSet;

/**
 * import the candidate pattern patterns from xml files to the Prom workspace. 
 * the import file is an xml-based file, we use SAX for parsing. 
 * @author cliu3
 *
 */

@Plugin(
		name = "Import Candidate Singleton Patterns (.xml)",
		parameterLabels = { "File" },
		returnLabels = {"Candidate Singleton Pattern Instances"},
		returnTypes = {SingletonPatternSet.class},
		userAccessible = true
	)
	@UIImportPlugin(
			description = "Import Candidate Singleton Patterns (.xml)",//show the type in window for recording files
			extensions = {"xml"}// the suffix name
	)
public class ImportCandidateSingletonPatterns extends AbstractImportPlugin{
	
	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		// TODO Auto-generated method stub
		context.getFutureResult(0).setLabel(filename);
		
		System.out.println("candidate singleton pattern importer starts!");

		// import and parse the design pattern instance from the input stream. 
		return importer(input);
	}
	public static SingletonPatternSet importer(InputStream inputStream)
	{
		HashSet<SingletonCandidate> singletons = new HashSet<>();
		SAXParser parser = null;  
        try {  
            parser = SAXParserFactory.newInstance().newSAXParser();  
           
            SAXParseXML parseXml=new SAXParseXML();  
             
            //InputStream stream=new FileInputStream("C:\\Users\\cliu3\\Desktop\\DPDOberserPattern.xml");
            parser.parse(inputStream, parseXml);  
            singletons =parseXml.getSingletons();

          
        } catch (ParserConfigurationException e) {  
            e.printStackTrace();  
        } catch (SAXException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    
        
        /*
         * in the following, we parse each candidate pattern and construct the corresponding candidate pattern instances
         */
        
        SingletonPatternSet singletonSet = new SingletonPatternSet();
        for(SingletonCandidate singletonC:singletons)
        {
        	ClassClass singletonClass = new ClassClass();
        	singletonClass.setClassName(ImportCandidateObserverPatterns.extractClass(singletonC.getSingleton()));
        	singletonClass.setPackageName(ImportCandidateObserverPatterns.extractPackage(singletonC.getSingleton()));
        	
        	//new a singleton class
        	SingletonPatternClass spc = new SingletonPatternClass();
        	//set the pattern name
        	spc.setPatternName("Singleton Pattern");
        	spc.setSingletonClass(singletonClass);
        	singletonSet.add(spc);
        }
        
		return singletonSet;
	}
}
