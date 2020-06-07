package edu.uab.mukhtarlab.wkshelldecomposition.internal.task;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * Factory to create the task that will perform the weighted k-shell decomposition
 */

public class DecomposeTaskFactory implements TaskFactory {

	private final CyNetwork network;
	private final CyNetworkView nView;
	private final VisualStyleFactory visualStyleFactoryServiceRef;
	private final VisualMappingFunctionFactory vmfFactoryC;
	private final VisualMappingFunctionFactory vmfFactoryP;

	public DecomposeTaskFactory(
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
		return new TaskIterator(new DecomposeTask(network, nView, visualStyleFactoryServiceRef, vmfFactoryC, vmfFactoryP));
	}

	@Override
	public boolean isReady() {
		return true;
	}
}
