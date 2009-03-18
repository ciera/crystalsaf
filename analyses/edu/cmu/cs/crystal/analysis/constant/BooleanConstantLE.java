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
package edu.cmu.cs.crystal.analysis.constant;


import org.eclipse.jdt.core.dom.ASTNode;

import edu.cmu.cs.crystal.flow.Lattice;
import edu.cmu.cs.crystal.flow.LatticeElement;

public enum BooleanConstantLE implements LatticeElement<BooleanConstantLE> {

	TRUE,
	FALSE,
	UNKNOWN,
	BOTTOM;     // bottom
	
	static final Lattice<BooleanConstantLE> lattice = new Lattice<BooleanConstantLE>(
			UNKNOWN, BOTTOM);
	

	public BooleanConstantLE join(BooleanConstantLE other, ASTNode node)
	{
		if (other == this)
		{
			return this;
		}
		else if (other == BOTTOM)
		{
			return this;
		}
		else if (this == BOTTOM)
		{
			return other;
		}
		else
		{
			return UNKNOWN;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.cmu.cs.crystal.flow.LatticeElement#atLeastAsPrecise(LE)
	 */
	public boolean atLeastAsPrecise(BooleanConstantLE other, ASTNode node)
	{
		if (other == this)
		{
			return true;
		}
		else if (this == BOTTOM)
		{
			return true;
		}
		else if (other == UNKNOWN)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.cmu.cs.crystal.flow.LatticeElement#copy()
	 */
	public BooleanConstantLE copy()
	{
		return this;
	}
	
	public String toString() {
		if (this == BOTTOM)
			return "Not Boolean";
		else if (this == UNKNOWN)
			return "Unknown";
		else if (this == TRUE)
			return "True";
		else
			return "False";
	}

}
