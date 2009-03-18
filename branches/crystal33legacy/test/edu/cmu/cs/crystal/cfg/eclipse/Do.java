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
public class Do {
	// Assume that we are using a class where every method has a unique name
	static Map<String, MethodDeclaration> methods = new HashMap<String, MethodDeclaration>();

	@BeforeClass
	static public void setup() throws CoreException {
		CompilationUnit compUnit = CFGTestUtils.parseCode(Do.class.getName());
		methods = CFGTestUtils.createMethodNameMap(compUnit);
	}

	@Test
	public void simpleTest() throws Exception {
		MethodDeclaration decl = methods.get("simple");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void simple(boolean cond, int a) {
		do {
			a++;
		} while (cond);
	}

	@Test
	public void breakingTest() throws Exception {
		MethodDeclaration decl = methods.get("breaking");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void breaking(boolean cond, int a) {
		do {
			if (a == 1)
				break;
			a++;
		} while (!cond);
	}

	@Test
	public void continuingTest() throws Exception {
		MethodDeclaration decl = methods.get("continuing");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void continuing(boolean cond, int a) {
		do {
			if (a == 3)
				continue;
			++a;
		} while (!cond);
	}

	@Test
	public void returningTest() throws Exception {
		MethodDeclaration decl = methods.get("returning");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void returning(int a) {
		do {
			if (a == 2)
				return;
			a--;
		} while (a > 3);
	}

}
