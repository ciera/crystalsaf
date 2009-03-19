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
package edu.cmu.cs.crystal.tac.examples;

import org.eclipse.jdt.core.dom.ASTNode;

import edu.cmu.cs.crystal.simple.LatticeElement;

public class DivideByZeroLatticeElement implements
		LatticeElement<DivideByZeroLatticeElement> {

	/** There are only four lattice values: bottom, MAYBEZERO (top), NONZERO, and ZERO */
	static final DivideByZeroLatticeElement MAYBEZERO = new DivideByZeroLatticeElement("MAYBEZERO");

	static final DivideByZeroLatticeElement bottom = new DivideByZeroLatticeElement("bottom");

	static final DivideByZeroLatticeElement NONZERO = new DivideByZeroLatticeElement("NONZERO");

	static final DivideByZeroLatticeElement ZERO = new DivideByZeroLatticeElement("ZERO");

	private final String name;

	/** Private constructor used because this is a singleton pattern; clients should not
	 * create new instances
	 * @param _name  The name of this lattice elemeent
	 */
	private DivideByZeroLatticeElement(String _name) {
		name = _name;
	}
	
	/**
	 * A short string representation of this Divide by zero lattice element.
	 */
	public String toString()
	{
		return name;
	}
	
	public boolean atLeastAsPrecise(DivideByZeroLatticeElement other, ASTNode node) {
		// true if elements equal
		if (other.equals(this))
		{
			return true;
		}
		// bottom more precise than any other
		else if (this.equals(bottom))
		{
			return true;
		}
		// top less precise than any other
		else if (other.equals(MAYBEZERO))
		{
			return true;
		}
		// otherwise other is more precise, or no relationship
		else
		{
			return false;
		}
	}

	/** Since our lattice elements are immutable, it would be OK
	 * for the copy operation to return this.  That would be
	 * dangerous, however, for mutable lattices, so as a more
	 * general best practice we've written copy to actually
	 * create a new object here.
	 */
	public DivideByZeroLatticeElement copy() {
		return new DivideByZeroLatticeElement(this.name);
	}

	public DivideByZeroLatticeElement join(DivideByZeroLatticeElement other, ASTNode node) {
		// join of equal elements is the element
		if (other.equals(this))
		{
			return this;
		}
		// join of X and bottom is X
		else if (other.equals(bottom))
		{
			return this;
		}
		else if (this.equals(bottom))
		{
			return other;
		}
		// any other join is top (MAYBEZERO)
		else
		{
			return MAYBEZERO;
		}
	}

	@Override
	public boolean equals(Object object)
	{
		if (!(object instanceof DivideByZeroLatticeElement))
		{
			return false;
		}
		else
		{
			return this.name.equals(((DivideByZeroLatticeElement)object).name);
		}
	}
	
}
