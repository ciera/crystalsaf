/**
 * Copyright (c) 2006, 2007, 2008 Marwan Abi-Antoun, Jonathan Aldrich, Nels E. Beckman,
 * Kevin Bierhoff, David Dickey, Ciera Jaspan, Thomas LaToza, Gabriel Zenarosa, and others.
 *
 * This file is part of Crystal.
 *
 * Crystal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Crystal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Crystal.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cmu.cs.crystal.flow.experimental;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.dom.ASTNode;

import edu.cmu.cs.crystal.BooleanLabel;
import edu.cmu.cs.crystal.ILabel;
import edu.cmu.cs.crystal.NormalLabel;
import edu.cmu.cs.crystal.cfg.ICFGEdge;
import edu.cmu.cs.crystal.cfg.ICFGNode;
import edu.cmu.cs.crystal.cfg.IControlFlowGraph;
import edu.cmu.cs.crystal.flow.AnalysisDirection;
import edu.cmu.cs.crystal.flow.IResult;
import edu.cmu.cs.crystal.flow.Lattice;
import edu.cmu.cs.crystal.flow.LatticeElement;

/**
 * This class encapsulates a worklist algorithm for computing fixed points
 * over flow graphs as a <i>Template Method</i> {@link #performAnalysis()}.  
 * Subclasses in particular need to provide 
 * <ul>
 * <li>a flow graph,</li>
 * <li>a analysis direction,</li>
 * <li>a lattice, and</li> 
 * <li>a way of transferring over flow graph nodes.</li>
 * </ul>
 * While branch sensitivity is achieved in specific implementations of the transfer method,
 * the implementation keeps incoming analysis results from different branches separate.
 * This allows precise treatment of short-circuiting Java operators and backwards analysis results.
 * 
 * @author Kevin Bierhoff
 */
public abstract class WorklistTemplate<LE extends LatticeElement<LE>>  {
	
	private static final Logger log = Logger.getLogger(WorklistTemplate.class.getName());
	
