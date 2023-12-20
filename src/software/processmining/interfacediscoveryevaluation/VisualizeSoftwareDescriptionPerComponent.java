package software.processmining.interfacediscoveryevaluation;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.widgets.ProMSplitPane;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotCluster;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

import UtilityClasses.MethodClass;

@Plugin(name = "Visualize Software Description Per Component", 
returnLabels = { "Dot visualization" }, 	
returnTypes = { JComponent.class }, 	
parameterLabels = {"Visualize Software Description Per Component"}, 	
userAccessible = true)
@Visualizer
public class VisualizeSoftwareDescriptionPerComponent {
	private HashMap<String, Dot> component2dot = new HashMap<String, Dot>();
	// define the main splitPane as the main visualization 
	private ProMSplitPane splitPane =new ProMSplitPane(ProMSplitPane.HORIZONTAL_SPLIT);
	// set the right part be a dot panel
	private JPanel rightDotpanel= new JPanel();
	// set the left part be a component list. 
	private JList componentList = new JList();
	
	public static final Color COLOR_LIST_BG = new Color(60, 60, 60);
	public static final Color COLOR_LIST_FG = new Color(180, 180, 180);
	public static final Color COLOR_LIST_SELECTION_BG = new Color(80, 0, 0);
	public static final Color COLOR_LIST_SELECTION_FG = new Color(240, 240, 240);
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Cong Liu", email = "c.liu.3@tue.nl")
	@PluginVariant(variantLabel = "cog", requiredParameterLabels = {0})
	
	public JComponent visualize(UIPluginContext context, SoftwareDescription sd) 
	{	
		// get the components for each component, we visualize its interface (HPNs) as dot.

		// get the components.
		HashSet<String> Components = new HashSet<String>();
		for (ComponentDescription comM:sd.getComponentSet())
		{
			Dot dot = convert(comM);
			component2dot.put(comM.getComponentName(), dot);
			Components.add(comM.getComponentName());
		}
		
				
		splitPane.setResizeWeight(0.05);
		splitPane.setLeftComponent(componentList);
		
		componentList.setListData(Components.toArray());
		componentList.setLayoutOrientation(JList.VERTICAL);// the single line list
		componentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);// single selection.
		componentList.setBackground(COLOR_LIST_BG);
		componentList.setForeground(COLOR_LIST_FG);
		componentList.setSelectionBackground(COLOR_LIST_SELECTION_BG);
		componentList.setSelectionForeground(COLOR_LIST_SELECTION_FG);
		componentList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				listselected();
			}
		});
		
		return splitPane;
	}
	
	private void listselected() 
	{
		// two types of mouse operations, if this is not true
		if (!componentList.getValueIsAdjusting())
		{
			//System.out.println(traceList.getSelectedValue());
			rightDotpanel=new DotPanel(component2dot.get(componentList.getSelectedValue()));
			splitPane.setRightComponent(rightDotpanel);
			//rightDotpanel.repaint();
			
		}
	}
	
	public static Dot convert(ComponentDescription cd)
	{
		Dot dot = new Dot();
		dot.setDirection(GraphDirection.leftRight);
		//dot.setDirection(GraphDirection.topDown);
		
		//for each component, we create a cluster, 

		DotCluster comCluster=dot.addCluster();
		comCluster.setLabel(cd.getComponentName());
		comCluster.setOption("penwidth", "3");
		//for each interface
		for(InterfaceDescription interf: cd.getInterfaceSet())
		{
			DotCluster interCluster = comCluster.addCluster();
			interCluster.setOption("penwidth", "2");
			interCluster.setOption("color", "blue");
			interCluster.setOption("style","dashed");
			
			for(MethodClass m: interf.getMethodSet())
			{
				DotNode methodNode = interCluster.addNode(m.toString());
				methodNode.setOption("shape", "box");
				methodNode.setOption("penwidth", "1");
			}
		}
		
		return dot;
	}
}
