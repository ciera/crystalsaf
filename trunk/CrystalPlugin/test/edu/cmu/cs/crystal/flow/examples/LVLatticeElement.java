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
package edu.cmu.cs.crystal.flow.examples;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;

import edu.cmu.cs.crystal.simple.LatticeElement;
/**
 * Live Variables Lattice Element Example.
 * 
 * @author David Dickey
 *
 */
public class LVLatticeElement implements LatticeElement<LVLatticeElement> {
	private Set<IBinding> liveVariables;
	
	public LVLatticeElement() {
		liveVariables = new HashSet<IBinding>();
	}
	
	public LVLatticeElement addVariable(IBinding binding) {
		this.liveVariables.add(binding);
		return this;
	}	
	public LVLatticeElement removeVariable(IBinding binding) {
		this.liveVariables.remove(binding);
		return this;
	}

	public Iterator<IBinding> iterator() {
		return liveVariables.iterator();
	}
	
	public LVLatticeElement join(LVLatticeElement other, ASTNode node) {
		// Join includes all variables from all paths
		this.liveVariables.addAll(other.liveVariables);
		return this;
	}
	/**
	 * Example: {x, y} is LESS PRECISE than {x}
	 * Therefore: {x}.atLeastAsPrecise({x, y}) = true
	 * Therefore: {x}.atLeastAsPrecise({x}) = true
	 * Therefore: {x, y}.atLeastAsPrecise({x}) = false
	 * @param other
	 * @return
	 */
	public boolean atLeastAsPrecise(LVLatticeElement other, ASTNode node) {
		Iterator<IBinding> i = this.liveVariables.iterator();
		IBinding binding;
		// If ALL elements in this are in other, then
		// this is atLeastAsPrecise as other
		while(i.hasNext()) {
			binding = i.next();
			if(!other.liveVariables.contains(binding))
				return false;
		}	
		return true;
	}

	/**
	 * Performs a deep copy, but doesn't make copy of ASTNodes
	 */
	public LVLatticeElement copy() {
		LVLatticeElement newLattice = new LVLatticeElement();
		newLattice.liveVariables.addAll(this.liveVariables);
		return newLattice;
	}

	public String toString() {
		String s = "LVA: ";
		if(liveVariables.size() > 0) {
			Iterator<IBinding> i = liveVariables.iterator();
			IBinding next;
			boolean isFirst = true;
			s += "{";
			for(;i.hasNext();) {
				next = i.next();
				if(isFirst)
					isFirst = false;
				else
					s += ", ";
				s += next.getName().toString();
			}
			s += "}";
		} else {
			s += "(empty)";
		}
		return s;
	}

	public boolean equal(Object object) {
		if(!(object instanceof LVLatticeElement))
			throw new RuntimeException("Equality on non-LVLatticeElement");
		LVLatticeElement inLattice = (LVLatticeElement) object;
		return inLattice.liveVariables.equals(this.liveVariables);
	}

}