	/**
     * Carries out the worklist algorithm to discover the results
     * of the ASTNode argument.  This method implements the <i>Template 
     * Method</i> pattern: It calls abstract methods defined in this class
     * at the appropriate moments.
     * 
     * @see #getAnalysisDirection()
     * @see #getControlFlowGraph()
     * @see #getLattice()
     * @see #transferNode(ICFGNode, LatticeElement, ILabel)
     */
    public AnalysisResult<LE> performAnalysis() {
    	
		// Setup result mappings
    	HashMap<ICFGNode<?>, IResult<LE>> labeledResultsBefore = new HashMap<ICFGNode<?>, IResult<LE>>();
    	HashMap<ICFGNode<?>, IResult<LE>> labeledResultsAfter = new HashMap<ICFGNode<?>, IResult<LE>>();
    	HashMap<ASTNode, Set<ICFGNode<?>>> nodeMap = new HashMap<ASTNode, Set<ICFGNode<?>>>();
		
		// 0. Verify and Collect required data: direction, lattice, CFG
		AnalysisDirection direction;
		Lattice<LE> lattice;
		IControlFlowGraph cfg;
		
		direction = getAnalysisDirection();
		assert direction != null : "Cannot perform dataflow analysis without a direction";
		lattice = getLattice();
		assert lattice != null : "Cannot perform dataflow analysis without a lattice";
		cfg = getControlFlowGraph();
		assert cfg != null : "Cannot perform dataflow analysis without a CFG";

		// Populate fields about the current analysis
		boolean isForward = direction.equals(AnalysisDirection.FORWARD_ANALYSIS);
		Map<ICFGNode<?>, IResult<LE>> resultsBeforeAnalyzing;  
		Map<ICFGNode<?>, IResult<LE>> resultsAfterAnalyzing;
		// make result mappings relative to analysis direction
		// will use results[Before|After]Analyzing throughout the algorithm
		if (isForward) {
			resultsBeforeAnalyzing = labeledResultsBefore;
			resultsAfterAnalyzing = labeledResultsAfter;
		} 
		else {
			resultsBeforeAnalyzing = labeledResultsAfter;
			resultsAfterAnalyzing = labeledResultsBefore;
		}

		// 1. Set up worklist with initial node.
		SortedSet<ICFGNode<?>> worklist = new TreeSet<ICFGNode<?>>(
				WorklistNodeOrderComparator.createPostOrderAndPopulateNodeMap(cfg, nodeMap, isForward));

		ICFGNode<?> initialNode = isForward ? cfg.getStartNode() : cfg.getEndNode();
		worklist.add(initialNode);
		resultsBeforeAnalyzing.put(initialNode, new IncomingResult<LE>(lattice.entry()));
		
		// 2. LOOP Until Stack is Empty
		while (! worklist.isEmpty()) {
			
			// Pop a ControlFlowNode off the stack
			// Pick last post-order to visit nodes in "reverse" post-order
			ICFGNode<?> fromNode = worklist.last();
			worklist.remove(fromNode);
			
			try {
				// Add CFG node to AST node map
//				registerCfgNode(nodeMap, fromNode);
	
				// 2a. Establish before-node analysis result
				
				// Retrieve the lattice information from the fromNode
				// It's an error if this information doesn't exist
				IResult<LE> beforeFromLattice = checkNull(resultsBeforeAnalyzing.get(fromNode));
	
				// 2b. transfer over node
				IResult<LE> afterResults = null;
				for (ILabel transferLabel : beforeFromLattice.keySet()) {
				
					// Create a copy of the lattice to protect it from accidental
					// manipulation by the transfer function.
					LE beforeFromLatticeCopy = checkNull(beforeFromLattice.get(transferLabel).copy());
					
					// Carry out the associated flow function with the copy lattice
					IResult<LE> transferResults = 
						checkNull(transferNode(fromNode, beforeFromLatticeCopy, transferLabel));
							
					if (afterResults == null)
						afterResults = transferResults;
					else
						afterResults = checkNull(afterResults.join(transferResults));
				}		
						
				// put result back in
				resultsAfterAnalyzing.put(fromNode, checkNull(afterResults));
							
				// 2c. Transfer over following edges
				for (ICFGEdge edge : (isForward ? fromNode.getOutputs() : fromNode.getInputs())) {
					ILabel edgeLabel = edge.getLabel();
					ILabel toLabel = incomingLabel(edgeLabel);
					
					// 2c-i. Find node and lattice to merge
					ICFGNode<?> toNode = isForward ? edge.getSink() : edge.getSource();
					LE mergeIntoNode = afterResults.get(edgeLabel);
					
					// 2c-ii. Update following node
					if (resultsBeforeAnalyzing.containsKey(toNode)) {
						// Get the previously stored "before" results of the toNode
						IncomingResult<LE> beforeToResults = (IncomingResult<LE>) resultsBeforeAnalyzing.get(toNode);
						// If the child node's before lattice is not null and the beforeTo 
						// is more precise than the result then join and revisit this child.
						if (! beforeToResults.keySet().contains(toLabel)) {
							// no previous result for this branch
							beforeToResults.put(toLabel, checkNull(mergeIntoNode));
						}
						else if (! mergeIntoNode.atLeastAsPrecise(beforeToResults.get(toLabel), 
								toNode.getASTNode())) {
							if(beforeToResults.get(toLabel).atLeastAsPrecise(mergeIntoNode, toNode.getASTNode()))
								// no need to join, just override existing result
								beforeToResults.put(toLabel, mergeIntoNode);
							else {
								// Make a deep copy of the result lattice
								LE beforeToLatticeCopy = beforeToResults.get(toLabel).copy();
								LE resultLatticeCopy = checkNull(mergeIntoNode.copy());
								// Store the join of the resultLattice and the beforeToLattice
								beforeToResults.put(toLabel,
										beforeToLatticeCopy.join(resultLatticeCopy, toNode.getASTNode()));
							}
						} 
						else
							// in this case we did not update the lattice, so don't change the results
							continue;
					}
					else
						// no previous "before" result for toNode
						resultsBeforeAnalyzing.put(toNode, new IncomingResult<LE>(mergeIntoNode, toLabel));
					
					// 2c-iii. Push on the stack for further processing
					worklist.add(toNode);
				}
			}
			catch(RuntimeException e) {
				// for debugging purposes, catch and rethrow exceptions to print out source AST node where it happened
				log.log(Level.WARNING, "Runtime exception processing node: " + fromNode + " with code " + fromNode.getASTNode(), e);
				throw e;
			}
		}
		return createAnalysisResult(labeledResultsBefore, labeledResultsAfter, nodeMap,
				                    lattice, cfg.getStartNode(), cfg.getEndNode());
    }

