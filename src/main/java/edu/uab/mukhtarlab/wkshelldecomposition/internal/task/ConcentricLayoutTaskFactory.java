package edu.uab.mukhtarlab.wkshelldecomposition.internal.task;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * Applies ConcentricLayout to the given CyNetworkView
 */
public class ConcentricLayoutTaskFactory implements TaskFactory {
    private CyNetwork network;
    private CyNetworkView nView;
    private VisualStyleFactory visualStyleFactoryServiceRef;
    private VisualMappingFunctionFactory vmfFactoryC;
    private VisualMappingFunctionFactory vmfFactoryP;

    public ConcentricLayoutTaskFactory(
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

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new ConcentricLayoutTask(network, nView, visualStyleFactoryServiceRef, vmfFactoryC, vmfFactoryP));
    }

    @Override
    public boolean isReady() {
        return true;
    }


    public boolean isReady(CyNetworkView view) {
        return view != null;
    };
}