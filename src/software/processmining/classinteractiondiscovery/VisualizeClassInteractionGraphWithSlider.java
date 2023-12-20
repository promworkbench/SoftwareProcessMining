package software.processmining.classinteractiondiscovery;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.widgets.ProMSplitPane;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.colourMaps.ColourMaps;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

import UtilityClasses.ComponentConfig;


/**
 * this class provides an interactive manner to visualize class interaction graph. 
 * In addition, the filtered graph can be exported to the ProM workspace, in the format of ComponentConfig.
 * This can be used as the input of component behavior discovery plugins. 
 * @author cliu3
 *
 */

@Plugin(name = "Visualize Class Interaction Graph", 
returnLabels = { "Dot visualization" }, 
returnTypes = { JComponent.class }, 
parameterLabels = { "Class Interaction Graph" }, 
userAccessible = true)
@Visualizer
public class VisualizeClassInteractionGraphWithSlider {
	
	// define the main splitPane as the main visualization 
	private ProMSplitPane splitPane =new ProMSplitPane(ProMSplitPane.HORIZONTAL_SPLIT);
	// set the right part be a dot panel, a slider and a export button
	private JPanel rightDotpanel= new JPanel();
	// set the left part be a dot panel
	private JPanel leftDotpanel= new JPanel();
	private Dot dot=null;
	private JSlider slider= new JSlider(JSlider.VERTICAL);//create the slider
	private JButton exportButton = new JButton("Export Cluster");//create a button
	private PluginContext Fcontext;
		
	// create a new weighted directed graph
	DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> Newg = 
			new DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
	ClassInteractionGraph currentCIG =new ClassInteractionGraph(Newg);
	
	
	ComponentConfig comConfig = new ComponentConfig();// to be exported
	final ComponentConfigWrapper c2csWrapper= new ComponentConfigWrapper(comConfig);//used for the action listener as the final. 
	
	int LowestBound = 0;
	int HighestBound = 0;
	int DefaultValue = 0;    //initial frames per second
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Cong Liu", email = "c.liu.3@tue.nl")
	@PluginVariant(variantLabel = "cig", requiredParameterLabels = { 0 })
	public JComponent visualize(UIPluginContext context, final ClassInteractionGraph cig) 
	{
		//it is shown as the title of returned results. 
		context.getFutureResult(0).setLabel("Class Interaction Graph");
		
		Fcontext=context;
		splitPane.setResizeWeight(0.9);
		
		//set the maximal value for the slider		
		HighestBound=ClusteringClassInteractionGraph.getHighestVaule(cig);
		
		// the default setting of the clusters. 
		c2csWrapper.setValue(ClusteringClassInteractionGraph.getClusters(cig));
				
		dot = convert(cig);
		leftDotpanel=new DotPanel(dot);
		splitPane.setLeftComponent(leftDotpanel);// set the left panel
		
		//create a slider 
		slider.setMaximum(HighestBound);
		slider.setMinimum(LowestBound);
		slider.setValue(DefaultValue);
		if(HighestBound<500)
		{
			slider.setMajorTickSpacing(10);
			slider.setMinorTickSpacing(1);
		}
		else if(HighestBound <2000)
		{
			slider.setMajorTickSpacing(30);
			slider.setMinorTickSpacing(1);
		}
		else if (HighestBound <10000)
		{
			slider.setMajorTickSpacing(200);
			slider.setMinorTickSpacing(1);
		}
		else {
			slider.setMajorTickSpacing(500);
			slider.setMinorTickSpacing(1);
		}
		
//		slider.setMajorTickSpacing(10);
//		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setName("Edge Filtering Threshold");
		
		//add change listerner for the slider.
		slider.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				  int frequency = slider.getValue();
			      // filterting the interaction graph based on the selected value. 
				  currentCIG =ClusteringClassInteractionGraph.filterEdges(frequency,cig);
				  dot = convert(currentCIG);
				  c2csWrapper.setValue(ClusteringClassInteractionGraph.getClusters(currentCIG));

			      leftDotpanel=new DotPanel(dot);
			      splitPane.setLeftComponent(leftDotpanel);
//			      leftDotpanel.repaint();
			}
		});
		
		rightDotpanel.setLayout(new BorderLayout());
		rightDotpanel.add(slider, BorderLayout.CENTER);
				
		//add action for the button
		rightDotpanel.add(exportButton, BorderLayout.SOUTH);
		exportButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				//System.out.println("Button has been clicked");
				Fcontext.getProvidedObjectManager().createProvidedObject("Clustering Result of: "+c2csWrapper.getValue().hashCode(),
						c2csWrapper.getValue(), ComponentConfig.class, Fcontext);
			   if (Fcontext instanceof UIPluginContext) {
			          UIPluginContext uiPluginContext = (UIPluginContext) Fcontext;
			          uiPluginContext.getGlobalContext().getResourceManager().getResourceForInstance(c2csWrapper.getValue()).setFavorite(true);
				}
			}
		});
		splitPane.setRightComponent(rightDotpanel);// set the right part of the splitpane.
		
		return splitPane;
	}
	
	public static Dot convert(ClassInteractionGraph cig) {
		Dot dot = new Dot();
		dot.setDirection(GraphDirection.leftRight);
		
		//prepare the nodes
		HashMap<String, DotNode> activityToNode = new HashMap<String, DotNode>();
		for (String activity : cig.getClassInteractionGraph().vertexSet()) 
		{
			DotNode node = dot.addNode(activity);
			activityToNode.put(activity, node);
			node.setOption("shape", "box");
		}
		
		//prepare the edges
		for (DefaultWeightedEdge edge :cig.getClassInteractionGraph().edgeSet()) 
		{
			String from = cig.getClassInteractionGraph().getEdgeSource(edge);
			String to =  cig.getClassInteractionGraph().getEdgeTarget(edge);
			double weight = cig.getClassInteractionGraph().getEdgeWeight(edge);

			DotNode source = activityToNode.get(from);
			DotNode target = activityToNode.get(to);
			String label = String.valueOf(weight);

			DotEdge dotEdge =dot.addEdge(source, target, label);
			
			//dotEdge.setOption("color", ColourMap.toHexString(ColourMaps.colourMapBlue((long)weight/2, (long)weight)));
			dotEdge.setOption("color", ColourMap.toHexString(ColourMaps.colourMapBlue((long)weight/2, (long)weight)));
			//dotEdge.setOption("color", ColourMap.toHexString(new ColourMapBlue().colour((long)weight/2, 0, (long)weight)));
			//dotEdge.setOption("penwidth",String.valueOf(weight/5));
		}

		return dot;
	}
}
