package edu.uab.mukhtarlab.wkshelldecomposition.internal.task;

import com.google.gson.Gson;
import org.cytoscape.model.*;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;

import java.awt.*;
import java.util.*;
import java.util.List;


/**
 * Performs the weighted k-shell decomposition
 */

public class ConcentricLayoutTask implements ObservableTask {

    private boolean cancelled;
    private final CyNetwork network;
    private final CyNetworkView nView;
    private final VisualStyleFactory visualStyleFactoryServiceRef;
    private final VisualMappingFunctionFactory vmfFactoryC;
    private final VisualMappingFunctionFactory vmfFactoryP;

    public ConcentricLayoutTask(
            CyNetwork network,
            CyNetworkView nView,
            VisualStyleFactory visualStyleFactoryServiceRef,
            VisualMappingFunctionFactory vmfFactoryC,
            VisualMappingFunctionFactory vmfFactoryP
    ) {
        this.network = network;
        this.nView = nView;
        this.visualStyleFactoryServiceRef = visualStyleFactoryServiceRef;
        this.vmfFactoryC = vmfFactoryC;
        this.vmfFactoryP = vmfFactoryP;
    }

    /**
     * Runs the layout
     */
    @Override
    public void run(TaskMonitor tm) throws Exception {
        double nodeDiameter = 20.0;

        int layers = 20;

        int currentNode = 0;
        int nodesPerCircle = 1;
        int currentCircle = 0;
        double circleGap = 5.0;
        int currentLayer = 0;
        double layerGap = 50.0;

        double currentPositionRadius = 0;
        double angle = 0;
        double x = 0;
        double y = 0;
        double angleOffset = 0;

        CyRow row;
        Integer percentileBucket;
        Integer shell;
        double circlePerimeter;
        int maxShell = 0;

        List<CyNode> nodes = network.getNodeList();
        //sort nodes by bucket, shell
        Comparator<CyNode> comparator = new Comparator<CyNode>() {
            @Override
            public int compare(CyNode a, CyNode b) {
                CyRow aRow = network.getRow(a);
                CyRow bRow = network.getRow(b);

                Integer aShell = aRow.get("_wkshell", Integer.class);
                Integer bShell = bRow.get("_wkshell", Integer.class);
                aShell = (aShell!=null) ? aShell : 0;
                bShell = (bShell!=null) ? bShell : 0;
                if(bShell!=aShell) {
                    return bShell - aShell;
                } else {
                    Integer aBucket = aRow.get("_wks_percentile_bucket", Integer.class);
                    Integer bBucket = bRow.get("_wks_percentile_bucket", Integer.class);
                    aBucket = (aBucket!=null) ? aBucket : 0;
                    bBucket = (bBucket!=null) ? bBucket : 0;
                    return bBucket - aBucket;
                }

            }
        };
        nodes.sort(comparator);

        boolean layerChange = false;
        boolean circleChange = false;

        for (final CyNode node : nodes) {
            View<CyNode> nodeView = nView.getNodeView(node);
            row = network.getRow(node);

            //Get max shell for gradient mapping later
            shell = row.get("_wkshell", Integer.class);
            shell = (shell!=null) ? shell : 0;
            if(shell>maxShell) {
                maxShell = shell;
            }

            //For unimplemented layering
            percentileBucket = row.get("_wks_percentile_bucket", Integer.class);
            percentileBucket = (percentileBucket!=null) ? percentileBucket : 0;
            layerChange = false;//((layers-1-(percentileBucket/5))>currentLayer);
            if(layerChange) {
                currentLayer = layers-1-(percentileBucket/5);
            }

            //Does this node need to start a new circle for filling
            circleChange = (currentNode>=(nodesPerCircle));
            if(circleChange) {
                currentCircle++;
            }

            //If new circle for filling, update radius
            if(layerChange || circleChange){
                currentNode = 0;
                currentPositionRadius = (((double) currentCircle)*(nodeDiameter+circleGap)) + (((double) currentLayer)*layerGap);
                circlePerimeter = Math.PI*currentPositionRadius*2.0;
                nodesPerCircle = (int) (circlePerimeter/((nodeDiameter+circleGap)));
            }

            //TODO Maybe
            //Add an extra angle offset for the last circle if its empty to evenly space out the nodes within the circle
            //Would have to if on last circle and how many nodes there are in the last circle


            angle = Math.toRadians((((double) currentNode)/((double) nodesPerCircle))*360.0);
            x = Math.cos(angle) * currentPositionRadius;
            y = Math.sin(angle) * currentPositionRadius;

            nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION,x);
            nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION,y);