    /**
     * Creates an analysis result object from the given result maps.
     * @param labeledResultsBefore Labeled results before AST nodes (relative to normal control flow).
     * @param labeledResultsAfter Labeled results after AST nodes (relative to normal control flow).
     * @param nodeMap Map from AST to CFG nodes, to cover the case where one AST node maps to multiple CFG nodes.
     * @return Analysis result object for the given result maps.
     */
	protected AnalysisResult<LE> createAnalysisResult(
			Map<ICFGNode<?>, IResult<LE>> labeledResultsBefore,
			Map<ICFGNode<?>, IResult<LE>> labeledResultsAfter,
			Map<ASTNode, Set<ICFGNode<?>>> nodeMap,
			Lattice<LE> lattice, ICFGNode _startNode, ICFGNode _endNode) {
		// TODO maybe translate these results to not require the node map anymore, e.g. by merging results for CFG nodes
		return new AnalysisResult<LE>(nodeMap, labeledResultsAfter, labeledResultsBefore, lattice, _startNode, _endNode);
	}

	/**
	 * Turns label on edge into label for incoming analysis results.
	 * {@link edu.cmu.cs.crystal.BooleanLabel}s will be used as-is; 
	 * any other label is replaced by {@link edu.cmu.cs.crystal.NormalLabel}.
	 * @param edgeLabel Label retrieved from edge
	 * @return Label for {@link IncomingResult}
	 * 
	 * @see IncomingResult
	 */
    protected ILabel incomingLabel(ILabel edgeLabel) {
		if(edgeLabel instanceof BooleanLabel)
			return edgeLabel;
		return NormalLabel.getNormalLabel();
	}

	/**
	 * Implement this method to determine the analysis direction for the current worklist run.
	 * @return The analysis direction
	 */
	protected abstract AnalysisDirection getAnalysisDirection();

	/**
	 * Implement this method to create a control flow graph for the current worklist run.
	 * @return CFG for the given method.
	 */
	protected abstract IControlFlowGraph getControlFlowGraph();

	/**
	 * Implement this method to create the lattice to be used in the current worklist run.
	 * @return Lattice to be used for analyzing the given method.
	 */
	protected abstract Lattice<LE> getLattice();

	/**
	 * Implement this method to transfer over the given CFG node based on an
	 * incoming lattice element for a given label.  The label is determined
	 * with {@link #incomingLabel(ILabel)} and distinguishes incoming analysis
	 * results along different kinds of edges.  It is <i>recommended</i> to
	 * treat nodes for &&, ||, and ! specially based on the <code>transferLabel</code>:
	 * && and || should only return a result for a given {@link edu.cmu.cs.crystal.BooleanLabel};
	 * ! should only return a result for the opposite {@link edu.cmu.cs.crystal.BooleanLabel}.
	 * @param cfgNode The CFG node to transfer over.  Notice that, in the case of a dummy node,
	 * there may not be an AST node associated with the CFG node. 
	 * @param incoming The incoming lattice element (relative to the analysis direction).
	 * @param transferLabel Label to distinguish analysis results along different
	 * kinds of edges.
	 * @return Analysis results for labels occurring on the given node's outgoing edges
	 * (relative to the analysis direction).
	 */
	protected abstract IResult<LE> transferNode(
			ICFGNode<?> cfgNode,
			LE incoming, ILabel transferLabel);
	
	/**
	 * Returns a given object only if non-<code>null</code>; throws an exception otherwise.
	 * @param <T>
	 * @param o An object
	 * @return The given object, if non-<code>null</code>.
	 * @throws NullPointerException If the given object is null.
	 */
	protected static <T> T checkNull(T o) {
		assert o != null : "No value available.";
		return o;
	}

	/**
	 * Internal class to hold incoming analysis results distinguished by
	 * false, true, and other incoming edges.
	 * 
	 * @author Kevin Bierhoff
	 *
	 * @param <LE> Lattice element type held by the result.
	 */
	protected static class IncomingResult<LE extends LatticeElement<LE>> implements IResult<LE> {
		
