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
public class SetIntersectionLE<K> 
		implements LatticeElement<SetIntersectionLE<K>>, 
		Freezable<SetIntersectionLE<K>>,
		Set<K> {
	
	/** The set.  Unmodifiable if frozen. */
	private Set<K> set;
	
	/**
	 * Creates a bottom lattice element.
	 * @param <K>
	 * @return a bottom lattice element.
	 */
	public static <K> SetIntersectionLE<K> createBottom() {
		return new SetIntersectionLE<K>(null);
	}
	
	/**
	 * Creates a modifiable top lattice element (empty set).
	 * @param <K>
	 * @return a top lattice element.
	 */
	public static <K> SetIntersectionLE<K> createTop() {
		return new SetIntersectionLE<K>();
	}
	
	/**
	 * Creates a modifiable lattice element with a copy of the given set.
	 * @param <K>
	 * @param set
	 * @return a modifiable lattice element with a copy of the given set.
	 */
	public static <K> SetIntersectionLE<K> createSet(Set<? extends K> set) {
		return new SetIntersectionLE<K>(new HashSet<K>(set));
	}
	
	/**
	 * Uses the given set.
	 * @param set
	 */
	private SetIntersectionLE(Set<K> set) {
		this.set = set;
	}
	
	/**
	 * Empty modifiable set.
	 */
	private SetIntersectionLE() {
		this.set = new HashSet<K>();
	}

	/**
	 * Tests whether this is bottom.
	 * @return <code>true</code> if this is bottom, <code>false</code> otherwise.
	 */
	public boolean isBottom() {
		return set == null;
	}
	
	/**
	 * The set or <code>null</code> if this is {@link #isBottom() bottom}.
	 * @return The set or <code>null</code>.
	 */
	public Set<K> get() {
		return set;
	}
	
	/**
	 * Tests whether any of the given elements is in the set.
	 * @param elements
	 * @return
	 */
	public boolean containsAny(Collection<?> elements) {
		if(isBottom())
			return true;
		if(elements.isEmpty())
			return false;
		for(Object e : elements) {
			if(set.contains(e))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.cmu.cs.crystal.flow.LatticeElement#atLeastAsPrecise(edu.cmu.cs.crystal.flow.LatticeElement, org.eclipse.jdt.core.dom.ASTNode)
	 */
	public boolean atLeastAsPrecise(SetIntersectionLE<K> other, ASTNode node) {
		this.freeze();
		if(this == other)
			return true;
		if(other == null)
			return false;
		other.freeze();
		if(this.isBottom())
			return true;
		if(other.isBottom())
			return false;
		return this.get().containsAll(other.get());
	}

	/* (non-Javadoc)
	 * @see edu.cmu.cs.crystal.flow.LatticeElement#copy()
	 */
	public SetIntersectionLE<K> copy() {
		return freeze();
	}

	/* (non-Javadoc)
	 * @see edu.cmu.cs.crystal.flow.LatticeElement#join(edu.cmu.cs.crystal.flow.LatticeElement, org.eclipse.jdt.core.dom.ASTNode)
	 */
	public SetIntersectionLE<K> join(SetIntersectionLE<K> other, ASTNode node) {
		this.freeze();
		if(this == other || other == null)
			return this;
		other.freeze();
		if(other.isBottom())
			return this;
		if(this.isBottom())
			return other;
		HashSet<K> newSet = new HashSet<K>(this.get());
		newSet.retainAll(other.get());
		return create(newSet).freeze();
	}

	/* (non-Javadoc)
	 * @see edu.cmu.cs.crystal.internal.Freezable#freeze()
	 */
	public SetIntersectionLE<K> freeze() {
		if(set != null)
			set = Collections.unmodifiableSet(set);	
		return this;
	}

	/* (non-Javadoc)
	 * @see edu.cmu.cs.crystal.internal.Freezable#mutableCopy()
	 */
	public SetIntersectionLE<K> mutableCopy() {
		if(set == null)
			return this;
		return create(new HashSet<K>(set));
	}

	/**	
	 * Creates a new lattice element with the given set.
	 * @param set
	 * @return
	 */
	private SetIntersectionLE<K> create(HashSet<K> set) {
		return new SetIntersectionLE<K>(set);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.Set#add(java.lang.Object)
	 */
	public boolean add(K e) {
		if(set == null)
			return false;
		return set.add(e);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Set#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends K> c) {
		if(set == null)
			return false;
		return set.addAll(c);
	}

	/**
	 * 
	 * @see java.util.Set#clear()
	 */
	public void clear() {
		if(set != null)
			set.clear();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.Set#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		if(set == null)
			return true;
		return set.contains(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Set#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		if(set == null)
			return true;
		return set.containsAll(c);
	}

	/**
	 * @return
	 * @see java.util.Set#isEmpty()
	 */
	public boolean isEmpty() {
		if(set == null)
			return false;
		return set.isEmpty();
	}

	/**
	 * @return
	 * @see java.util.Set#iterator()
	 */
	public Iterator<K> iterator() {
		if(set == null)
			return null;
		return set.iterator();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.Set#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		if(set == null)
			return true;
		return set.remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Set#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		if(set == null)
			return true;
		return set.removeAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Set#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		if(set == null)
			return false;
		return set.retainAll(c);
	}

	/**
	 * @return
	 * @see java.util.Set#size()
	 */
	public int size() {
		if(set == null)
			return -1;
		return set.size();
	}

	/**
	 * @return
	 * @see java.util.Set#toArray()
	 */
	public Object[] toArray() {
		if(set == null)
			return null;
		return set.toArray();
	}

	/**
	 * @param <T>
	 * @param a
	 * @return
	 * @see java.util.Set#toArray(T[])
	 */
	public <T> T[] toArray(T[] a) {
		if(set == null)
			return null;
		return set.toArray(a);
	}

}
