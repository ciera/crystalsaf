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
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * All flow analyses must be able to return the information defined by this
 * interface.
 * 
 * @author Nels Beckman
 *
 * @param <LE>
 */
public interface IFlowAnalysis<LE> {

	/**
	 * Retrieves the analysis state that exists <b>before</b> analyzing the node.
	 * 
	 * Before is respective to normal program flow and not the direction of the analysis.
	 * 
	 * @param node		the {@link ASTNode} of interest 
	 * @return			the lattice that represents the analysis state 
	 * 					before analyzing the node.  Or null if the node doesn't
	 * 					have a corresponding control flow node.
	 */
	public LE getResultsBefore(ASTNode node);

	/**
	 * Retrieves the analysis state that exists <b>after</b> analyzing the node.
	 * 
	 * After is respective to normal program flow and not the direction of the analysis.
	 * 
	 * @param node		the {@link ASTNode} of interest 
	 * @return			the lattice that represents the analysis state 
	 * 					before analyzing the node.  Or null if the node doesn't
	 * 					have a corresponding control flow node.
	 */
	public LE getResultsAfter(ASTNode node);

	/**
	 * Get the analysis lattice before the first ASTNode in the CFG.
	 * 
	 * @param d Method declaration for the method you need the results for.
	 */
	public LE getStartResults(MethodDeclaration d);
	
	/**
	 * Get the analysis lattice after the last ASTNode in the CFG.
	 * 
	 * @param d Method declaration for the method you need the results for.
	 */
	public LE getEndResults(MethodDeclaration d);
	
	/**
	 * Retrieves the analysis state that exists <b>before</b> analyzing the node.
	 * 
	 * Before is respective to normal program flow and not the direction of the analysis.
	 * 
	 * @param node		the {@link ASTNode} of interest 
	 * @return			the lattice that represents the analysis state 
	 * 					after analyzing the node.  Or null if the node doesn't
	 * 					have a corresponding control flow node.
	 */
	public IResult<LE> getLabeledResultsBefore(ASTNode node);

	/**
	 * Retrieves the analysis state that exists <b>after</b> analyzing the node.
	 * 
	 * After is respective to normal program flow and not the direction of the analysis.
	 * 
	 * @param node		the {@link ASTNode} of interest 
	 * @return			the lattice that represents the analysis state 
	 * 					after analyzing the node.  Or null if the node doesn't
	 * 					have a corresponding control flow node.
	 */
	public IResult<LE> getLabeledResultsAfter(ASTNode node);

	/**
	 * Get the analysis lattice before the first ASTNode in the CFG, with
	 * labels.
	 * 
	 * @param d Method declaration for the method you need the results for.
	 */
	public IResult<LE> getLabeledStartResult(MethodDeclaration d);
	
	/**
	 * Get the analysis lattice after the last ASTNode in the CFG, which is
	 * a merge of all the actual end nodes, with labels.
	 * 
	 * @param d Method declaration for the method you need the results for.
	 */
	public IResult<LE> getLabeledEndResult(MethodDeclaration d);
}