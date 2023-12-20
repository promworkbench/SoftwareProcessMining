package software.processmining.componentidentification;

import java.awt.Color;
import java.util.HashMap;
import java.util.Set;

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
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

import UtilityClasses.ClassClass;
import UtilityClasses.ComponentConfig;



public class VisualizeComponentConfig {
	//the component to dot map
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
	@Plugin(name = "Visualize Component Configuration (Seperate View)", 
			returnLabels = { "Dot visualization" }, 	
			returnTypes = { JComponent.class }, 	
			parameterLabels = {"Visualize Component Configuration"}, 	
			userAccessible = true)
			@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Cong Liu", email = "c.liu.3@tue.nl")
	@PluginVariant(variantLabel = "cog", requiredParameterLabels = {0})
	
	public JComponent visualize(UIPluginContext context, ComponentConfig cc) 
	{	
		// get the components
		Set<String> componentSet =cc.getAllComponents();
	
		//for each component, we visualize its classes as dot.
		for(String componentName: componentSet)
		{
			Dot dot = new Dot();
			dot.setDirection(GraphDirection.leftRight);
			for(ClassClass c: cc.getClasses(componentName))
			{
				DotNode tempNode = dot.addNode(c.toString());
				tempNode.setOption("shape", "box");
			}		
			component2dot.put(componentName, dot);
		}
		
		splitPane.setResizeWeight(0.05);
		splitPane.setLeftComponent(componentList);
		
		componentList.setListData(componentSet.toArray());
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
			rightDotpanel=new DotPanel(component2dot.get(componentList.getSelectedValue()));
			splitPane.setRightComponent(rightDotpanel);
			//rightDotpanel.repaint();
		}
	}
}
