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
package edu.cmu.cs.crystal.flow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrefixExpression;

import edu.cmu.cs.crystal.BooleanLabel;
import edu.cmu.cs.crystal.Crystal;
import edu.cmu.cs.crystal.ILabel;
import edu.cmu.cs.crystal.cfg.ICFGEdge;
import edu.cmu.cs.crystal.cfg.ICFGNode;
import edu.cmu.cs.crystal.cfg.IControlFlowGraph;
import edu.cmu.cs.crystal.cfg.eclipse.EclipseCFG;

/**
 * This class implements a branch-sensitive flow analysis.  Implement 
 * {@link IBranchSensitiveTransferFunction} to define a specific analysis.  
 * 
 * @author Kevin Bierhoff
 *
 * @param <LE>	the LatticeElement subclass that represents the analysis knowledge
 *
 * @see edu.cmu.cs.crystal.tac.BranchSensitiveTACAnalysis
 * @deprecated Use FlowAnalysis instead.
 */
public class BranchSensitiveFlowAnalysis<LE extends LatticeElement<LE>> extends MotherFlowAnalysis<LE> {
	
	protected IBranchSensitiveTransferFunction<LE> transferFunction;

	public BranchSensitiveFlowAnalysis(Crystal crystal, IBranchSensitiveTransferFunction<LE> transferFunction) {
		super(crystal);
		this.transferFunction = transferFunction;
	}

	@Override
	protected IFlowAnalysisDefinition<LE> createTransferFunction(MethodDeclaration method) {
		return transferFunction;
	}
}
