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

/**
 * Lattice provides the base knowledge (embodied in a LatticeElement) 
 * for places where no prior knowledge existed.
 * 
 * @author David Dickey
 * @author Jonathan Aldrich
 * 
 * @param <LE>	the LatticeElement subclass that represents the analysis knowledge
 */
public final class Lattice<LE extends LatticeElement<LE>>  {
	private LE entry;
	private LE bottom;


	/**
	 * Constructor.  Instantiates a lattice.
	 * 
	 * @param entry	the entry lattice
	 * @param bottom	the "bottom" lattice
	 */
	public Lattice(LE entry, LE bottom) {
		this.entry = entry;
		this.bottom = bottom;
	}

	/**
	 * Returns the stored entry lattice.
	 * 
	 * @return		the lattice that represents entry
	 */
	public LE entry() {
		return entry;
	}
	
	/**
	 * Responsible for returning a lattice that represents no knowledge.
	 * 
	 * @return		the lattice that represents "bottom"
	 */
	public LE bottom() {
		return bottom;
	}
}
