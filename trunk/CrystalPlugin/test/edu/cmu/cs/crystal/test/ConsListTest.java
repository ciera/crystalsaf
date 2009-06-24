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
package edu.cmu.cs.crystal.test;

import static edu.cmu.cs.crystal.util.ConsList.cons;
import static edu.cmu.cs.crystal.util.ConsList.list;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

import edu.cmu.cs.crystal.util.ConsList;
import edu.cmu.cs.crystal.util.Lambda2;

public class ConsListTest {

	@Test
	public void testEmpty() {
		
		ConsList<Integer> l = ConsList.empty();
		
		assertTrue(l.isEmpty());
		assertTrue(l.tl().isEmpty());
		assertEquals(l.size(), 0);
	}

	@Test
	public void testSingleton() {
		ConsList<Integer> l = ConsList.singleton(4);
		
		assertFalse(l.isEmpty());
		assertEquals(l.size(), 1);
		assertEquals(l.hd().intValue(), 4);
	}

	@Test
	public void testCons() {
		ConsList<Integer> l = ConsList.singleton(1);
		
		l = cons(2, l);
		l = cons(3, l);
		l = cons(4, l);
		
		assertEquals(l.size(), 4);
		assertFalse(l.isEmpty());
		assertEquals(l.hd().intValue(), 4);
	}

	@Test
	public void testConcat() {
		ConsList<Integer> l = ConsList.list(1,2,3,4,5,6,7,8,9,10);
		ConsList<Integer> front = ConsList.list(1,2,3,4,5);
		ConsList<Integer> back = ConsList.list(6,7,8,9,10);
		
		assertEquals(l, ConsList.concat(front, back));
	}
	
	@Test
	public void testTl() {
		ConsList<Integer> l = ConsList.singleton(1);
		ConsList<Integer> l2 = cons(2, l);
		
		assertEquals(l, l2.tl());
		assertEquals(l2.tl().size(), 1);
	}

	@Test
	public void testRemoveElement() {
		ConsList<Integer> l = ConsList.singleton(1);
		assertTrue(l.removeElement(1).isEmpty());
		assertEquals(l.removeElement(1).size(),0);
		
		l = cons(2, l);
		l = cons(3, l);
		l = cons(1, l);
		l = cons(1, l);
		l = l.removeElement(1);
		assertEquals(l.size(), 2);
	}

	@Test
	public void testIterator() {
		ConsList<Integer> l = ConsList.list(5,4,3,2,1);
		
		Iterator<Integer> iter = l.iterator();
		
		assertTrue(iter.hasNext());
		assertEquals(iter.next().intValue(), 5);
		
		assertTrue(iter.hasNext());
		assertEquals(iter.next().intValue(), 4);
		
		assertTrue(iter.hasNext());
		assertEquals(iter.next().intValue(), 3);
		
		assertTrue(iter.hasNext());
		assertEquals(iter.next().intValue(), 2);
		
		assertTrue(iter.hasNext());
		assertEquals(iter.next().intValue(), 1);
		
		assertFalse(iter.hasNext());
	}

	@Test
	public void testFoldl() {
		ConsList<Integer> l = list(5,4,3,2,1);
		
		Integer result =
		l.foldl(new Lambda2<Integer,Integer,Integer>(){
			public Integer call(Integer i1, Integer i2) {
				return i1 + i2;
			}}, 0);
		assertEquals(result.intValue(), 15);
	}
	
	@Test
	public void testList() {
		ConsList<Integer> l = list(1,2,3,4,5);
		
		Iterator<Integer> iter = l.iterator();
		
		assertTrue(iter.hasNext());
		assertEquals(iter.next().intValue(), 1);
		
		assertTrue(iter.hasNext());
		assertEquals(iter.next().intValue(), 2);
		
		assertTrue(iter.hasNext());
		assertEquals(iter.next().intValue(), 3);
		
		assertTrue(iter.hasNext());
		assertEquals(iter.next().intValue(), 4);
		
		assertTrue(iter.hasNext());
		assertEquals(iter.next().intValue(), 5);
		
		assertFalse(iter.hasNext());
	}
	
	public void testContains() {
		ConsList<Integer> l = list(4,5,6,7);
		
		assertTrue(l.contains(6));
		assertFalse(l.contains(9));
	}
}
