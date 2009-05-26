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
package edu.cmu.cs.crystal.analysis.alias;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

import edu.cmu.cs.crystal.bridge.LatticeElement;

/**
 * This is an immutable lattice. 
 *
 */
public class AliasLE implements LatticeElement<AliasLE>, Aliasing {

	protected final Set<ObjectLabel> labels; // this is an immutable set
	
	public static AliasLE create(Set<ObjectLabel> newLabels) {
		return new AliasLE(newLabels);
	}
	
	public static AliasLE create(ObjectLabel label) {
		return new AliasLE(label);
	}
	
	public static AliasLE bottom() {
		return new AliasLE();
	}
	
	protected AliasLE() {
		this.labels = Collections.emptySet();
	}
	
	protected AliasLE(ObjectLabel label) {
		this.labels = Collections.singleton(label);
	}

	protected AliasLE(Set<ObjectLabel> labels) {
		this.labels = Collections.unmodifiableSet(labels);
	}

	/* (non-Javadoc)
	 * @see edu.cmu.cs.crystal.analysis.alias.Aliasing#getLabels()
	 */
	public Set<ObjectLabel> getLabels() {
		return labels;
	} 
	
//	public void removeAlias(ObjectLabel removeVar) {
//		labels.remove(removeVar);
//	}
//
//	public void addAlias(ObjectLabel addVar) {
//		labels.add(addVar);
//	}

	public boolean atLeastAsPrecise(AliasLE other, ASTNode node) {
		return other.labels.containsAll(labels);
	}

	public AliasLE copy() {
		return this;
	}

	public AliasLE join(AliasLE other, ASTNode node) {
		HashSet<ObjectLabel> copy;
		copy = new HashSet<ObjectLabel>(this.labels);
		copy.addAll(other.labels);
		return new AliasLE(copy);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((labels == null) ? 0 : labels.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		final AliasLE other = (AliasLE) obj;
		return labels.containsAll(other.labels) && other.labels.containsAll(labels);
	}
	
	
	public String toString() {
		return labels.toString();
	}

	/* (non-Javadoc)
	 * @see edu.cmu.cs.crystal.analysis.alias.Aliasing#hasAnyLabels(java.util.Set)
	 */
	public boolean hasAnyLabels(Set<ObjectLabel> labelsToFind) {
		for (ObjectLabel label : labelsToFind)
			if (labels.contains(label))
				return true;
		return false;
	}

}
