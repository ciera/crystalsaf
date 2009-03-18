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
package edu.cmu.cs.crystal.cfg.eclipse;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class For {
	// Assume that we are using a class where every method has a unique name
	static Map<String, MethodDeclaration> methods = new HashMap<String, MethodDeclaration>();

	@BeforeClass
	static public void setup() throws CoreException {
		CompilationUnit compUnit = CFGTestUtils.parseCode(For.class.getName());
		methods = CFGTestUtils.createMethodNameMap(compUnit);
	}

	@Test
	public void simpleTest() throws Exception {
		MethodDeclaration decl = methods.get("simple");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void simple(int[] array, boolean cond) {
		int foo = 0;
		for (int ndx = 0; ndx < array.length; ndx++) {
			foo++;
		}
	}

	@Test
	public void breakingTest() throws Exception {
		MethodDeclaration decl = methods.get("breaking");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void breaking(int[] array, boolean cond) {
		int foo = 0;
		for (int ndx = 0, k = 1; cond; ndx = foo + ndx, --foo) {
			if (!cond)
				break;
			k--;
		}
	}

	@Test
	public void continuingTest() throws Exception {
		MethodDeclaration decl = methods.get("continuing");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void continuing(int[] array, boolean cond) {
		int foo = 0;
		for (int ndx = 0, k = 1; cond; ndx = foo + ndx, --foo) {
			if (!cond)
				continue;
			k--;
		}
	}

	@Test
	public void emptyCondTest() throws Exception {
		MethodDeclaration decl = methods.get("emptyCond");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void emptyCond(boolean cond) {
		for (int ndx = 0;; ndx++) {
			if (cond)
				break;
		}
	}

	@Test
	public void emptyInitsTest() throws Exception {
		MethodDeclaration decl = methods.get("emptyInits");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void emptyInits(int ndx, boolean cond) {
		for (; cond; ndx++) {
			ndx++;
		}
	}

	@Test
	public void emptyUpdatesTest() throws Exception {
		MethodDeclaration decl = methods.get("emptyUpdates");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void emptyUpdates(boolean cond) {
		for (int ndx = 0; cond;) {
			ndx++;
		}
	}

	@Test
	public void blankTest() throws Exception {
		MethodDeclaration decl = methods.get("blank");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void blank(boolean cond) {
		for (;;) {
			if (cond)
				break;
		}
	}

	@Test
	public void infiniteTest() throws Exception {
		MethodDeclaration decl = methods.get("infinite");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void infinite(boolean cond) {
		for (int ndx = 0;; ndx++)
			;
	}

}
