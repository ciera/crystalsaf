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

/**
 * @author Ciera Jaspan
 * 
 */
public class Infix {
	// Assume that we are using a class where every method has a unique name
	static Map<String, MethodDeclaration> methods = new HashMap<String, MethodDeclaration>();

	@BeforeClass
	static public void setup() throws CoreException {
		CompilationUnit compUnit = CFGTestUtils
				.parseCode(Infix.class.getName());
		methods = CFGTestUtils.createMethodNameMap(compUnit);
	}

	@Test
	public void simpleTest() throws Exception {
		MethodDeclaration decl = methods.get("simple");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public int simple(int a, int b) {
		return a + b;
	}

	@Test
	public void shortcircuitTest() throws Exception {
		MethodDeclaration decl = methods.get("shortcircuit");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public boolean shortcircuit(boolean a, boolean b, boolean c, boolean d) {
		return a && b && c && d;
	}

	@Test
	public void multipleTest() throws Exception {
		MethodDeclaration decl = methods.get("multiple");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public int multiple(int a, int b, int c, int d) {
		return a + b + c + d;
	}

	@Test
	public void orderingTest() throws Exception {
		MethodDeclaration decl = methods.get("ordering");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public int ordering(int a, int b, int c, int d) {
		return a + b * c + d;
	}

}
