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

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Interface for defining standard flow analyses.  Implement this interface directly
 * or use {@link FlowAnalysisDefinition}.  Use {@link IBranchSensitiveTransferFunction}
 * to define branch-sensitive analyses.  To create a flow analysis, pass an instance of this
 * interface to {@link FlowAnalysis}.
 * @author Kevin Bierhoff
 *
 */
public interface ITransferFunction<LE extends LatticeElement<LE>> extends IFlowAnalysisDefinition<LE> {
	
	/**
	 * Transfer over a given AST node.  
	 * @param astNode The node to transfer over.
	 * @param value Incoming analysis information.
	 * @return Analysis information after transferring over the given node.
	 */
	public LE transfer(ASTNode astNode, LE value);
}
