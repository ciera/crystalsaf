/**
 * Copyright (c) 2006, 2007, 2008 Marwan Abi-Antoun, Jonathan Aldrich, Nels E. Beckman, Kevin
 * Bierhoff, David Dickey, Ciera Jaspan, Thomas LaToza, Gabriel Zenarosa, and others.
 * 
 * This file is part of Crystal.
 * 
 * Crystal is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Crystal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with Crystal. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package edu.cmu.cs.crystal.cfg.eclipse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MethodDecl {
	// Assume that we are using a class where every method has a unique name
	static Map<String, MethodDeclaration> methods = new HashMap<String, MethodDeclaration>();
	static IProject project;

	@BeforeClass
	static public void setup() throws CoreException {
		CompilationUnit compUnit = CFGTestUtils.parseCode(MethodDecl.class.getName());
		methods = CFGTestUtils.createMethodNameMap(compUnit);
	}

	@Test
	public void completelyEmptyTest() throws Exception {
		MethodDeclaration decl = methods.get("completelyEmpty");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void completelyEmpty() {}

	@Test
	public void abstractTest() throws Exception {
		MethodDeclaration decl = methods.get("abstractMethod");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public abstract class AbsDecl {
		public abstract void abstractMethod();
	}

	@Test
	public void withParamsTest() throws Exception {
		MethodDeclaration decl = methods.get("withParams");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void withParams(String str, int a) {}

	@Test
	public void throwsExceptionsTest() throws Exception {
		MethodDeclaration decl = methods.get("throwsExceptions");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void throwsExceptions(String str, int a) throws IOException {}

	@Test
	public void throwsExceptionsNoParamsTest() throws Exception {
		MethodDeclaration decl = methods.get("throwsExceptionsNoParams");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void throwsExceptionsNoParams() throws IOException {}

	@Test
	public void varsTest() throws Exception {
		MethodDeclaration decl = methods.get("vars");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void vars(String bar) {
		String foo;
		boolean cond = false;
		int x, y;
		int z = 9;
	}

	@Test
	public void hasBodyTest() throws Exception {
		MethodDeclaration decl = methods.get("hasBody");
		Assert.assertTrue(CFGTestUtils.testAndCompareCFG(decl));
	}

	public void hasBody() {
		int x, y, z = 9;
		x = 5;
		y = x + z;
		z++;
	}
}
