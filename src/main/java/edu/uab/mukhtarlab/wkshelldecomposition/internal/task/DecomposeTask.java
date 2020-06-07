package edu.uab.mukhtarlab.wkshelldecomposition.internal.task;

import com.google.gson.Gson;
import edu.uab.mukhtarlab.wkshelldecomposition.internal.model.Result;
import edu.uab.mukhtarlab.wkshelldecomposition.internal.model.Shell;
import org.cytoscape.model.*;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;

import java.util.*;

import edu.uab.mukhtarlab.wkshelldecomposition.internal.algorithm.Graph;
import edu.uab.mukhtarlab.wkshelldecomposition.internal.algorithm.WKShell;


/**
 * Performs the weighted k-shell decomposition
 */

public class DecomposeTask extends AbstractTask implements ObservableTask {

	private boolean cancelled;
	private final CyNetwork network;
	private final CyNetworkView nView;
	private final VisualStyleFactory visualStyleFactoryServiceRef;
	private final VisualMappingFunctionFactory vmfFactoryC;
	private final VisualMappingFunctionFactory vmfFactoryP;

	private Result result = new Result();

	public DecomposeTask(
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
	 * Runs the decomposition
	 */
	@Override
	public void run(TaskMonitor tm) throws Exception {
		//Convert network to a graph
		Graph graph = new Graph();
		TreeMap<CyNode, Integer> cytoscapeToAnalysisDict = new TreeMap<CyNode, Integer>(new Comparator<CyNode>()
		{
			public int compare(CyNode o1, CyNode o2)
			{
				return o1.getSUID().compareTo(o2.getSUID());
			}
		});
		TreeMap<Integer, CyNode> analysisToCytoscapeDict = new TreeMap<Integer, CyNode>();

		int i=1;
		boolean outwardEdge;
		for(CyNode node : network.getNodeList()) {
			if(!cytoscapeToAnalysisDict.containsKey(node)) {
				outwardEdge = false;
				for(CyEdge edge : network.getAdjacentEdgeList(node, CyEdge.Type.ANY)) {
					if(edge.getTarget().getSUID()!=edge.getSource().getSUID()) {
						outwardEdge = true;
						break;
					}
				}
				if(outwardEdge) {
					cytoscapeToAnalysisDict.put(node, i);
					analysisToCytoscapeDict.put(i, node);
					i++;
				}
			}
		}

		CyNode source;
		CyNode target;
		int sourceInt;
		int targetInt;
		for(CyEdge edge : network.getEdgeList()) {
			source = edge.getSource();
			if(cytoscapeToAnalysisDict.containsKey(source)) {
				sourceInt = cytoscapeToAnalysisDict.get(source);
				target = edge.getTarget();
				if(cytoscapeToAnalysisDict.containsKey(target)) {
					targetInt = cytoscapeToAnalysisDict.get(target);
					if(sourceInt!=targetInt) {
						graph.addEdge("" + sourceInt, "" + targetInt);
					}
				}
			}
		}


		//Perform the algorithm on the graph
		WKShell decomposer = new WKShell();
		ArrayList<String> shells = decomposer.decompose(graph);

		//Write results to network and result class
		CyTable nodeTable = network.getDefaultNodeTable();
		if(nodeTable.getColumn("_wkshell")==null) {
			nodeTable.createColumn("_wkshell", Integer.class, false);
		}
		if(nodeTable.getColumn("_wks_percentile_bucket")==null) {
			nodeTable.createColumn("_wks_percentile_bucket", Integer.class, false);
		}
		CyRow nodeRow;
		CyNode node;
		i=1;
		Shell resultShell;
		for(String shell : shells) {
			resultShell = new Shell(i);
			for (String nodeIndex : shell.split(",")) {
				if(nodeIndex!=null && !nodeIndex.equals("") && analysisToCytoscapeDict.containsKey(Integer.parseInt(nodeIndex))) {
					//Get Cynode
					node = analysisToCytoscapeDict.get(Integer.parseInt(nodeIndex));
					nodeRow = network.getRow(node);
					nodeRow.set("_wkshell", i);
					resultShell.addNode(node);
				}
			}
			i++;
			this.result.addShell(resultShell);
		}

		//Determine which bucket each shell belongs to
		int networkSize = this.result.getSize();
		int nodesTraversed = 0;
		int bucket = 0;

		//double percentile;
		for(Shell shell : this.result.getShells())  {
			for(CyNode tempNode : shell.getNodes()) {
				bucket = (((int)((nodesTraversed/(double) networkSize)*100))/5)*5;
				//test if greater than >5 then 10>
				nodeRow = network.getRow(tempNode);
				nodeRow.set("_wks_percentile_bucket", bucket);
				nodesTraversed++;
			}
		}

		insertTasksAfterCurrentTask(new ConcentricLayoutTask(network, nView, visualStyleFactoryServiceRef, vmfFactoryC, vmfFactoryP));
	}

	@Override
	public void cancel() {
		cancelled = true;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getResults(Class type) {
		if (type == Result.class)
			return this.result;

		if (type == String.class) {
			StringBuilder sb = new StringBuilder();

			if (result == null) {
				sb.append("The decomposition failed"
				);
			} else {
				List<Shell> shells = result.getShells();

				sb.append(String.format(
						"<html><body>"
								+ "<table style='font-family: monospace;'>"
								+ "<tr style='font-weight: bold; border-width: 0px 0px 1px 0px; border-style: dotted;'>"
								+ "<th style='text-align: left;'>K</th>"
								+ "<th style='text-align: left;'>Nodes</th>"
								+ "</tr>"
				));

				for (Shell shell : shells)
					sb.append(String.format(
							"<tr>"
									+ "<td style='text-align: right;'>%d</td>"
									+ "<td style='text-align: right;'>%d</td></tr>",
							shell.getK(),
							shell.getSize()
					));

				sb.append("</table></body></html>");
			}

			return sb.toString();
		}

		if (type == JSONResult.class) {
			Gson gson = new Gson();
			JSONResult res = () -> { return gson.toJson(result); };

			return res;
		}

		return null;
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(Result.class, String.class, JSONResult.class);
	}
}
