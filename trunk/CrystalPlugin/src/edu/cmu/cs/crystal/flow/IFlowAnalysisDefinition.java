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

import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * 
 * @author Kevin Bierhoff
 *
 * @param <LE>
 */
public interface IFlowAnalysisDefinition<LE extends LatticeElement<LE>> {

	/**
	 * Retrieves the lattice for a method.
	 * 
	 * @param methodDeclaration the method to get the entry lattice for
	 * @return the entry lattice for the specified method
	 */
	public Lattice<LE> getLattice(MethodDeclaration methodDeclaration);

	/**
	 * Informs the FlowAnalysis which direction to perform the analysis.
	 * Default is a Forward analysis.
	 * <p>
	 * Use AnalysisDirection enumeration. 
	 * Currently BACKWARD_ANALYSIS doesn't work. 
	 * 
	 * @return	the direction of the analysis
	 */
	public AnalysisDirection getAnalysisDirection();

}