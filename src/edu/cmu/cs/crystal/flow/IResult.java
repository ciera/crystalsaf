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

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

import edu.cmu.cs.crystal.ILabel;

/**
 * Interface for mapping branch labels to analysis information.
 * Clients do not usually have to implement this interface.  Instead, use
 * one of the pre-defined implementing classes.
 * 
 * @author Kevin Bierhoff
 * 
 * @param <LE>	the LatticeElement subclass that represents the analysis knowledge
 */
public interface IResult<LE extends LatticeElement<LE>> {

	/**
	 * If label is null, provide a default value.
	 * @param label
	 * @return A valid lattice element or <code>null</code> if the label
	 * is unknown.
	 */
	public LE get(ILabel label);
	
	/**
	 * Returns the set of labels mapped by this <code>IResult</code>.
	 * @return The set of labels mapped by this <code>IResult</code>.
	 * This method must not return <code>null</code>
	 */
	public Set<ILabel> keySet();
	
	/**
	 * Join two results "pointwise" by joining lattice elements with 
	 * the same label.  This method must not modify either <code>IResult</code>
	 * objects passed in.
	 * @param otherResult <code>IResult</code> object to join this <code>IResult</code> with.
	 * @return Pointwise joined lattice elements.
	 * 
	 * @see LatticeElement#join(LatticeElement)
	 */
	public IResult<LE> join(IResult<LE> otherResult);
}