		private LE normalResult;
		private LE falseResult;
		private LE trueResult;
		
		/**
		 * Create new result with given lattice value as normal result
		 * @param normalResult
		 * @see edu.cmu.cs.crystal.NormalLabel
		 */
		public IncomingResult(LE normalResult) {
			this.normalResult = checkNull(normalResult);
		}

		/**
		 * Create new result with given lattice value as boolean result
		 * @param branchResult
		 * @param branchValue Determine whether <code>branchResult</code> should
		 * be the <code>true</code> or <code>false</code> result.
		 * @see edu.cmu.cs.crystal.BooleanLabel
		 */
		public IncomingResult(LE branchResult, boolean branchValue) {
			if(branchValue == true)
				trueResult = checkNull(branchResult);
			else
				falseResult = checkNull(branchResult);
		}

		/**
		 * Create new result with given lattice value as result for given label.
		 * @param label Label to replace value for: {@link edu.cmu.cs.crystal.NormalLabel}
		 * or {@link edu.cmu.cs.crystal.BooleanLabel}.
		 * @see edu.cmu.cs.crystal.NormalLabel
		 */
		public IncomingResult(LE result, ILabel label) {
			put(label, result);
		}

		/**
		 * This constructor is to be <b>only used internally by {@link #join(IResult)}</b>.
		 * It sets the three lattice elements tracked by this result to the given values.
		 * At least one of the parameters must be non-<code>null</code>.
		 * @param normalResult
		 * @param falseResult
		 * @param trueResult
		 */
		protected IncomingResult(LE normalResult, LE falseResult, LE trueResult) {
			if(normalResult == null && falseResult == null && trueResult == null)
				throw new NullPointerException("Only null results provided");
			this.normalResult = normalResult;
			this.falseResult = falseResult;
			this.trueResult = trueResult;
		}

		public LE get(ILabel label) {
			if(NormalLabel.getNormalLabel().equals(label))
				return checkNull(normalResult);
			if(BooleanLabel.getBooleanLabel(false).equals(label))
				return checkNull(falseResult);
			if(BooleanLabel.getBooleanLabel(true).equals(label))
				return checkNull(trueResult);
			throw new IllegalArgumentException("Unknown label: " + label);
		}
		
		/**
		 * Replaces the lattice value for the given label with a new lattice value
		 * @param label Label to set value for: {@link edu.cmu.cs.crystal.NormalLabel}
		 * or {@link edu.cmu.cs.crystal.BooleanLabel}.
		 * @param result The new lattice value.
		 */
		public void put(ILabel label, LE result) {
			if(NormalLabel.getNormalLabel().equals(label))
				normalResult = checkNull(result);
			else if(BooleanLabel.getBooleanLabel(false).equals(label))
				falseResult = checkNull(result);
			else if(BooleanLabel.getBooleanLabel(true).equals(label))
				trueResult = checkNull(result);
			else
				throw new IllegalArgumentException("Unknown label: " + label);
		}

		public Set<ILabel> keySet() {
			HashSet<ILabel> result = new HashSet<ILabel>(3);
			if(normalResult != null)
				result.add(NormalLabel.getNormalLabel());
			if(falseResult != null)
				result.add(BooleanLabel.getBooleanLabel(false));
			if(trueResult != null)
				result.add(BooleanLabel.getBooleanLabel(true));
			return result;
		}

		public IResult<LE> join(IResult<LE> otherResult) {
			if(otherResult == null)
				return this;
			if(otherResult instanceof IncomingResult) {
				IncomingResult<LE> other = (IncomingResult<LE>) otherResult;
				LE nrm = this.normalResult;
				if(other.normalResult != null)
					nrm = (nrm == null) ? other.normalResult : nrm.join(other.normalResult, null);
				LE tru = this.trueResult;
				if(other.trueResult != null)
					tru = (tru == null) ? other.trueResult : tru.join(other.trueResult, null);
				LE fls = this.falseResult;
				if(other.falseResult != null)
					fls = (fls == null) ? other.falseResult : fls.join(other.falseResult, null);
				return new IncomingResult<LE>(nrm, fls, tru);
			}
			throw new IllegalStateException("Internal results should never be joined with results of type: " + otherResult.getClass());
		}
		
	}

}
