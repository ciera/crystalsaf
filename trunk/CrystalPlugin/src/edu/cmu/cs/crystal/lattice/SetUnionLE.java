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
package edu.cmu.cs.crystal.lattice;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

import edu.cmu.cs.crystal.simple.LatticeElement;
import edu.cmu.cs.crystal.util.Freezable;

/**
 * @author Kevin Bierhoff
 * @since 3.3.1
 */
public class SetUnionLE<T> implements 
		LatticeElement<SetUnionLE<T>>, Freezable<SetUnionLE<T>>, Set<T> {
	
	private static SetUnionLE BOTTOM = new SetUnionLE();
	
	/**
	 * Returns an immutable <i>bottom</i> empty set lattice element.
	 */
	public static <T> SetUnionLE<T> getBottom() {
		return BOTTOM;
	}
	
	/** The set. Unmodifiable if frozen. */
	private Set<T> set;
	
	/**
	 * Creates an immutable <i>bottom</i> empty set lattice element.
	 */
	private SetUnionLE() {
		set = Collections.emptySet();
	}
	
	/**
	 * Creates a mutable lattice element with a <i>copy</i> of the given set.
	 * @param set Will be copied.
	 */
	private SetUnionLE(Set<T> set) {
		set = new HashSet<T>(set);
	}

	public Set<T> getSet() {
		return set;
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.Set#add(java.lang.Object)
	 */
	public boolean add(T e) {
		return set.add(e);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Set#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends T> c) {
		return set.addAll(c);
	}

	/**
	 * 
	 * @see java.util.Set#clear()
	 */
	public void clear() {
		set.clear();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.Set#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return set.contains(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Set#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	/**
	 * @return
	 * @see java.util.Set#isEmpty()
	 */
	public boolean isEmpty() {
		return set.isEmpty();
	}

	/**
	 * @return
	 * @see java.util.Set#iterator()
	 */
	public Iterator<T> iterator() {
		return set.iterator();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.Set#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return set.remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Set#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return set.removeAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Set#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		return set.retainAll(c);
	}

	/**
	 * @return
	 * @see java.util.Set#size()
	 */
	public int size() {
		return set.size();
	}

	/**
	 * @return
	 * @see java.util.Set#toArray()
	 */
	public Object[] toArray() {
		return set.toArray();
	}

	/**
	 * @param <T>
	 * @param a
	 * @return
	 * @see java.util.Set#toArray(T[])
	 */
	public <T> T[] toArray(T[] a) {
		return set.toArray(a);
	}

	public boolean atLeastAsPrecise(SetUnionLE<T> other, ASTNode node) {
		return other.getSet().containsAll(this.getSet());
	}

	public SetUnionLE<T> copy() {
		return freeze();
	}

	public SetUnionLE<T> join(SetUnionLE<T> other, ASTNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	public SetUnionLE<T> freeze() {
		set = Collections.unmodifiableSet(set);
		return this;
	}

	public SetUnionLE<T> mutableCopy() {
		return new SetUnionLE<T>(set);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((set == null) ? 0 : set.hashCode());
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
		final SetUnionLE other = (SetUnionLE) obj;
		if (set == null) {
			if (other.set != null)
				return false;
		} else if (!set.equals(other.set))
			return false;
		return true;
	}

}