            currentNode++;
        }



        VisualStyle vs = getVisualStyle(nodeDiameter, maxShell);
        vs.apply(nView);

        nView.updateView();
        nView.fitContent();
    }

    private VisualStyle getVisualStyle(double nodeDiameter, int maxShell) {
        VisualStyle vs = visualStyleFactoryServiceRef.createVisualStyle("Shell gradient");
        vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, Color.BLACK); //Default handles null shell
        //Mapping for border paint handled below
        vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, nodeDiameter/10.0);
        vs.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.WHITE); //Default handles null shell
        //Mapping for fill color handled below
        vs.setDefaultValue(BasicVisualLexicon.NODE_HEIGHT, nodeDiameter);
        //Mapping for label handled below
        vs.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, Color.BLACK);
        vs.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, (int)(nodeDiameter*0.3));
        vs.setDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY, 255);
        vs.setDefaultValue(BasicVisualLexicon.NODE_WIDTH, nodeDiameter);

        vs.setDefaultValue(BasicVisualLexicon.NODE_SIZE, nodeDiameter);
        vs.setDefaultValue(BasicVisualLexicon.NODE_OPACITY, 200);


        //Fill Color
        ContinuousMapping fillMapping = (ContinuousMapping)
                vmfFactoryC.createVisualMappingFunction("_wkshell", Integer.class, BasicVisualLexicon.NODE_FILL_COLOR);
        // Define the points
        Double val1 = 1d;
        BoundaryRangeValues<Paint> brv1 = new BoundaryRangeValues<Paint>(Color.BLUE, Color.BLUE, Color.BLUE);
        Double val2 = (double) maxShell;
        BoundaryRangeValues<Paint> brv2 = new BoundaryRangeValues<Paint>(Color.RED, Color.RED, Color.RED);
        // Set the points
        fillMapping.addPoint(val1, brv1);
        fillMapping.addPoint(val2, brv2);
        // add the mapping to visual style
        vs.addVisualMappingFunction(fillMapping);

        //Border paint
        ContinuousMapping borderMapping = (ContinuousMapping)
                vmfFactoryC.createVisualMappingFunction("_wkshell", Integer.class, BasicVisualLexicon.NODE_BORDER_PAINT);
        //Reuse points from fill color
        // Set the points
        borderMapping.addPoint(val1, brv1);
        borderMapping.addPoint(val2, brv2);
        //add the mapping to visual style
        vs.addVisualMappingFunction(borderMapping);

        //Label
        PassthroughMapping labelMapping = (PassthroughMapping)
                vmfFactoryP.createVisualMappingFunction("name", String.class, BasicVisualLexicon.NODE_LABEL);



        vs.addVisualMappingFunction(labelMapping);

        return vs;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getResults(Class type) {
        if (type == CyNetworkView.class)
            return nView;

        if (type == String.class) {
            return "Created view: " + nView;
        }

        if (type == JSONResult.class) {
            Gson gson = new Gson();
            JSONResult res = () -> { return gson.toJson(nView); };

            return res;
        }

        return null;
    }

    @Override
    public List<Class<?>> getResultClasses() {
        return Arrays.asList(CyNetworkView.class, String.class, JSONResult.class);
    }
}
