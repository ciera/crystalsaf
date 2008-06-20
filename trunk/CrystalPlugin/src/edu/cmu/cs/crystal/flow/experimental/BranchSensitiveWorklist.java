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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrefixExpression;

import edu.cmu.cs.crystal.BooleanLabel;
import edu.cmu.cs.crystal.ILabel;
import edu.cmu.cs.crystal.cfg.ICFGEdge;
import edu.cmu.cs.crystal.cfg.ICFGNode;
import edu.cmu.cs.crystal.flow.AnalysisDirection;
import edu.cmu.cs.crystal.flow.IBranchSensitiveTransferFunction;
import edu.cmu.cs.crystal.flow.IResult;
import edu.cmu.cs.crystal.flow.Lattice;
import edu.cmu.cs.crystal.flow.LatticeElement;

/**
 * This is the branch-sensitive version of the worklist algorithm.
 * Call {@link #performAnalysis()} to run the worklist.
 * @author Kevin Bierhoff
 * @see #checkBreakpoint(ASTNode) for breakpoint support
 * @see BranchInsensitiveWorklist
 */
public class BranchSensitiveWorklist<LE extends LatticeElement<LE>> extends
		AbstractWorklist<LE> {
	
	/** The analysis-specific transfer function. */
	private final IBranchSensitiveTransferFunction<LE> transferFunction;
	/** Cache of label lists for recently visited nodes. */
	private final WeakHashMap<ICFGNode<?>, List<ILabel>> labelMap = new WeakHashMap<ICFGNode<?>, List<ILabel>>();

	/**
	 * Creates a worklist instance for the given method and transfer function.
	 * @param method
	 * @param transfer
	 */
	public BranchSensitiveWorklist(MethodDeclaration method, IBranchSensitiveTransferFunction<LE> transfer) {
		super(method);
		this.transferFunction = transfer;
	}

	/* (non-Javadoc)
	 * @see edu.cmu.cs.crystal.flow.experimental.WorklistTemplate#getAnalysisDirection()
	 */
	@Override
	protected AnalysisDirection getAnalysisDirection() {
		return transferFunction.getAnalysisDirection();
	}

	/* (non-Javadoc)
	 * @see edu.cmu.cs.crystal.flow.experimental.WorklistTemplate#getLattice()
	 */
	@Override
	protected Lattice<LE> getLattice() {
		return transferFunction.getLattice(getMethod());
	}

	/* (non-Javadoc)
	 * @see edu.cmu.cs.crystal.flow.experimental.WorklistTemplate#transferNode(edu.cmu.cs.crystal.cfg.ICFGNode, edu.cmu.cs.crystal.flow.LatticeElement, edu.cmu.cs.crystal.ILabel)
	 */
	@Override
	protected IResult<LE> transferNode(ICFGNode<?> cfgNode, LE incoming,
			ILabel transferLabel) {
		final ASTNode astNode = cfgNode.getASTNode();
		checkBreakpoint(astNode);
		if(transferLabel instanceof BooleanLabel) {
			if(astNode instanceof InfixExpression) {
				if(InfixExpression.Operator.CONDITIONAL_AND.equals(((InfixExpression) astNode).getOperator()) ||
						InfixExpression.Operator.CONDITIONAL_OR.equals(((InfixExpression) astNode).getOperator()))
					return transferFunction.transfer(astNode, Collections.singletonList(transferLabel), incoming);
			}
			else if(astNode instanceof PrefixExpression) {
				if(PrefixExpression.Operator.NOT.equals(((PrefixExpression) astNode).getOperator())) {
					ILabel otherLabel = BooleanLabel.getBooleanLabel(! ((BooleanLabel) transferLabel).getBranchValue());
					return transferFunction.transfer(astNode, Collections.singletonList(otherLabel), incoming);
				}
			}
		}
		return transferFunction.transfer(astNode, getLabels(cfgNode), incoming);
	}

	/**
	 * Returns the labels out of the given node (relative to the analysis direction) 
	 * as a list.
	 * @param cfgNode
	 * @return the labels out of the given node (relative to the analysis direction).
	 */
	private List<ILabel> getLabels(ICFGNode cfgNode) {
		if(labelMap.containsKey(cfgNode))
			return labelMap.get(cfgNode);
		
		Set<ICFGEdge> edges = (AnalysisDirection.FORWARD_ANALYSIS == getAnalysisDirection() ? cfgNode.getOutputs() : cfgNode.getInputs());
		List<ILabel> labels = new LinkedList<ILabel>();
		for(ICFGEdge e : edges) {
			if(labels.contains(e.getLabel()) == false)
				labels.add(e.getLabel());
		}
		labels = Collections.unmodifiableList(labels);
		labelMap.put(cfgNode, labels);
		return labels;
	}

}
